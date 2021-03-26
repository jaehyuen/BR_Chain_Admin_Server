package com.brchain.core.container.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.container.dto.CreateOrgConInfoDto;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.container.service.DockerService;
import com.brchain.core.fabric.service.FabricService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/container")
@RequiredArgsConstructor

public class ContainerController {

	private final DockerService    dockerService;
	private final ContainerService containerService;

	@ApiOperation(value = "모든 컨테이너 정보 조회", notes = "모든 도커 컨테이너 정보를 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getContainerInfo() {

		return ResponseEntity.status(HttpStatus.OK).body(dockerService.getAllContainersInfo());

	}

	@ApiOperation(value = "컨테이너 삭제", notes = "컨테이너 ID 또는 조직명으로 컨테이너 중지 및 삭제하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/remove")
	public ResponseEntity<ResultDto> removeContainer(@ApiParam(value = "삭제할 컨테이너 ID", required = false) @RequestParam(value = "conId", required = false) String conId, @ApiParam(value = "삭제할 조직명", required = false) @RequestParam(value = "orgName", required = false) String orgName) {

		if (conId != null && conId.equals("")) {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeAllContainers());

		} else if (conId != null) {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeContainer(conId));

		} else {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeOrgContainers(orgName));
		}

	}

	@ApiOperation(value = "포트 체크", notes = "사용중인 포트인지 체크하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/check/port")
	public ResponseEntity<ResultDto> portCheck(@ApiParam(value = "확인할 포트 번호", required = false) @RequestParam(value = "port") String port) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.canUseConPort(port));

	}

}
