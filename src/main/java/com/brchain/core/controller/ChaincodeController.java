package com.brchain.core.controller;

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
import com.brchain.core.dto.chaincode.InstallCcDto;
import com.brchain.core.dto.chaincode.InstantiateCcDto;
import com.brchain.core.service.ChaincodeService;
import com.brchain.core.service.FabricService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/chaincode/")
@RequiredArgsConstructor
public class ChaincodeController {
	
	private final ChaincodeService chaincodeService;
	private final FabricService fabricService;

	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChaincodeList(
			@RequestParam(value = "conName", required = false) String conName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListPeer(conName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcList());
		}

	}
	
	
	@GetMapping("/channel/list")
	public ResponseEntity<ResultDto> getChaincodeListChannel(@RequestParam(value = "channelName", required = true) String channelName) {

			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListActive(channelName));
		

	}

	@GetMapping("/active")
	public ResponseEntity<ResultDto> getChaincodeListToActiveInChannel(@RequestParam(value = "channelName", required = true) String channelName) {

			return ResponseEntity.status(HttpStatus.OK).body(chaincodeService.getCcListToActiveInChannel(channelName));
		

	}

	@PostMapping(value = "/install")
	public ResponseEntity<ResultDto> installChaincode(@RequestBody InstallCcDto installCcDto) throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.installChaincode(installCcDto));

	}

	@PostMapping(value = "/upload")
	public ResponseEntity<ResultDto> uploadChaincode(@RequestParam("ccFile") MultipartFile ccFile,
			@RequestParam("ccName") String ccName, @RequestParam("ccDesc") String ccDesc,
			@RequestParam("ccLang") String ccLang,@RequestParam("ccVersion") String ccVersion) throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.ccFileUpload(ccFile, ccName, ccDesc, ccLang,ccVersion));

	}
	
	@PostMapping("/active")
	public ResponseEntity<ResultDto> getChaincodeListToActiveInChannel(@RequestBody InstantiateCcDto instantiateCcDto) throws Exception{

			return ResponseEntity.status(HttpStatus.OK).body(fabricService.instantiateChaincode(instantiateCcDto));
		

	}
}
