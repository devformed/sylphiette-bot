package com.devformed.sylphiette.config;

import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("chat-gpt")
public record ChatGptConfig(
		String url,
		String key,
		Double temperature,
		ChatCompletion.Model model,

		Integer maxTokensResponse,
		Integer maxTokens,

		Integer promptTokens,
		String promptSylphiette
) {
}
