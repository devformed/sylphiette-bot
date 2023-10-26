package com.devformed.sylphiette.service.listener;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.i18n.I18n;
import com.devformed.sylphiette.util.UserUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SlashCommandsListener extends ListenerAdapter {

	private final BotConfig botConfig;

	@Autowired
	public SlashCommandsListener(BotConfig botConfig) {
		this.botConfig = botConfig;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		String command = event.getName();
		Locale locale = Locale.forLanguageTag(botConfig.defaultLocale());

		if ("author".equals(command)) {
			handleAuthorCommand(event, locale);
		}
	}

	private void handleAuthorCommand(SlashCommandInteractionEvent event, Locale locale) {
		event.reply(I18n.translate("MESSAGE.ANSWER.AUTHOR_DESC", locale) + " " + UserUtils.ping(botConfig.authorId())).queue();
	}
}
