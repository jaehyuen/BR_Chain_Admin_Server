package com.brchain.core.chaincode.dto;

import java.time.LocalDateTime;

import com.brchain.core.channel.dto.ChannelInfoDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채널에 활성화된 체인코드  정보 dto")
public class CcInfoChannelDto {

	@Schema(example = "1", description = "체인코드 정보 (채널) 시퀀스")
	private Long id; // id

	@Schema(example = "1", description = "활성화된 체인코드 버전")
	private String ccVersion; // 체인코드 버전

	private ChannelInfoDto channelInfoDto; // 채널 정보
	private CcInfoDto ccInfoDto; // 체인코드 DTO
	private LocalDateTime createdAt; // 생성 시간

}
