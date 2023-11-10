package com.devformed.sylphiette;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Anton Gorokh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalConstants {

	public static final String DEFAULT_LOCALE_TAG = "en";
	public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag(DEFAULT_LOCALE_TAG);
	public static final Pattern DISCROD_ID_PATTERN = Pattern.compile("(\\d{17,19})");
}
