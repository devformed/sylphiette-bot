package com.devformed.sylphiette.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserUtils {

	public static String ping(String authorId) {
		return "<@" + authorId + ">";
	}

	public static String ping(User author) {
		return ping(author.getId());
	}
}
