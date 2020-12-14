package com.brchain.core.dto.channel;

import java.time.LocalDateTime;

import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity.ChannelInfoEntityBuilder;

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
	private String batchTimeout;
	private long batchSizeAbsolMax;
	private long batchSizeMaxMsg;
	private long batchSizePreferMax;

	private LocalDateTime createdAt;

//	public ChannelInfoEntity toEntity() {
//
//		ChannelInfoEntityBuilder channelInfoEntityBuilder = ChannelInfoEntity.builder().channelName(channelName)
//				.channelBlock(channelBlock).channelTx(channelTx).orderingOrg(orderingOrg)
//				.appAdminPolicyType(appAdminPolicyType).appAdminPolicyValue(appAdminPolicyValue)
//				.ordererAdminPolicyType(ordererAdminPolicyType).ordererAdminPolicyValue(ordererAdminPolicyValue)
//				.channelAdminPolicyType(channelAdminPolicyType).channelAdminPolicyValue(channelAdminPolicyValue)
//				.batchTimeout(batchTimeout).batchSizeAbsolMax(batchSizeAbsolMax).batchSizeMaxMsg(batchSizeMaxMsg)
//				.batchSizePreferMax(batchSizePreferMax);
//		
//		if (createdAt == null) {
//			return channelInfoEntityBuilder.build();
//		} else {
//			return channelInfoEntityBuilder.createdAt(createdAt).build();
//		}
//	}

}
