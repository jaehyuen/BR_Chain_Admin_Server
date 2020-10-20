package com.brchain.core.controller;

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
import com.brchain.core.dto.CreateChannelDto;
import com.brchain.core.dto.InstantiateCcDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.service.ChannelInfoService;
import com.brchain.core.service.FabricService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/channel")
public class ChannelController {

	@Autowired
	ChannelInfoService channelInfoService;

	@Autowired
	FabricService fabricService;

	@GetMapping("/list")
	public ResponseEntity<ResultDto> getChannelList(@RequestParam(value = "conName", required = false) String conName,
			@RequestParam(value = "channelName", required = false) String channelName) {

		if (conName != null) {
			return ResponseEntity.status(HttpStatus.OK).body(channelInfoService.getChannelListPeerByConName(conName));
		} else if (channelName != null) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(channelInfoService.getChannelListPeerByChannelName(channelName));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(channelInfoService.getChannelList());
		}

	}

	@PostMapping("/create")
	public ResponseEntity<ResultDto> createChannel(@RequestBody CreateChannelDto createChannelDto) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createChannel(createChannelDto));

	}
	
	@GetMapping("/register")
	public ResponseEntity<ResultDto> test(@RequestParam(value = "channelName") String channelName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.registerEventListener(channelName));

	}
	@PostMapping("/test2")
	public ResponseEntity<ResultDto> test2(@RequestBody InstantiateCcDto instantiateCcDto,@RequestParam(value = "a") String a) {
		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.registerEventListener2(instantiateCcDto,a));

	}

}
