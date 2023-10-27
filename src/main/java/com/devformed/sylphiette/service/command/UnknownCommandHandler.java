package com.devformed.sylphiette.service.command;

import com.devformed.sylphiette.i18n.I18n;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UnknownCommandHandler implements CommandHandler {

	@Override
	public String supportedCommand() {
		return null;
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event, Locale locale) {
		event.reply(I18n.translate("MESSAGE.ANSWER.UNKNOWN_COMMAND", locale)).queue();
	}
}
