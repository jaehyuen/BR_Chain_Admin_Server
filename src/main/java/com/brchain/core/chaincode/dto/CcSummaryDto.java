package com.brchain.core.chaincode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CcSummaryDto {

	private String conName;
	private int    ccCnt;
	
	
}
