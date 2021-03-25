package com.brchain.core.chaincode.dto;

import java.time.LocalDateTime;

import com.brchain.core.channel.dto.ChannelInfoDto;

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
public class CcInfoChannelDto {

	private Long id; // id
	private String ccVersion; // 체인코드 버전
	private ChannelInfoDto channelInfoDto; // 채널 정보
	private CcInfoDto ccInfoDto; // 체인코드 DTO
	private LocalDateTime createdAt; // 생성 시간

}
