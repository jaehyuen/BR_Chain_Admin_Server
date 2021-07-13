package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채널  정보 dto")
public class ChannelInfoDto {

	@Schema(example = "test-channel", description = "채널 이름")
	private String channelName; // 채널 이름

	@Schema(example = "10", description = "채널 블록 개수")
	private long channelBlock; // 채널 블럭수

	@Schema(example = "20", description = "채널 트렌젝션 개수")
	private long channelTx; // 채널 트렌젝션수

	@Schema(example = "testorderer", description = "채널을 운영중인 오더러 조직")
	private String orderingOrg; // 운영중인 오더러 조직

	// 정책 관련
	@Schema(example = "ImplicitMeta", description = "Application 어드민 정책 타입")
	private String appAdminPolicyType; // Application 어드민 정책 타입

	@Schema(example = "ANY Admins", description = "Application 어드민 정책 값")
	private String appAdminPolicyValue; // Application 어드민 정책 벨류

	@Schema(example = "ImplicitMeta", description = "Orderer 어드민 정책 타입")
	private String ordererAdminPolicyType; // Orderer 어드민 정책 타입

	@Schema(example = "ANY Admins", description = "Orderer 어드민 정책 값")
	private String ordererAdminPolicyValue; // Orderer 어드민 정책 벨류

	@Schema(example = "ImplicitMeta", description = "Channel 어드민 정책 타입")
	private String channelAdminPolicyType; // Channel 어드민 정책 타입

	@Schema(example = "ANY Admins", description = "Channel 어드민 정책 값")
	private String channelAdminPolicyValue; // Channel 어드민 정책 벨류

	// 블록 설정 관련
	@Schema(example = "1s", description = "batchTimeout 옵션")
	private String batchTimeout; // batchTimeout 옵션

	@Schema(example = "81920", description = "batchSizeAbsolMax 옵션")
	private long batchSizeAbsolMax; // batchSizeAbsolMax 옵션

	@Schema(example = "20", description = "batchSizeMaxMsg 옵션")
	private long batchSizeMaxMsg; // batchSizeMaxMsg 옵션

	@Schema(example = "20480", description = "batchSizePreferMax 옵션")
	private long batchSizePreferMax; // batchSizePreferMax 옵션

	private LocalDateTime createdAt; // 생성 시간

}
