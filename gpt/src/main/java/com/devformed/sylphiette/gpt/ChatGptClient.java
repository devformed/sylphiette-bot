package com.devformed.sylphiette.gpt;

import cn.hutool.core.collection.ListUtil;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletion.Model;
import com.plexpt.chatgpt.entity.chat.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Anton Gorokh
 */
@Component
public final class ChatGptClient {

	private final String apiKey;
	private final String model;
	private final int maxRequest;
	private final int maxResponse;

	private ChatGPT chatGPT;

	@Autowired
	public ChatGptClient(@Value("${chat-gpt.key}") String apiKey,
						 @Value("${chat-gpt.model}") String model,
						 @Value("${chat-gpt.tokens.max-request}") int maxRequest,
						 @Value("${chat-gpt.tokens.max-response}") int maxResponse
	) {
		this.apiKey = apiKey;
		this.model = model;
		this.maxRequest = maxRequest;
		this.maxResponse = maxResponse;
	}

	@PostConstruct
	private void init() {
		chatGPT = ChatGPT.builder()
				.apiKey(this.apiKey)
				.build()
				.init();
	}

	public String send(List<Message> messages) {
		ChatCompletion.Model gptModel = getTargetModel(model);
		List<Message> truncatedMessages = truncateToFitTokens(messages);
		return sendRequest(getChatCompletion(truncatedMessages, gptModel, maxRequest));
	}

	private List<Message> truncateToFitTokens(List<Message> messages) {
		int maxTokens = maxRequest - maxResponse;
		int currentTokens = 0;

		var messagesIterator = messages.reversed().iterator();
		List<Message> truncatedMessages = new ArrayList<>();

		do {
			Message message = messagesIterator.next();
			String msg = message.getName() + message.getContent();
			int msgTokens = getApproximateTokensCount(msg);

			if (currentTokens + msgTokens > maxTokens) break;
			truncatedMessages.add(message);

		} while (messagesIterator.hasNext());
		return truncatedMessages.reversed();
	}

	private String sendRequest(ChatCompletion chatCompletion) {
		return chatGPT.chatCompletion(chatCompletion)
				.getChoices()
				.get(0)
				.getMessage()
				.getContent();
	}

	private ChatCompletion getChatCompletion(List<Message> messages, Model model, int maxTokens) {
		return ChatCompletion.builder()
				.model(model.getName())
				.messages(messages)
				.maxTokens(maxTokens)
				.build();
	}

	private ChatCompletion.Model getTargetModel(String modelName) {
		return Arrays.stream(Model.values())
				.filter(e -> e.getName().equals(modelName))
				.findAny()
				.orElse(Model.GPT_3_5_TURBO);
	}

	private int getApproximateTokensCount(String str) {
		return str == null ? 0 : str.length() / 3;
	}
}
