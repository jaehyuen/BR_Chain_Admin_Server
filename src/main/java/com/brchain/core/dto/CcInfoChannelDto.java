package com.brchain.core.dto;

import com.brchain.core.entity.CcInfoChannelEntity;
import com.brchain.core.entity.CcInfoEntity;
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
public class CcInfoChannelDto {

	private String ccVersion; // 체인코드 버전
	private ChannelInfoEntity channelInfoEntity; // 채널 정보
	private CcInfoEntity ccInfoEntity; // 체인코드 정보

	public CcInfoChannelEntity toEntity() {

		CcInfoChannelEntity ccInfoChannelEntity = CcInfoChannelEntity.builder().ccVersion(ccVersion)
				.channelInfoEntity(channelInfoEntity).ccInfoEntity(ccInfoEntity).build();
		return ccInfoChannelEntity;
	}

}
