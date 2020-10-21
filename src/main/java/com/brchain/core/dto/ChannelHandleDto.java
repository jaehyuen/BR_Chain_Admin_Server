package com.brchain.core.dto;

import com.brchain.core.entity.ChannelHandleEntity;
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
public class ChannelHandleDto {

	private String handle; // 이벤트 핸들러
	private String channelName; // 채널 정보

	public ChannelHandleEntity toEntity() {

		ChannelHandleEntity channelHandleEntity = ChannelHandleEntity.builder().handle(handle)
				.channelName(channelName).build();
		return channelHandleEntity;
	}

}
