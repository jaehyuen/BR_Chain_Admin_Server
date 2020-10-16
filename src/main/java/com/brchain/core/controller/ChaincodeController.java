package com.brchain.core.controller;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.brchain.core.dto.InstallCcDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.service.CcInfoService;
import com.brchain.core.service.FabricService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/chaincode/")
public class ChaincodeController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	CcInfoService ccInfoService;

	@Autowired
	FabricService fabricService;

	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChaincodeList(
			@RequestParam(value = "conName", required = false) String conName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(ccInfoService.getCcListPeer(conName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(ccInfoService.getCcList());
		}

	}

	@PostMapping(value = "/install")
	public ResponseEntity<ResultDto> installChaincode(@RequestBody InstallCcDto installCcDto) throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.installChaincode(installCcDto));

	}

	@PostMapping(value = "/upload")
	public ResponseEntity<ResultDto> uploadChaincode(@RequestParam("ccFile") MultipartFile ccFile,
			@RequestParam("ccName") String ccName, @RequestParam("ccDesc") String ccDesc,
			@RequestParam("ccLang") String ccLang) throws IOException {

		return ResponseEntity.status(HttpStatus.OK).body(ccInfoService.ccFileUpload(ccFile, ccName, ccDesc, ccLang));

	}
}
