package com.brchain.core.container.dto;

import lombok.Data;

@Data
public class DockerStatsDto {

	private String  conId;      // 컨테이너 아이디
	private String  conName;    // 컨테이너 이름
	private String  conStatus;  // 컨테이너 상태
	private String  conCreated; // 컨테이너 생성 시간
	private Integer conPort;    // 컨테이너 포

}
