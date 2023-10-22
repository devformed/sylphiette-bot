package com.devformed.sylphiette.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

/**
 * @author Anton Gorokh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtils {

	public static boolean isAuthorBot(Message message, String botId) {
		return botId.equals(message.getAuthor().getId());
	}
}
