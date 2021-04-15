package com.brchain.core.container.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.container.service.DockerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/container")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class ContainerController {

	private final DockerService    dockerService;
	private final ContainerService containerService;

	@Operation(summary = "모든 컨테이너 정보 조회", description = "모든 도커 컨테이너 정보를 조회하는 API")
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getContainerInfo() {

		return ResponseEntity.status(HttpStatus.OK).body(dockerService.getAllContainersInfo());

	}

	@Operation(summary = "컨테이너 삭제", description = "컨테이너 ID 또는 조직명으로 컨테이너 중지 및 삭제하는 API")
	@GetMapping("/remove")
	public ResponseEntity<ResultDto> removeContainer(@Parameter(description = "삭제할 컨테이너 ID", required = false) @RequestParam(value = "conId", required = false) String conId, @Parameter(description = "삭제할 조직명", required = false) @RequestParam(value = "orgName", required = false) String orgName) {

		if (conId != null && conId.equals("")) {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeAllContainers());

		} else if (conId != null) {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeContainer(conId));

		} else {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeOrgContainers(orgName));
		}

	}

	@Operation(summary = "포트 체크", description = "사용중인 포트인지 체크하는 API")
	@GetMapping("/check/port")
	public ResponseEntity<ResultDto> portCheck(@Parameter(description = "확인할 포트 번호", required = false) @RequestParam(value = "port") String port) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.canUseConPort(port));

	}

}
