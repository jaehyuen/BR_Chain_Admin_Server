package com.brchain.core.dto;

import javax.persistence.Column;

import com.brchain.core.entity.CcInfoChannelEntity;
import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;
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

	private String ccName; // 체인코드 이름
	private String ccVersion; // 체인코드 버전
	private String ccLang; // 체인코드 언어
	private String channelName; // 채널 이름

	public CcInfoChannelEntity toEntity() {

		CcInfoChannelEntity ccInfoChannelEntity = CcInfoChannelEntity.builder().ccName(ccName).ccVersion(ccVersion)
				.ccLang(ccLang).channelName(channelName).build();
		return ccInfoChannelEntity;
	}

}
