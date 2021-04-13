package com.brchain.core.fabric.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.brchain.core.channel.dto.ChannelInfoDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

	private long           id;
	private String         txId;           // 트랜잭션 아이디
	private String         creatorId;      // 트랜잭션 생성자 아이디
	private String         txType;         // 트랜잭션 타입
	private Date           timestamp;      // 트랜잭션 타임스테프
	private String         ccName;         // 체인코드 이름
	private String         ccVersion;      // 체인코드 버전
	private String         ccArgs;         // 체인코드 파라미터
	private BlockDto       blockDto;       // 블록정보 DTO
	private ChannelInfoDto channelInfoDto; // 채널정보 DTO
	private LocalDateTime  createdAt;      // 생성 시간

}
