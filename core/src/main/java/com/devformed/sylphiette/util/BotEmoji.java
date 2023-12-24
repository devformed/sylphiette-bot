package com.devformed.sylphiette.util;

import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

/**
 * @author Anton Gorokh
 */
public enum BotEmoji {
	HEART("U+2764");

	@Getter
	private final Emoji emoji;

	BotEmoji(String unicode) {
		this.emoji = Emoji.fromUnicode(unicode);
	}
}
