package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHandleDto {

	private String        handle;      // 이벤트 핸들러
	private String        channelName; // 채널 정보
	private LocalDateTime createdAt;   // 생성 시간

}
