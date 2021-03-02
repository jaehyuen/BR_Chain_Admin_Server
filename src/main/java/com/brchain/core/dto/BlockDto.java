package com.brchain.core.dto;

import java.time.LocalDateTime;

import com.brchain.core.dto.channel.ChannelInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {

	private String blockDataHash; // 블록 데이터 해쉬
	private int blockNum; // 블록 번호
	private int txCount; // 트렌젝션 개수
	private String prevDataHash;// 이전블록 데이터 해쉬
	private ChannelInfoDto channelInfoDto; // 채널 정보 DTO
	private LocalDateTime createdAt; // 생성 시

}
