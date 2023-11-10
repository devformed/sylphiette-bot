package com.devformed.sylphiette.service;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.config.ChatGptConfig;
import com.devformed.sylphiette.dto.MessageDto;
import com.devformed.sylphiette.util.UserUtils;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.Message;
import org.apache.commons.collections4.list.TreeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Anton Gorokh
 */
@Component
public class ChatGptResponder {

	private final ChatGptConfig gptConfig;
	private final BotConfig botConfig;
	private final ChatGPT chatGPT;

	@Autowired
	public ChatGptResponder(ChatGptConfig gptConfig, BotConfig botConfig) {
		this.gptConfig = gptConfig;
		this.botConfig = botConfig;
		this.chatGPT = initGPT(gptConfig.key());
	}

	public String askSylphiette(List<MessageDto> history, MessageDto prompt) {
		return getResponse(history, prompt)
				.replaceAll("^[\\w@<>]+:", "");
	}

	private static ChatGPT initGPT(String apiKey) {
		return ChatGPT
				.builder()
				.apiKey(apiKey)
				.build()
				.init();
	}

	private String getResponse(List<MessageDto> history, MessageDto prompt) {
		Message promptMessage = toMessage(prompt);
		int initTokens = gptConfig.tokensPromptSylphiette() + tokenize(promptMessage);

		List<Message> messages = toMessages(history, initTokens);
		messages.add(Message.ofSystem(gptConfig.promptSylphiette()));
		messages.add(promptMessage);
		return sendRequest(getChatCompletion(messages));
	}

	private List<Message> toMessages(List<MessageDto> historyMessages, int currentTokens) {
		TreeList<MessageDto> selectedHistory = new TreeList<>();
		for (MessageDto historyMsg : historyMessages) {
			if (historyMsg.content().length() <= 10) {
				continue;
			}

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
		String content = UserUtils.ping(prompt.author()) + ": " + prompt.content();
		return prompt.author().contains(botConfig.id()) ? Message.ofAssistant(content) : Message.of(content);
	}

	private int tokenize(Message message) {
		return message.getContent().length() / 3;
	}

	private boolean messageExceedsLimit(int currentTokens, int msgTokens) {
		return gptConfig.tokensMaxResponse() + currentTokens + msgTokens >= gptConfig.tokensMaxRequest();
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
				.maxTokens(gptConfig.tokensMaxResponse())
				.temperature(gptConfig.temperature())
				.build();
	}
}
