package com.brchain.core.chaincode.dto;

import lombok.Data;

@Data
public class InstallCcDto {

	private String orgName;   // 조직 이름
	private int    conNum;    // 컨테이너 번호
	private String ccName;    // 체인코드 이름
	private String ccVersion; // 체인코드 버전
	private Long   id;        // 체인코드 id

}
