package com.devformed.sylphiette.service.listener;

import com.devformed.sylphiette.GlobalConstants;
import com.devformed.sylphiette.service.command.CommandHandler;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Anton Gorokh
 */
@Log
@Component
public class SlashCommandsListener extends ListenerAdapter {

	private final Map<String, CommandHandler> commandHandlers;

	@Autowired
	public SlashCommandsListener(Set<CommandHandler> commandHandlers) {
		this.commandHandlers = commandHandlers.stream()
				.collect(HashMap::new, (m, v) -> m.put(v.supportedCommand(), v), HashMap::putAll);
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		Optional.ofNullable(commandHandlers.get(event.getName()))
				.or(this::getDefaultHandler)
				.ifPresentOrElse(handler -> handler.handleCommand(event, GlobalConstants.DEFAULT_LOCALE), this::logHandlerMiss);
	}

	private Optional<CommandHandler> getDefaultHandler() {
		return Optional.ofNullable(commandHandlers.get(null));
	}

	private void logHandlerMiss() {
		log.log(Level.WARNING, "No default command handler provided");
	}
}
