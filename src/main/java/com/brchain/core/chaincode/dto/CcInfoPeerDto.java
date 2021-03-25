package com.brchain.core.chaincode.dto;

import java.time.LocalDateTime;

import com.brchain.core.container.dto.ConInfoDto;

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

	private Long id; // 아이
	private String ccVersion; // 체인코드 버전
	private ConInfoDto conInfoDto; // 컨테이너 정보
	private CcInfoDto ccInfoDto;// 체인코드 정보
	private LocalDateTime createdAt; // 생성 시

}
