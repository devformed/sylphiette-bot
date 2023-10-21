package com.devformed.sylphiette.service;

import com.devformed.sylphiette.config.ChatGptConfig;
import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ChatGptResponder {

	private final ChatGptConfig gptConfig;
	private final ChatGPT chatGPT;
	private final Message systemMsg;

	@Autowired
	public ChatGptResponder(ChatGptConfig gptConfig) {
		this.gptConfig = gptConfig;
		this.chatGPT = ChatGPT.builder()
				.apiKey(gptConfig.key())
				.build().init();
		this.systemMsg = Message.ofSystem(gptConfig.systemMsg());

	}

	public String askBot(User user, String question) {
		String prompt = "[" + user.getName() + "]: " + question;
		return chatGPT.chatCompletion(getChatCompletion(prompt))
				.getChoices()
				.get(0)
				.getMessage()
				.getContent();
	}

	private ChatCompletion getChatCompletion(String prompt) {
		Message userMsg = Message.of(prompt);
		return ChatCompletion.builder()
				.model(gptConfig.model().getName())
				.messages(Arrays.asList(systemMsg, userMsg))
				.maxTokens(gptConfig.maxTokens())
				.temperature(gptConfig.temperature())
				.build();
	}
}
