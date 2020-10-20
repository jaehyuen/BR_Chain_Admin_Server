package com.brchain.core.dto;

import com.brchain.core.entity.ChannelHandlerEntity;
import com.brchain.core.entity.ChannelInfoEntity;
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
public class ChannelHandlerDto {

	private String handler; // 이벤트 핸들러
	private ChannelInfoEntity channelInfoEntity; // 채널 정보

	public ChannelHandlerEntity toEntity() {

		ChannelHandlerEntity channelHandlerEntity = ChannelHandlerEntity.builder().handler(handler)
				.channelInfoEntity(channelInfoEntity).build();
		return channelHandlerEntity;
	}

}
