package com.devformed.sylphiette.service;

import com.devformed.sylphiette.gpt.ChatGptClient;
import com.devformed.sylphiette.i18n.I18n;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Anton Gorokh
 */
@Service
public class ChatGptClientService {

	private final String botName;
	private final String promptSylphiette;
	private final String promptUnlimited;

	private final ChatGptClient chatGptClient;
	private final ExecutorService executorService;
	private final Random random;

	@Autowired
	public ChatGptClientService(@Value("${bot.name}") String botName, @Value("${chat-gpt.prompt.sylphiette}") String promptSylphiette,
								@Value("${chat-gpt.prompt.unlimited}") String promptUnlimited, ChatGptClient chatGptClient) {
		this.botName = botName;
		this.promptSylphiette = promptSylphiette;
		this.promptUnlimited = promptUnlimited;
		this.chatGptClient = chatGptClient;
		this.executorService = Executors.newVirtualThreadPerTaskExecutor();
		this.random = new Random();
	}

	public void processSylphyRequest(Message message, Locale locale) {
		sendRandomFiller(message, locale);
		message.getChannel()
				.getHistoryBefore(message, 25)
				.map(MessageHistory::getRetrievedHistory)
				.queue(history -> processSylphyRequest(history, message));
	}

	public void processSylphyRequest(List<Message> history, Message message) {
		var gptMessages = new ArrayList<com.plexpt.chatgpt.entity.chat.Message>();
		for (Message historyMsg : history) {
			gptMessages.add(toGptMessage(historyMsg.getAuthor().getEffectiveName(), historyMsg.getContentDisplay(), false));
		}

		gptMessages.add(toGptMessage(null, promptSylphiette, true));
		gptMessages.add(toGptMessage(message.getAuthor().getEffectiveName(), message.getContentDisplay(), false));
		executorService.submit(() -> sendToGptAndReply(gptMessages, message));
	}

	public void processUnlimitedRequest(Message message, Locale locale) {
		sendRandomFiller(message, locale);
		String content = message.getContentDisplay();

		String prompt = promptUnlimited
				.replace("${{messageText}}", content);
		var gptMsg = toGptMessage(null, prompt, true);
		executorService.submit(() -> sendToGptAndReply(List.of(gptMsg), message));
	}

	private void sendRandomFiller(Message message, Locale locale) {
		message.getChannel().sendMessage(getRandomFiller(locale)).queue();
	}

	private String getRandomFiller(Locale locale) {
		String[] fillers = I18n.translate("MESSAGE.ANSWER.FILLER_WORDS", locale).split("\n");
		return fillers[random.nextInt(fillers.length)];
	}

	private void sendToGptAndReply(List<com.plexpt.chatgpt.entity.chat.Message> messages, Message replyTo) {
		replyTo.reply(chatGptClient.send(messages)).queue();
	}

	private com.plexpt.chatgpt.entity.chat.Message toGptMessage(String author, String content, boolean system) {
		if (system) return com.plexpt.chatgpt.entity.chat.Message.ofSystem(content);
		return com.plexpt.chatgpt.entity.chat.Message.builder()
				.role(botName.equals(author) ? "assistant" : "user")
				.name(author)
				.content(content)
				.build();
	}
}
