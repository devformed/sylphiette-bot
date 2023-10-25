package com.devformed.sylphiette.util;

import com.devformed.sylphiette.dto.MessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Anton Gorokh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtils {

	public static boolean isAuthorBot(Message message, String botId) {
		return botId.equals(message.getAuthor().getId());
	}

	public static MessageDto toDto(Message message) {
		return new MessageDto(message.getTimeCreated(), message.getContentRaw(), message.getAuthor().getId());
	}

	public static List<MessageDto> toDto(Collection<Message> messages) {
		return messages.stream()
				.map(MessageUtils::toDto)
				.collect(Collectors.toList());
	}
}
