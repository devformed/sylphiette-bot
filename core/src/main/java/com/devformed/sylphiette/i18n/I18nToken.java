package com.devformed.sylphiette.i18n;

import lombok.Builder;
import lombok.Singular;

import java.util.Locale;
import java.util.Map;

@Builder
public record I18nToken(
		String key,
		@Singular Map<String, String> placeHolders
) {
	public String translate(Locale locale) {
		return I18n.translate(key, placeHolders, locale);
	}
}
