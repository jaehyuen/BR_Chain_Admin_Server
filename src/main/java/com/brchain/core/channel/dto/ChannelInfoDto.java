package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

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
public class ChannelInfoDto {

	private String channelName; // 채널 이름
	private int channelBlock; // 채널 블럭수
	private int channelTx; // 채널 트렌젝션수
	private String orderingOrg; // 운영중인 오더러 조직

	// 정책 관련
	private String appAdminPolicyType; // Application 어드민 정책 타입
	private String appAdminPolicyValue; // Application 어드민 정책 벨류
	private String ordererAdminPolicyType; // Orderer 어드민 정책 타입
	private String ordererAdminPolicyValue; // Orderer 어드민 정책 벨류
	private String channelAdminPolicyType; // Channel 어드민 정책 타입
	private String channelAdminPolicyValue; // Channel 어드민 정책 벨류

	// 블록 설정 관련
	private String batchTimeout; // batchTimeout 옵션
	private long batchSizeAbsolMax;// batchSizeAbsolMax 옵션
	private long batchSizeMaxMsg;// batchSizeMaxMsg 옵션
	private long batchSizePreferMax;// batchSizePreferMax 옵션

	private LocalDateTime createdAt;// 생성 시간



}
