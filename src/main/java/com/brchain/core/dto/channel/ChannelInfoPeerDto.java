package com.brchain.core.dto.channel;

import java.time.LocalDateTime;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity.ChannelInfoPeerEntityBuilder;

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

	private Long id;
	private boolean anchorYn; // 앵커피어 여부
	private ConInfoEntity conInfoEntity; // 컨테이너 정보
	private ChannelInfoEntity channelInfoEntity;// 채널 정보
	private LocalDateTime createdAt;

//	public ChannelInfoPeerEntity toEntity() {
//
//		ChannelInfoPeerEntityBuilder channelInfoPeerEntityBuilder = ChannelInfoPeerEntity.builder().id(id).anchorYn(anchorYn)
//				.conInfoEntity(conInfoEntity).channelInfoEntity(channelInfoEntity);
//
//		if (createdAt == null) {
//			return channelInfoPeerEntityBuilder.build();
//		} else {
//			return channelInfoPeerEntityBuilder.createdAt(createdAt).build();
//		}
//	}

}
