package com.brchain.core.dto;

import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;
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
public class CcInfoPeerDto {

	private String ccVersion; // 체인코드 버전
	private ConInfoEntity conInfoEntity; // 컨테이너 정보
	private CcInfoEntity ccInfoEntity;// 체인코드 정보

	public CcInfoPeerEntity toEntity() {

		CcInfoPeerEntity ccInfoPeerEntity = CcInfoPeerEntity.builder().ccVersion(ccVersion).conInfoEntity(conInfoEntity)
				.ccInfoEntity(ccInfoEntity).build();
		return ccInfoPeerEntity;
	}

}
