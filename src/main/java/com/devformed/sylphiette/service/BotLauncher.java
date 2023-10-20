package com.devformed.sylphiette.service;

import com.devformed.sylphiette.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Anton Gorokh
 */
@Component
public final class BotLauncher implements CommandLineRunner {

	private final BotConfig config;
	private final EventListener[] eventListeners;

	@Autowired
	public BotLauncher(BotConfig config, EventListener[] eventListeners) {
		this.config = config;
		this.eventListeners = eventListeners;
	}

	@Override
	public void run(String... args) {
		JDA jda = JDABuilder.createLight(config.token(), BotConfig.INTENTS)
				.addEventListeners((Object[]) eventListeners)
				.build();
	}
}
