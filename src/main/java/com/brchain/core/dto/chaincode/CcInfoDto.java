package com.brchain.core.dto.chaincode;

import java.time.LocalDateTime;

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

	private Long id; // 아이디
	private String ccName; // 체인코드 이름
	private String ccPath; // 체인코드 경로
	private String ccLang; // 체인코드 언어
	private String ccDesc; // 체인코드 설명
	private String ccVersion; // 체인코드 버전
	private LocalDateTime createdAt; // 생성 시간

}
