package com.brchain.core.chaincode.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error401ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error403ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error500ResultDto;
import com.brchain.core.chaincode.dto.ActiveCcDto;
import com.brchain.core.chaincode.dto.CcInfoChannelDto;
import com.brchain.core.chaincode.dto.CcInfoDto;
import com.brchain.core.chaincode.dto.CcInfoPeerDto;
import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.chaincode.dto.InstallCcDto;
import com.brchain.core.chaincode.service.ChaincodeService;
import com.brchain.core.fabric.service.FabricService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/chaincode/")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class ChaincodeController {

	private final ChaincodeService chaincodeService;
	private final FabricService    fabricService;

	@Operation(summary = "Hyperledger Fabric 체인코드 조회", description = "Hyperledger Fabric 체인코드를 조회하는 API (분리예정)", responses = {
			@ApiResponse(responseCode = "200", content = {
					@Content(schema = @Schema(implementation = CcInfoResultDto.class)),
					@Content(schema = @Schema(implementation = CcInfoPeerResultDto.class)) }),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChaincodeList(
			@Parameter(description = "컨테이너 이름", required = false) @RequestParam(value = "conName", required = false) String conName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK)
				.body(chaincodeService.getCcListPeer(conName));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
				.body(chaincodeService.getCcList());
		}

	}

	@Operation(summary = "Hyperledger Fabric 체인코드 요약 리스트 조회", description = "Hyperledger Fabric 체인코드 요약 리스트를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CcSummaryResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/list/summary")
	public ResponseEntity<ResultDto> getChannelSummaryList() {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chaincodeService.getCcSummaryList());

	}

	@Operation(summary = "Hyperledger Fabric 채널에 활성화된 체인코드 조회", description = "Hyperledger Fabric 채널에 활성화된 체인코드를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CcInfoChannelResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/list/channel")
	public ResponseEntity<ResultDto> getChaincodeListChannel(
			@Parameter(description = "채널 이름", required = true) @RequestParam(value = "channelName", required = true) String channelName) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chaincodeService.getCcListActive(channelName));

	}

	@Operation(summary = "Hyperledger Fabric 채널에 활성 가능한 체인코드 조회", description = "Hyperledger Fabric 활성 가능한 체인코드를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/list/toactive")
	public ResponseEntity<ResultDto> getChaincodeListToActiveInChannel(
			@Parameter(description = "채널 이름", required = true) @RequestParam(value = "channelName", required = true) String channelName) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chaincodeService.getCcListToActiveInChannel(channelName));

	}

	@Operation(summary = "Hyperledger Fabric 체인코드 설치", description = "Hyperledger Fabric 체인코드를 피어에 설치하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@PostMapping(value = "/install")
	public ResponseEntity<ResultDto> installChaincode(
			@Parameter(description = "체인코드 설치 관련 DTO", required = true) @RequestBody InstallCcDto installCcDto)
			throws IOException {

		return ResponseEntity.status(HttpStatus.OK)
			.body(fabricService.installChaincode(installCcDto));

	}

	@Operation(summary = "Hyperledger Fabric 체인코드 업로드", description = "Hyperledger Fabric 체인코드를 서버에 업로드하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@PostMapping(value = "/upload")
	public ResponseEntity<ResultDto> uploadChaincode(
			@Parameter(description = "체인코드 압축 파일", required = true) @RequestParam("ccFile") MultipartFile ccFile,
			@Parameter(description = "체인코드 이름", required = true) @RequestParam("ccName") String ccName,
			@Parameter(description = "체인코드 설명", required = true) @RequestParam("ccDesc") String ccDesc,
			@Parameter(description = "체인코드 언어", required = true) @RequestParam("ccLang") String ccLang,
			@Parameter(description = "체인코드 버전", required = true) @RequestParam("ccVersion") String ccVersion)
			throws IOException {

		return ResponseEntity.status(HttpStatus.OK)
			.body(fabricService.ccFileUpload(ccFile, ccName, ccDesc, ccLang, ccVersion));

	}

	@Operation(summary = "Hyperledger Fabric 체인코드 활성화", description = "Hyperledger Fabric 체인코드를 채널에 활성화 하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@PostMapping("/active")
	public ResponseEntity<ResultDto> getChaincodeListToActiveInChannel(
			@Parameter(description = "체인코드 활성화 관련 DTO", required = true) @RequestBody ActiveCcDto activeCcDto)
			throws Exception {

		return ResponseEntity.status(HttpStatus.OK)
			.body(fabricService.activeChaincode(activeCcDto));

	}

	private class CcInfoResultDto extends ResultDto<CcInfoDto> {
	}

	private class CcInfoPeerResultDto extends ResultDto<CcInfoPeerDto> {
	}

	private class CcSummaryResultDto extends ResultDto<CcSummaryDto> {
	}

	private class CcInfoChannelResultDto extends ResultDto<CcInfoChannelDto> {
	}

}
