package com.devformed.sylphiette.service;

import com.devformed.sylphiette.config.ChatGptConfig;
import com.devformed.sylphiette.dto.MessageDto;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.Message;
import lombok.extern.java.Log;
import org.apache.commons.collections4.list.TreeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log
@Component
public class ChatGptResponder {

	private static final Pattern GPT_RESPONSE_PATTERN = Pattern.compile("<?@?\\w+>?:(.+)");

	private final ChatGptConfig gptConfig;
	private final ChatGPT chatGPT;

	@Autowired
	public ChatGptResponder(ChatGptConfig gptConfig) {
		this.gptConfig = gptConfig;
		this.chatGPT = ChatGPT.builder()
				.apiKey(gptConfig.key())
				.build().init();
	}

	public String askSylphiette(List<MessageDto> history, MessageDto prompt) {
		String rawResponse = getResponse(history, prompt);
		log.log(Level.INFO, "raw ChatGpt response=" + rawResponse);

		Matcher matcher = GPT_RESPONSE_PATTERN.matcher(rawResponse);
		if (matcher.find()) return matcher.group(1);
		return rawResponse;
	}

	private String getResponse(List<MessageDto> history, MessageDto prompt) {
		Message promptMessage = toMessage(prompt);
		int initTokens = gptConfig.promptTokens() + tokenize(promptMessage);

		List<Message> messages = toMessages(history, initTokens);
		messages.add(Message.ofSystem(gptConfig.promptSylphiette()));
		messages.add(promptMessage);
		return sendRequest(getChatCompletion(messages));
	}

	private List<Message> toMessages(List<MessageDto> historyMessages, int currentTokens) {
		TreeList<MessageDto> selectedHistory = new TreeList<>();
		for (MessageDto historyMsg : historyMessages) {
			Message msg = toMessage(historyMsg);
			int newTokens = tokenize(msg);

			if (messageExceedsLimit(currentTokens, newTokens)) {
				break;
			}

			currentTokens += newTokens;
			selectedHistory.add(historyMsg);
		}

		return selectedHistory.stream()
				.map(this::toMessage)
				.collect(Collectors.toList());
	}

	private Message toMessage(MessageDto prompt) {
		return Message.ofSystem("<@" + prompt.author() + ">: " + prompt.content());
	}

	private int tokenize(Message message) {
		return message.getContent().length() / 3;
	}

	private boolean messageExceedsLimit(int currentTokens, int msgTokens) {
		return gptConfig.maxTokens() < gptConfig.maxTokensResponse() + currentTokens + msgTokens;
	}

	private String sendRequest(ChatCompletion chatCompletion) {
		return chatGPT.chatCompletion(chatCompletion)
				.getChoices()
				.get(0)
				.getMessage()
				.getContent();
	}

	private ChatCompletion getChatCompletion(List<Message> messages) {
		return ChatCompletion.builder()
				.model(gptConfig.model().getName())
				.messages(messages)
				.maxTokens(gptConfig.maxTokensResponse())
				.temperature(gptConfig.temperature())
				.build();
	}
}
