package com.devformed.sylphiette.service;

import com.devformed.sylphiette.config.ChatGptConfig;
import com.devformed.sylphiette.util.MessageUtils;
import com.google.common.collect.Lists;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

@Log
@Component
public class ChatGptResponder {

	private final ChatGptConfig gptConfig;
	private final ChatGPT chatGPT;
	private final com.plexpt.chatgpt.entity.chat.Message systemMsg;

	@Autowired
	public ChatGptResponder(ChatGptConfig gptConfig) {
		this.gptConfig = gptConfig;
		this.chatGPT = ChatGPT.builder()
				.apiKey(gptConfig.key())
				.build().init();
		this.systemMsg = com.plexpt.chatgpt.entity.chat.Message.ofSystem(gptConfig.systemMsg());

	}

	public String askBot(Message message, String botId) {
		return  message.getChannel().getHistoryBefore(message, 50)
				.map(history -> askBot(history, message, botId)).complete();
	}

	private String askBot(MessageHistory history, Message current, String botId) {
		int promptTokens = getExpectedTokens(current);

		var gptMessages = Lists.newArrayList(systemMsg);
		getIncludedMessages(history, promptTokens)
				.stream()
				.filter(msg -> msg.getContentDisplay().length() > 10)
				.sorted(Comparator.comparing(Message::getTimeCreated))
				.map(msg -> toChatGptMessage(msg, botId))
				.forEach(gptMessages::add);
		gptMessages.add(toChatGptMessage(current, botId));

		String response = sendRequest(getChatCompletion(gptMessages));
		log.log(Level.INFO, "Raw ChatGpt response=" + response);
		return response;
	}

	private List<Message> getIncludedMessages(MessageHistory history, int promptTokens) {
		List<Message> historyDesc = history.getRetrievedHistory().stream()
				.sorted(Comparator.comparing(Message::getTimeCreated).reversed())
				.toList();

		List<Message> messagesToInclude = new ArrayList<>();
		for (Message message : historyDesc) {
			int tokens = getExpectedTokens(message);
			if (addingExceedsLimit(promptTokens, tokens)) {
				break;
			}

			promptTokens += tokens;
			messagesToInclude.add(message);
		}

		return messagesToInclude;
	}

	private String sendRequest(ChatCompletion chatCompletion) {
		return chatGPT.chatCompletion(chatCompletion)
				.getChoices()
				.get(0)
				.getMessage()
				.getContent();
	}

	private ChatCompletion getChatCompletion(List<com.plexpt.chatgpt.entity.chat.Message> messages) {
		return ChatCompletion.builder()
				.model(gptConfig.model().getName())
				.messages(messages)
				.maxTokens(gptConfig.maxTokensResponse())
				.temperature(gptConfig.temperature())
				.build();
	}

	private com.plexpt.chatgpt.entity.chat.Message toChatGptMessage(Message message, String botId) {
		if (MessageUtils.isAuthorBot(message, botId)) {
			return com.plexpt.chatgpt.entity.chat.Message.ofAssistant(message.getContentRaw());
		}

		String content = message.getAuthor().getId() + ": " + message.getContentRaw();
		return com.plexpt.chatgpt.entity.chat.Message.of(content);
	}

	private boolean addingExceedsLimit(int currentTokens, int newTokens) {
		return newTokens + currentTokens + gptConfig.maxTokensResponse() + gptConfig.systemMsgTokens() >= gptConfig.maxTokens();
	}

	private int getExpectedTokens(Message message) {
		return (message.getContentDisplay().length() + message.getAuthor().getEffectiveName().length()) / 3;
	}
}
