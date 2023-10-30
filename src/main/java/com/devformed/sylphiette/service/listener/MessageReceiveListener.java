package com.devformed.sylphiette.service.listener;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.i18n.I18n;
import com.devformed.sylphiette.service.ChatGptRequestHandler;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.logging.Level;

/**
 * @author Anton Gorokh
 */
@Log
@Component
public class MessageReceiveListener extends ListenerAdapter {

	private final ChatGptRequestHandler chatGptRequestHandler;
	private final BotConfig botConfig;

	@Autowired
	public MessageReceiveListener(ChatGptRequestHandler chatGptRequestHandler, BotConfig botConfig) {
		this.chatGptRequestHandler = chatGptRequestHandler;
		this.botConfig = botConfig;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentDisplay();
		if (isBot(message.getAuthor())) {
			return;
		}

		MessageChannelUnion channel = message.getChannel();
		Locale locale = Locale.forLanguageTag(botConfig.defaultLocale());

		try {
			processMessage(message, locale);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to process message=" + content);
			if (content.contains("@" + botConfig.name())) {
				channel.sendMessage(I18n.translate("MESSAGE.ANSWER.ERROR", locale)).queue();
			}
		}
	}

	private void processMessage(Message message, Locale locale) {
		String content = message.getContentDisplay();
		if (isBotMentioned(content))
			chatGptRequestHandler.handleGptRequest(message, locale);
	}

	private boolean isBot(User author) {
		return botConfig.id().equals(author.getId());
	}

	private boolean isBotMentioned(String content) {
		return content.contains("@" + botConfig.name());
	}
}
