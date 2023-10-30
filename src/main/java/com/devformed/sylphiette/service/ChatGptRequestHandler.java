package com.devformed.sylphiette.service;

import com.devformed.sylphiette.dto.MessageDto;
import com.devformed.sylphiette.i18n.I18n;
import com.devformed.sylphiette.util.MessageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Anton Gorokh
 */
@Component
public class ChatGptRequestHandler {

	private final ChatGptResponder chatGptResponder;
	private final ExecutorService executorService;
	private final Random random;

	@Autowired
	public ChatGptRequestHandler(ChatGptResponder chatGptResponder) {
		this.chatGptResponder = chatGptResponder;
		this.executorService = Executors.newCachedThreadPool();
		this.random = new Random();
	}

	public void handleGptRequest(Message message, Locale locale) {
		MessageChannelUnion channel = message.getChannel();
		channel.sendMessage(getRandomFiller(locale)).queue();
		executorService.submit(() -> processGptRequest(message, channel));
	}

	private void processGptRequest(Message message, MessageChannelUnion channel) {
		List<MessageDto> historyDto = channel.getHistoryBefore(message, 50)
				.complete()
				.getRetrievedHistory()
				.stream()
				.map(MessageUtils::toDto)
				.toList();
		MessageDto messageDto = MessageUtils.toDto(message);
		message.reply(chatGptResponder.askSylphiette(historyDto, messageDto)).queue();
	}

	private String getRandomFiller(Locale locale) {
		String[] fillers = I18n.translate("MESSAGE.ANSWER.FILLER_WORDS", locale).split("\n");
		return fillers[random.nextInt(fillers.length)];
	}
}
