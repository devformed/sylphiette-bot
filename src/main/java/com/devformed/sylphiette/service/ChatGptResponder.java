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

	private final ChatGPT chatGPT;
	private final ChatCompletion.Model model;
	private final Message systemMsg;

	@Autowired
	public ChatGptResponder(ChatGptConfig gptConfig) {
		this.chatGPT = ChatGPT.builder()
				.apiKey(gptConfig.key())
				.build().init();
		this.systemMsg = Message.ofSystem(gptConfig.systemMsg());
		this.model = gptConfig.model();

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
				.model(model.getName())
				.messages(Arrays.asList(systemMsg, userMsg))
				.maxTokens(3000)
				.temperature(0.9)
				.build();
	}
}
