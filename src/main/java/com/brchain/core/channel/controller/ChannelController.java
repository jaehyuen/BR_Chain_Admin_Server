package com.brchain.core.channel.controller;

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
import com.brchain.core.channel.dto.CreateChannelDto;
import com.brchain.core.channel.service.ChannelService;
import com.brchain.core.fabric.service.FabricService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/channel")
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class ChannelController {

	private final ChannelService channelService;
	private final FabricService fabricService;

	@Operation(summary = "Hyperledger Fabric 채널 조회", description = "Hyperledger Fabric 채널 조회하는 API")
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChannelList(
			@Parameter(description = "채널 이름", required = false) @RequestParam(value = "channelName", required = false) String channelName) {

		if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelList());
		}

	}
	
	@Operation(summary = "Hyperledger Fabric 채널 요약 리스트 조회", description = "Hyperledger Fabric 채널 요약 리스트를 조회하는 API")
	@GetMapping("/list/summary")
	public ResponseEntity<ResultDto> getChannelSummaryList() {

		return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelSummaryList());

	}

	@Operation(summary = "Hyperledger Fabric 채널에 가입된 컨테이너 조회", description = "컨테이너 이름 및 채널명으로 Hyperledger Fabric 채널에 가입된 컨테이너를 조회하는 API")
	@GetMapping("/list/peer")
	public ResponseEntity<ResultDto> getChannelListPeer(
			@Parameter(description = "컨테이너 이름", required = false) @RequestParam(value = "conName", required = false) String conName,
			@Parameter(description = "채널 이름", required = false) @RequestParam(value = "channelName", required = false) String channelName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelListPeerByConName(conName));
		} else if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(channelService.getChannelListPeerByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultDto());
		}

	}

	@Operation(summary = "Hyperledger Fabric 채널 생성 및 가입", description = "HyperLedger Fabric 채널을 생성하고 가입하는 API")
	@PostMapping("/create")
	public ResponseEntity<ResultDto> createChannel(
			@Parameter(description = "채널 생성 관련 DTO", required = true) @RequestBody CreateChannelDto createChannelDto) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createChannel(createChannelDto));

	}

	@Operation(summary = "Hyperledger Fabric 채널 이벤트 리스너 등록", description = "Hyperledger Fabric 채널 이벤트 리스너 등록하는 API")
	@GetMapping("/event/register")
	public ResponseEntity<ResultDto> registerListener(
			@Parameter(description = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.registerEventListener(channelName));

	}

//	@Operation(summary = "Hyperledger Fabric 채널 이벤트 리스너 삭제", description = "Hyperledger Fabric 채널 이벤트 리스너 삭제하는 API")
//	@GetMapping("/event/unregister")
//	public ResponseEntity<ResultDto> unregisterListener(
//			@Parameter(description = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName) {
//
//		return ResponseEntity.status(HttpStatus.OK).body(fabricService.unregisterEventListener(channelName));
//
//	}

	@Operation(summary = "Hyperledger Fabric 앵커피어 업데이트", description = "Hyperledger Fabric 앵커피어 업데이트를 하는 API")
	@GetMapping("/update/anchor")
	public ResponseEntity<ResultDto> setAnchorPeer(
			@Parameter(description = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName,
			@Parameter(description = "컨테이너 이름", required = true) @RequestParam(value = "conName") String conName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.setAnchorPeer(channelName, conName));

	}

}
