package com.brchain.core.dto.chaincode;

import java.time.LocalDateTime;

import com.brchain.core.entity.chaincode.CcInfoChannelEntity;
import com.brchain.core.entity.chaincode.CcInfoChannelEntity.CcInfoChannelEntityBuilder;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

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
public class CcInfoChannelDto {

	private Long id; //id
	private String ccVersion; // 체인코드 버전
	private ChannelInfoEntity channelInfoEntity; // 채널 정보
	private CcInfoEntity ccInfoEntity; // 체인코드 정보
	private LocalDateTime createdAt;

//	public CcInfoChannelEntity toEntity() {
//		
//		CcInfoChannelEntityBuilder ccInfoChannelEntityBuilder = CcInfoChannelEntity.builder().id(id)
//				.ccVersion(ccVersion).channelInfoEntity(channelInfoEntity).ccInfoEntity(ccInfoEntity);
//		
//		if (createdAt == null) {
//			return ccInfoChannelEntityBuilder.build();
//		} else {
//			return ccInfoChannelEntityBuilder.createdAt(createdAt).build();
//		}
//
//	}

}
