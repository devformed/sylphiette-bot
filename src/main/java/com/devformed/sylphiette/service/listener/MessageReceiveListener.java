package com.devformed.sylphiette.service.listener;

import com.devformed.sylphiette.config.BotConfig;
import com.devformed.sylphiette.service.ChatGptResponder;
import com.devformed.sylphiette.util.UserUtils;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;

/**
 * @author Anton Gorokh
 */
@Log
@Service
public class MessageReceiveListener extends ListenerAdapter {

	private final ChatGptResponder chatGptResponder;
	private final BotConfig botConfig;

	@Autowired
	public MessageReceiveListener(ChatGptResponder chatGptResponder, BotConfig botConfig) {
		this.chatGptResponder = chatGptResponder;
		this.botConfig = botConfig;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		Message message = event.getMessage();
		MessageChannelUnion channel = message.getChannel();
		User author = message.getAuthor();
		String content = message.getContentDisplay();

		try {
			processMessage(channel, author, content);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to process message=" + content);
			channel.sendMessage("Sorry, I'm really busy rn ^^").queue();
		}
	}

	private void processMessage(MessageChannelUnion channel, User author, String content) throws Exception {
		if (isNotCommand(content)) {
			if (content.contains("@" + botConfig.name())) {
				String response = chatGptResponder.askBot(author, content);
				channel.sendMessage(response).queue();
			}
		} else {
			String command = getCommand(content);
			String response = switch (command) {
				case "!author" -> "Created by " + UserUtils.ping(botConfig.authorId());
				default -> "Unknown command, available commands: !author";
			};
			channel.sendMessage(response).queue();
		}
	}

	private String getCommand(String content) {
		int spaceIndex = content.indexOf(" ");
		var command = spaceIndex != -1 ? content.substring(0, spaceIndex) : content;
		return command.toLowerCase();
	}

	private boolean isNotCommand(String content) {
		return '!' != content.charAt(0);
	}
}
