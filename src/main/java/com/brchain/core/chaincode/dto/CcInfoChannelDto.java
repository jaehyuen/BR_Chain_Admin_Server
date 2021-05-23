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
public class CcInfoChannelDto {

	@Schema(example="1")
	private Long           id;             // id
	@Schema(example="1")
	private String         ccVersion;      // 체인코드 버전
	private ChannelInfoDto channelInfoDto; // 채널 정보
	private CcInfoDto      ccInfoDto;      // 체인코드 DTO
	private LocalDateTime  createdAt;      // 생성 시간

}
