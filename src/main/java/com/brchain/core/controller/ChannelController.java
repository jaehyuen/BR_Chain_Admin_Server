package com.brchain.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.channel.CreateChannelDto;
import com.brchain.core.service.ChannelService;
import com.brchain.core.service.FabricService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/channel")
@RequiredArgsConstructor
public class ChannelController {

	private final ChannelService channelService;
	private final FabricService fabricService;

	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChannelList(@RequestParam(value = "channelName", required = false) String channelName) {

		if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelList());
		}

	}
	
	@GetMapping("/list/peer")
	public ResponseEntity<ResultDto> getChannelListPeer(@RequestParam(value = "conName", required = false) String conName,
			@RequestParam(value = "channelName", required = false) String channelName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelService.getChannelListPeerByConName(conName));
		} else if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(channelService.getChannelListPeerByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultDto());
		}

	}
	

	@PostMapping("/create")
	public ResponseEntity<ResultDto> createChannel(@RequestBody CreateChannelDto createChannelDto) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createChannel(createChannelDto));

	}
	
	@GetMapping("/register")
	public ResponseEntity<ResultDto> registerListener(@RequestParam(value = "channelName") String channelName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.registerEventListener(channelName));

	}
	@GetMapping("/unregister")
	public ResponseEntity<ResultDto> unregisterListener(@RequestParam(value = "channelName") String channelName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.unregisterEventListener(channelName));

	}
	
	@GetMapping("/anchor")
	public ResponseEntity<ResultDto> setAnchorPeer(@RequestParam(value = "channelName") String channelName,@RequestParam(value = "conName") String conName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.setAnchorPeer(channelName,conName));

	}

}
