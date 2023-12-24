package com.devformed.sylphiette.i18n;

import com.google.common.io.Resources;
import lombok.extern.java.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class I18n {

	public static final Set<Locale> SUPPORTED_LOCALES = Set.of(toLocale("en"), toLocale("ru"));
	private static final Map<Locale, ResourceBundle> BUNDLE_BY_LOCALE;

	static {
		BUNDLE_BY_LOCALE = SUPPORTED_LOCALES.stream()
				.collect(Collectors.toMap(Function.identity(), I18n::getBundle));
	}

	public static String translate(String key, Map<String, String> placeHolders, Locale locale) {
		return Optional.ofNullable(BUNDLE_BY_LOCALE.get(locale))
				.filter(bundle -> bundle.containsKey(key))
				.map(bundle -> bundle.getString(key))
				.map(translation -> applyPlaceholders(translation, placeHolders))
				.orElse("!" + key + "!");
	}

	public static String translate(String key, Locale locale) {
		return translate(key, Collections.emptyMap(), locale);
	}

	private static String applyPlaceholders(String translation, Map<String, String> placeHolders) {
		for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
			translation = translation.replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return translation;
	}

	private static ResourceBundle getBundle(Locale locale) {
		try {
			URL resource = Resources.getResource("i18n/translations_" + locale.getLanguage() + ".properties");
			ByteArrayInputStream resourceIS = new ByteArrayInputStream(Resources.toByteArray(resource));
			return new PropertyResourceBundle(resourceIS);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to load bundle for locale $locale", e);
			return null;
		}
	}

	private static Locale toLocale(String languageTag) {
		return Locale.forLanguageTag(languageTag);
	}
}
