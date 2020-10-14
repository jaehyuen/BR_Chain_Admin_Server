package com.brchain.core.dto;

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
public class CcInfoDto {

	private String ccName; // 체인코드 이름
	private String ccPath; // 체인코드 경로 
	private String ccLang; // 체인코드 언어
	private String ccDesc; // 체인코드 설명

	public CcInfoEntity toEntity() {

		CcInfoEntity ccInfoEntity = CcInfoEntity.builder().ccName(ccName)
				.ccPath(ccPath).ccLang(ccLang).ccDesc(ccDesc).build();
		return ccInfoEntity;
	}


}
