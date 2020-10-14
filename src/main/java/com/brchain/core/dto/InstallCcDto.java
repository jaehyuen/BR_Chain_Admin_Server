package com.brchain.core.dto;

import lombok.Data;

@Data
public class InstallCcDto {

	private String orgName; // 결과 코드
	private int conNum; // 결과 메시지
	private String ccName; // 결과 데이터
	private String ccVersion; // 결과 플래그
}
