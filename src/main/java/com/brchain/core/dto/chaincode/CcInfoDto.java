package com.brchain.core.dto.chaincode;

import java.time.LocalDateTime;

import com.brchain.core.dto.BlockDto;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity.CcInfoEntityBuilder;
import com.brchain.core.entity.channel.ChannelInfoEntity;

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
public class CcInfoDto {

	private String ccName; // 체인코드 이름
	private String ccPath; // 체인코드 경로
	private String ccLang; // 체인코드 언어
	private String ccDesc; // 체인코드 설명
	private LocalDateTime createdAt;

//	public CcInfoEntity toEntity() {
//
//		CcInfoEntityBuilder ccInfoEntityBuilder = CcInfoEntity.builder().ccName(ccName).ccPath(ccPath).ccLang(ccLang)
//				.ccDesc(ccDesc);
//
//		if (createdAt == null) {
//			return ccInfoEntityBuilder.build();
//		} else {
//			return ccInfoEntityBuilder.createdAt(createdAt).build();
//		}
//	}

}
