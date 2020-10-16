package com.brchain.core.dto;

import javax.persistence.Column;

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
public class CcInfoPeerDto {

	private String ccName; // 체인코드 이름
	private String ccVersion; // 체인코드 버전
	private String ccLang; // 체인코드 언어
	private String conName; // 컨테이너 이름
	private int conNum; // 컨테이너 번호
	private String orgName; // 조직 이름

	public CcInfoPeerEntity toEntity() {

		CcInfoPeerEntity ccInfoPeerEntity = CcInfoPeerEntity.builder().ccName(ccName).ccVersion(ccVersion)
				.ccLang(ccLang).conName(conName).conNum(conNum).orgName(orgName).build();
		return ccInfoPeerEntity;
	}

}
