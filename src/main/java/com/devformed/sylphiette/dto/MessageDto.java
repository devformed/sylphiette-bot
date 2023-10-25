package com.devformed.sylphiette.dto;

import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public record MessageDto(
		OffsetDateTime creationTime,
		String content,
		String author
) implements Comparable<MessageDto> {

	@Override
	public int compareTo(@NotNull MessageDto o) {
		return creationTime.compareTo(o.creationTime);
	}
}
