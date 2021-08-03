package com.brchain.core.fabric.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error401ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error403ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error500ResultDto;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.CreateOrgConInfoDto;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.dto.BlockDto;
import com.brchain.core.fabric.dto.TransactionDto;
import com.brchain.core.fabric.service.BlockService;
import com.brchain.core.fabric.service.FabricService;
import com.brchain.core.fabric.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

	@Operation(summary = "조직 타입에 따를 컨데이너 정보 조회", description = "조직 타입에 따른 컨테이너 정보를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ConInfoResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/org/list")
	public ResponseEntity<ResultDto> getOrgList(@Parameter(description = "조직 타입(peer, orderer, ca)", required = true) @RequestParam(value = "type") String orgType) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.getOrgList(orgType));

	}

	@Operation(summary = "조직 생성", description = "HyperLedger Fabric 조직을 생성하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@PostMapping("/org/create")
	public ResponseEntity<ResultDto> createOrg(@Parameter(description = "조직 생성 관련 컨테이너 DTO 리스트", required = true) @RequestBody ArrayList<CreateOrgConInfoDto> createOrgConInfoDtoArr) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createOrg(createOrgConInfoDtoArr));

	}
	
	@DeleteMapping("/org/{orgName}")
	public ResponseEntity<ResultDto> removeOrg(@PathVariable("orgName") String orgName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.removeOrg(orgName));

	}

	@Operation(summary = "조직 맴버 정보 조회", description = "HyperLedger Fabric 조직 이름에 따른 컨테이너 정보를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ConInfoResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/member/list")
	public ResponseEntity<ResultDto> getMemberList(@Parameter(description = "조회할 HyperLedger Fabric 조직명", required = true) @RequestParam(value = "orgName") String orgName) {

		return ResponseEntity.status(HttpStatus.OK).body(containerService.getMemberList(orgName));

	}
	
	@Operation(summary = "채널 블록 리스트 조회", description = "HyperLedger Fabric 채널 이름으로 블록 정보들을 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BlockAndTxDtoResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/block/list")
	public ResponseEntity<ResultDto> getBlockList(@Parameter(description = "조회할 HyperLedger Fabric 조직명", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(blockService.getBlockListByChannel(channelName));

	}
	
	@Operation(summary = "블록 정보 조회", description = "블록 데이터 해쉬값으로 블록 정보를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BlockResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/block")
	public ResponseEntity<ResultDto> getBlock(@Parameter(description = "조회할 블록 데이터 해쉬값", required = true) @RequestParam(value = "blockDataHash") String blockDataHash) {

		return ResponseEntity.status(HttpStatus.OK).body(blockService.getBlockByBlockDataHash(blockDataHash));

	}
	
	@Operation(summary = "채널 트랜잭션 리스트 조회", description = "HyperLedger Fabric 채널 이름으로 트렌잭션 정보들을 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionListResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/transaction/list")
	public ResponseEntity<ResultDto> getTxList(@Parameter(description = "조회할 HyperLedger Fabric 채널명", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTxListByChannel(channelName));

	}
	
	@Operation(summary = "트랜잭션 정보 조회", description = "트랜잭션 아이디값으로 트랜잭션 정보를 조회하는 API", responses = {
			@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TransactionResultDto.class))),
			@ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = Error401ResultDto.class))),
			@ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = Error403ResultDto.class))),
			@ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Error500ResultDto.class))) })
	@GetMapping("/transaction")
	public ResponseEntity<ResultDto> getTx(@Parameter(description = "트랜잭션 아이디값", required = true) @RequestParam(value = "txId") String txId) {

		return ResponseEntity.status(HttpStatus.OK).body(transactionService.getTxByTxId(txId));

	}
	
	private class ConInfoResultDto extends ResultDto<List<ConInfoDto>> {
	}
	
	private class BlockAndTxDtoResultDto extends ResultDto<List<BlockAndTxDto>> {
	}
	
	private class BlockResultDto extends ResultDto<BlockDto> {
	}
	
	private class TransactionResultDto extends ResultDto<TransactionDto> {
	}
	
	private class TransactionListResultDto extends ResultDto<List<TransactionDto>> {
	}

}
