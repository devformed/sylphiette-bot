package com.devformed.sylphiette.config;

import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("chat-gpt")
public record ChatGptConfig(
		String url,
		String key,
		String systemMsg,
		ChatCompletion.Model model
) {
}
