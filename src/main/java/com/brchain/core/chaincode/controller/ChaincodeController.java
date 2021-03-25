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
import com.brchain.core.chaincode.dto.ActiveCcDto;
import com.brchain.core.chaincode.dto.InstallCcDto;
import com.brchain.core.chaincode.service.ChaincodeService;
import com.brchain.core.fabric.service.FabricService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/chaincode/")
@RequiredArgsConstructor
public class ChaincodeController {

	private final ChaincodeService chaincodeService;
	private final FabricService fabricService;

	@ApiOperation(value = "Hyperledger Fabric 체인코드 조회", notes = "Hyperledger Fabric 체인코드를 조회하는 API (분리예정)")
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChaincodeList(
			@ApiParam(value = "컨테이너 이름", required = false) @RequestParam(value = "conName", required = false) String conName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListPeer(conName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcList());
		}

	}

	@ApiOperation(value = "Hyperledger Fabric 채널에 활성화된 체인코드 조회", notes = "Hyperledger Fabric 채널에 활성화된 체인코드를 조회하는 API")
	@GetMapping("/list/channel")
	public ResponseEntity<ResultDto> getChaincodeListChannel(
			@ApiParam(value = "채널 이름", required = true) @RequestParam(value = "channelName", required = true) String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListActive(channelName));

	}

	@ApiOperation(value = "Hyperledger Fabric 채널에 활성 가능한 체인코드 조회", notes = "Hyperledger Fabric 활성 가능한 체인코드를 조회하는 API")
	@GetMapping("/list/toactive")
	public ResponseEntity<ResultDto> getChaincodeListToActiveInChannel(
			@ApiParam(value = "채널 이름", required = true) @RequestParam(value = "channelName", required = true) String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListToActiveInChannel(channelName));

	}

	@ApiOperation(value = "Hyperledger Fabric 체인코드 설치", notes = "Hyperledger Fabric 체인코드를 피어에 설치하는 API")
	@PostMapping(value = "/install")
	public ResponseEntity<ResultDto> installChaincode(
			@ApiParam(value = "체인코드 설치 관련 DTO", required = true) @RequestBody InstallCcDto installCcDto)
			throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.installChaincode(installCcDto));

	}

	@ApiOperation(value = "Hyperledger Fabric 체인코드 업로드", notes = "Hyperledger Fabric 체인코드를 서버에 업로드하는 API")
	@PostMapping(value = "/upload")
	public ResponseEntity<ResultDto> uploadChaincode(
			@ApiParam(value = "체인코드 압축 파일", required = true) @RequestParam("ccFile") MultipartFile ccFile,
			@ApiParam(value = "체인코드 이름", required = true) @RequestParam("ccName") String ccName,
			@ApiParam(value = "체인코드 설명", required = true) @RequestParam("ccDesc") String ccDesc,
			@ApiParam(value = "체인코드 언어", required = true) @RequestParam("ccLang") String ccLang,
			@ApiParam(value = "체인코드 버전", required = true) @RequestParam("ccVersion") String ccVersion)
			throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.ccFileUpload(ccFile, ccName, ccDesc, ccLang, ccVersion));

	}

	@ApiOperation(value = "Hyperledger Fabric 체인코드 활성화", notes = "Hyperledger Fabric 체인코드를 채널에 활성화 하는 API")
	@PostMapping("/active")
	public ResponseEntity<String> getChaincodeListToActiveInChannel(
			@ApiParam(value = "체인코드 활성화 관련 DTO", required = true) @RequestBody ActiveCcDto activeCcDto)throws Exception {
			fabricService.activeChaincode(activeCcDto);
		return ResponseEntity.status(HttpStatus.OK).body("");

	}
}
