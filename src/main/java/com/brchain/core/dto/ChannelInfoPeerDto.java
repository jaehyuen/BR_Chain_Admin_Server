package com.brchain.core.dto;

import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;
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
public class ChannelInfoPeerDto {

	private boolean anchorYn; // 앵커피어 여부
	private ConInfoEntity conInfoEntity; // 컨테이너 정보
	private ChannelInfoEntity channelInfoEntity;// 채널 정보

	public ChannelInfoPeerEntity toEntity() {

		ChannelInfoPeerEntity channelInfoPeerEntity = ChannelInfoPeerEntity.builder().anchorYn(anchorYn)
				.conInfoEntity(conInfoEntity).channelInfoEntity(channelInfoEntity).build();
		return channelInfoPeerEntity;
	}

}
