package com.devformed.sylphiette.service.command;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.i18n.I18n;
import com.devformed.sylphiette.util.UserUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AuthorCommandHandler implements CommandHandler {

	private final BotConfig botConfig;

	@Autowired
	public AuthorCommandHandler(BotConfig botConfig) {
		this.botConfig = botConfig;
	}

	@Override
	public String supportedCommand() {
		return "author";
	}

	@Override
	public void handleCommand(SlashCommandInteractionEvent event, Locale locale) {
		event.reply(I18n.translate("MESSAGE.ANSWER.AUTHOR_DESC", locale) + " " + UserUtils.ping(botConfig.authorId())).queue();
	}
}
