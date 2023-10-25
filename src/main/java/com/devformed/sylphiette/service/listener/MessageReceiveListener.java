package com.devformed.sylphiette.service.listener;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.dto.MessageDto;
import com.devformed.sylphiette.i18n.I18n;
import com.devformed.sylphiette.service.ChatGptResponder;
import com.devformed.sylphiette.util.MessageUtils;
import com.devformed.sylphiette.util.UserUtils;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;

/**
 * @author Anton Gorokh
 */
@Log
@Component
public class MessageReceiveListener extends ListenerAdapter {

	private final ChatGptResponder chatGptResponder;
	private final BotConfig botConfig;
	private final Random random;

	@Autowired
	public MessageReceiveListener(ChatGptResponder chatGptResponder, BotConfig botConfig) {
		this.chatGptResponder = chatGptResponder;
		this.botConfig = botConfig;
		random = new Random();
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
		MessageChannelUnion channel = message.getChannel();

		if (isNotCommand(content)) {
			if (isBotMentioned(content)) {
				processGptRequest(message, locale);
			}
		} else {
			String command = getCommand(content);
			String response = switch (command) {
				case "!author" -> I18n.translate("MESSAGE.ANSWER.AUTHOR_DESC", locale) + " " + UserUtils.ping(botConfig.authorId());
				default -> I18n.translate("MESSAGE.ANSWER.UNKNOWN_COMMAND", locale);
			};
			channel.sendMessage(response).queue();
		}
	}

	private void processGptRequest(Message message, Locale locale) {
		MessageChannelUnion channel = message.getChannel();
		channel.sendMessage(getRandomFiller(locale)).queue();

		List<Message> history = channel.getHistoryBefore(message, 50)
				.complete()
				.getRetrievedHistory();
		List<MessageDto> historyDto = MessageUtils.toDto(history);
		MessageDto messageDto = MessageUtils.toDto(message);

		channel.sendMessage(chatGptResponder.askSylphiette(historyDto, messageDto)).queue();
	}

	private String getCommand(String content) {
		int spaceIndex = content.indexOf(" ");
		var command = spaceIndex != -1 ? content.substring(0, spaceIndex) : content;
		return command.toLowerCase();
	}

	private String getRandomFiller(Locale locale) {
		String[] fillers = I18n.translate("MESSAGE.ANSWER.FILLER_WORDS", locale).split("\n");
		return fillers[random.nextInt(fillers.length)];
	}

	private boolean isNotCommand(String content) {
		return '!' != content.charAt(0);
	}

	private boolean isBot(User author) {
		return botConfig.id().equals(author.getId());
	}

	private boolean isBotMentioned(String content) {
		return content.contains("@" + botConfig.name());
	}
}
