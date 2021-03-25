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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/channel")
@RequiredArgsConstructor
public class ChannelController {

	private final ChannelService channelService;
	private final FabricService fabricService;

	@ApiOperation(value = "Hyperledger Fabric 채널 조회", notes = "Hyperledger Fabric 채널 조회하는 API")
	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChannelList(
			@ApiParam(value = "채널 이름", required = false) @RequestParam(value = "channelName", required = false) String channelName) {

		if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelList());
		}

	}

	@ApiOperation(value = "Hyperledger Fabric 채널에 가입된 컨테이너 조회", notes = "컨테이너 이름 및 채널명으로 Hyperledger Fabric 채널에 가입된 컨테이너를 조회하는 API")
	@GetMapping("/list/peer")
	public ResponseEntity<ResultDto> getChannelListPeer(
			@ApiParam(value = "컨테이너 이름", required = false) @RequestParam(value = "conName", required = false) String conName,
			@ApiParam(value = "채널 이름", required = false) @RequestParam(value = "channelName", required = false) String channelName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelListPeerByConName(conName));
		} else if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(channelService.getChannelListPeerByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultDto());
		}

	}

	@ApiOperation(value = "Hyperledger Fabric 채널 생성 및 가입", notes = "HyperLedger Fabric 채널을 생성하고 가입하는 API")
	@PostMapping("/create")
	public ResponseEntity<ResultDto> createChannel(
			@ApiParam(value = "채널 생성 관련 DTO", required = true) @RequestBody CreateChannelDto createChannelDto) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createChannel(createChannelDto));

	}

	@ApiOperation(value = "Hyperledger Fabric 채널 이벤트 리스너 등록", notes = "Hyperledger Fabric 채널 이벤트 리스너 등록하는 API")
	@GetMapping("/event/register")
	public ResponseEntity<ResultDto> registerListener(
			@ApiParam(value = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.registerEventListener(channelName));

	}

	@ApiOperation(value = "Hyperledger Fabric 채널 이벤트 리스너 삭제", notes = "Hyperledger Fabric 채널 이벤트 리스너 삭제하는 API")
	@GetMapping("/event/unregister")
	public ResponseEntity<ResultDto> unregisterListener(
			@ApiParam(value = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.unregisterEventListener(channelName));

	}

	@ApiOperation(value = "Hyperledger Fabric 앵커피어 업데이트", notes = "Hyperledger Fabric 앵커피어 업데이트를 하는 API")
	@GetMapping("/update/anchor")
	public ResponseEntity<ResultDto> setAnchorPeer(
			@ApiParam(value = "채널 이름", required = true) @RequestParam(value = "channelName") String channelName,
			@ApiParam(value = "컨테이너 이름", required = true) @RequestParam(value = "conName") String conName) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.setAnchorPeer(channelName, conName));

	}

}
