package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

import com.brchain.core.container.dto.ConInfoDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelInfoPeerDto {

	private Long id;
	private boolean anchorYn; // 앵커피어 여부
	private ConInfoDto conInfoDto; // 컨테이너 정보
	private ChannelInfoDto channelInfoDto;// 채널 정보
	private LocalDateTime createdAt;// 생성 시간

}
