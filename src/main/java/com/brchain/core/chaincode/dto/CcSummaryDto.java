package com.brchain.core.chaincode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CcSummaryDto {

	@Schema(example="peer0.orgtest.com")
	private String conName; // 컨테이너 이름
	@Schema(example="1")
	private Long    ccCnt;   // 체인코드 개수

}
