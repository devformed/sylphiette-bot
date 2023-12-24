package com.devformed.sylphiette;

import com.devformed.sylphiette.config.BotConfig;
import com.google.common.collect.ImmutableList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Anton Gorokh
 */
@Service
public final class BotLauncher implements CommandLineRunner {

	private static final List<CommandData> COMMANDS = ImmutableList.<CommandData>builder()
			.add(Commands.slash("author", "Provides information about bot creator"))
			.build();

	private final BotConfig config;
	private final EventListener[] eventListeners;

	@Autowired
	public BotLauncher(BotConfig config, EventListener[] eventListeners) {
		this.config = config;
		this.eventListeners = eventListeners;
	}

	@Override
	public void run(String... args) throws InterruptedException {
		JDA jda = JDABuilder.createDefault(config.token(), BotConfig.INTENTS)
				.addEventListeners((Object[]) eventListeners)
				.build()
				.awaitReady();

		updateCommands(jda);
	}

	private void updateCommands(JDA jda) {
		jda.updateCommands().addCommands(COMMANDS).queue();
	}
}
