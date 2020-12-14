package com.brchain.core.dto.chaincode;

import java.time.LocalDateTime;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity.CcInfoPeerEntityBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcInfoPeerDto {

	private Long id;
	private String ccVersion; // 체인코드 버전
	private ConInfoEntity conInfoEntity; // 컨테이너 정보
	private CcInfoEntity ccInfoEntity;// 체인코드 정보
	private LocalDateTime createdAt;

//	public CcInfoPeerEntity toEntity() {
//
//		CcInfoPeerEntityBuilder ccInfoPeerEntityBuilder = CcInfoPeerEntity.builder().id(id).ccVersion(ccVersion)
//				.conInfoEntity(conInfoEntity).ccInfoEntity(ccInfoEntity);
//		if (createdAt == null) {
//			return ccInfoPeerEntityBuilder.build();
//		} else {
//			return ccInfoPeerEntityBuilder.createdAt(createdAt).build();
//		}
//	}

}
