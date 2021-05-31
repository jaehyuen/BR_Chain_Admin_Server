package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

import com.brchain.core.container.dto.ConInfoDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelInfoPeerDto {

	@Schema(example = "1", description = "채널에 가입된 피어 시퀀스")
	private Long id; // id

	@Schema(example = "true", description = "앵커피어 여부")
	private boolean anchorYn; // 앵커피어 여부
	private ConInfoDto conInfoDto; // 컨테이너 정보
	private ChannelInfoDto channelInfoDto;// 채널 정보
	private LocalDateTime createdAt; // 생성 시간

}
