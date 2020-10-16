package com.brchain.core.controller;

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
import com.brchain.core.dto.CreateChannelDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.service.ChannelInfoService;
import com.brchain.core.service.FabricService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/channel")
public class ChannelController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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

}
