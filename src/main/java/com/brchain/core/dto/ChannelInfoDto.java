package com.brchain.core.dto;

import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ConInfoEntity;

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
	private String activeCc; // 활성화된 체인코드

	public ChannelInfoEntity toEntity() {

		ChannelInfoEntity channelInfoEntity = ChannelInfoEntity.builder().channelName(channelName)
				.channelBlock(channelBlock).channelTx(channelTx).orderingOrg(orderingOrg).activeCc(activeCc).build();
		return channelInfoEntity;
	}

}
