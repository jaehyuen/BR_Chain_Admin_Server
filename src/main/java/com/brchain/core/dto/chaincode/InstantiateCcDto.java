package com.brchain.core.dto.chaincode;

import lombok.Data;

@Data
public class InstantiateCcDto {

	private String channelName; // 채널 이름
	private String ccLang; // 체인코드 언어
	private String ccName; // 체인코드 이름
	private String ccVersion; // 체인코드 버전
	private Long id;

}
