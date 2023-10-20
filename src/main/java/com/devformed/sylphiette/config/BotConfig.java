package com.devformed.sylphiette.config;

import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Anton Gorokh
 */
@ConfigurationProperties("bot")
public record BotConfig(
		String name,
		String token,
		String authorId
) {

	public static final List<GatewayIntent> INTENTS = ImmutableList.of(
			GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS
	);
}
