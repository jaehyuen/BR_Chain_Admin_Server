package com.brchain.core.chaincode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CcSummaryDto {

	private String conName; // 컨테이너 이름
	private Long    ccCnt;   // 체인코드 개수

}
