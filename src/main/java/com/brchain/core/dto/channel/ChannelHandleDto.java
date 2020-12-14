package com.brchain.core.dto.channel;

import java.time.LocalDateTime;

import com.brchain.core.entity.channel.ChannelHandleEntity;
import com.brchain.core.entity.channel.ChannelHandleEntity.ChannelHandleEntityBuilder;

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
	private LocalDateTime createdAt;

//	public ChannelHandleEntity toEntity() {
//
//		ChannelHandleEntityBuilder channelHandleEntityBuilder = ChannelHandleEntity.builder().handle(handle)
//				.channelName(channelName);
//		if (createdAt == null) {
//			return channelHandleEntityBuilder.build();
//		} else {
//			return channelHandleEntityBuilder.createdAt(createdAt).build();
//		}
//	}

}
