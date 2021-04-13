package com.brchain.core.fabric.controller;

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
import com.brchain.core.fabric.service.BlockService;
import com.brchain.core.fabric.service.FabricService;
import com.brchain.core.fabric.service.TransactionService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class CoreController {

	private final ContainerService   containerService;
	private final FabricService      fabricService;
	private final BlockService       blockService;
	private final TransactionService transactionService;

	@ApiOperation(value = "조직 타입에 따를 컨데이너 정보 조회", notes = "조직 타입에 따른 컨테이너 정보를 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/org/list")
	public ResponseEntity<ResultDto> getOrgList(@ApiParam(value = "조직 타입(peer, orderer, ca)", required = true) @RequestParam(value = "type") String orgType) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.getOrgList(orgType));

	}

	@ApiOperation(value = "조직 생성", notes = "HyperLedger Fabric 조직을 생성하는 API", authorizations = { @Authorization(value = "Authorization") })
	@PostMapping("/org/create")
	public ResponseEntity<ResultDto> createContainer(@ApiParam(value = "조직 생성 관련 컨테이너 DTO 리스트", required = true) @RequestBody ArrayList<CreateOrgConInfoDto> createOrgConInfoDtoArr) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createOrg(createOrgConInfoDtoArr));

	}

	@ApiOperation(value = "조직 맴버 정보 조회", notes = "HyperLedger Fabric 조직 이름에 따른 컨테이너 정보를 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/member/list")
	public ResponseEntity<ResultDto> getMemberList(@ApiParam(value = "조회할 HyperLedger Fabric 조직명", required = true) @RequestParam(value = "orgName") String orgName) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.getMemberList(orgName));

	}
	
	@ApiOperation(value = "채널 블록 리스트 조회", notes = "HyperLedger Fabric 채널 이름으로 블록 정보들을 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/block/list")
	public ResponseEntity<ResultDto> getBlockList(@ApiParam(value = "조회할 HyperLedger Fabric 조직명", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(blockService.getBlockListByChannel(channelName));

	}
	
	@ApiOperation(value = "블록 정보 조회", notes = "블록 데이터 해쉬값으로 블록 정보를 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/block")
	public ResponseEntity<ResultDto> getBlock(@ApiParam(value = "조회할 블록 데이터 해쉬값", required = true) @RequestParam(value = "blockDataHash") String blockDataHash) {

		return ResponseEntity.status(HttpStatus.OK).body(blockService.getBlockByBlockDataHash(blockDataHash));

	}
	
	@ApiOperation(value = "채널 트랜잭션 리스트 조회", notes = "HyperLedger Fabric 채널 이름으로 트렌잭션 정보들을 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/transaction/list")
	public ResponseEntity<ResultDto> getTxList(@ApiParam(value = "조회할 HyperLedger Fabric 채널명", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTxListByChannel(channelName));

	}
	
	@ApiOperation(value = "트랜잭션 정보 조회", notes = "트랜잭션 아이디값으로 트랜잭션 정보를 조회하는 API", authorizations = { @Authorization(value = "Authorization") })
	@GetMapping("/transaction")
	public ResponseEntity<ResultDto> getTx(@ApiParam(value = "트랜잭션 아이디값", required = true) @RequestParam(value = "txId") String txId) {

		return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTxByTxId(txId));

	}

}
