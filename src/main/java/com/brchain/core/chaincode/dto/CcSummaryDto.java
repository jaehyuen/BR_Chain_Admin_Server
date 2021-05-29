package com.brchain.core.chaincode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description="체인코드 설치 요약 정보 dto")
public class CcSummaryDto {

	@Schema(example = "peer0.orgtest.com", description = "피어 컨테이너 이름")
	private String conName; // 컨테이너 이름
	@Schema(example = "1", description = "설치된 체인코드 개수")
	private Long ccCnt; // 체인코드 개수

}
