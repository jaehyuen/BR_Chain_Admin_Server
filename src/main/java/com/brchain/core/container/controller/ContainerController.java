package com.brchain.core.container.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error401ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error403ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error500ResultDto;
import com.brchain.core.container.dto.DockerStatsDto;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.container.service.DockerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/container")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class ContainerController {

	private final DockerService    dockerService;
	private final ContainerService containerService;

	@Operation(summary = "모든 컨테이너 정보 조회", description = "모든 도커 컨테이너 정보를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = DockerStatsResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getContainerInfo() {

		return ResponseEntity.status(HttpStatus.OK).body(dockerService.getAllContainersInfo());

	}

	@Operation(summary = "컨테이너 삭제", description = "컨테이너 ID 또는 조직명으로 컨테이너 중지 및 삭제하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
//	@GetMapping("/remove")
	@DeleteMapping(value = {"/{conId}","/"})
	public ResponseEntity<ResultDto> removeContainer(@PathVariable("conId")  String conId) {

		if (conId == null ) {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeAllContainers());

		} else {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeContainer(conId));
		}


	}

	@Operation(summary = "컨테이너 재기동", description = "실행중인 컨테이너를 재기동 하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/reboot/{conId}")
	public ResponseEntity<ResultDto> rebootContainer(@PathVariable("conId")  String conId) {

		return ResponseEntity.status(HttpStatus.OK).body(dockerService.rebootContainer(conId));

	}
	
	@Operation(summary = "포트 체크", description = "사용중인 포트인지 체크하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/check/port")
	public ResponseEntity<ResultDto> portCheck(@Parameter(description = "확인할 포트 번호", required = false) @RequestParam(value = "port") String port) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.canUseConPort(port));

	}
	private class DockerStatsResultDto extends ResultDto<List<DockerStatsDto>> {
	}
	

}
