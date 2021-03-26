package com.brchain.core.container.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DockerStatsDto {

	private String conId; // 컨테이너 아이디
	private String conName; // 컨테이너 이름
	private String conStatus;
	private String conCreated;
	private Integer conPort;
	
}
