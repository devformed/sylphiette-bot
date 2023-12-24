package com.devformed.sylphiette.service.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Locale;

public interface CommandHandler {
	void handleCommand(SlashCommandInteractionEvent event, Locale locale);
	String supportedCommand();
}
