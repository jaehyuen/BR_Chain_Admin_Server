package com.brchain.core.controller;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.CreateChannelDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.service.ChannelInfoService;
import com.brchain.core.service.ConInfoService;
import com.brchain.core.service.DockerService;
import com.brchain.core.service.FabricService;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spotify.docker.client.exceptions.DockerException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/")
public class CoreController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	DockerService dockerService;

	@Autowired
	ConInfoService conInfoService;
	
	@Autowired
	ChannelInfoService channelInfoService;

	@Autowired
	FabricService fabricService;

	@Autowired
	Environment environment;

	@Autowired
	FabricClient fabricClient;

	@Autowired
	SshClient sshClient;

	ConInfoDto conInfoDto;

	@GetMapping("/containers")
	public ResponseEntity<ResultDto> getContainerInfo() {

		return ResponseEntity.status(HttpStatus.OK).body(dockerService.getAllContainersInfo());

	}

	@PostMapping("/create/org")
	public ResponseEntity<ResultDto> createContainer(@RequestBody CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr) {

		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createOrg(conInfoDtoArr));

	}

	@GetMapping("/orgs")
	public ResponseEntity<ResultDto> getOrgList(@RequestParam(value = "type") String orgType) {

		return ResponseEntity.status(HttpStatus.OK).body(conInfoService.getOrgList(orgType));

	}

	@GetMapping("/remove")
	public ResponseEntity<ResultDto> removeContainer(@RequestParam(value = "conId") String conId) {

		if (conId.equals("")) {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeAllContainers());

		} else {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeContainer(conId));

		}

	}

	@GetMapping("/check/port")
	public ResponseEntity<ResultDto> portCheck(@RequestParam(value = "port") String port) {

		return ResponseEntity.status(HttpStatus.OK).body(conInfoService.checkConPort(port));

	}

	
	@GetMapping("/channels")
	public ResponseEntity<ResultDto> getChannelList() {

		return ResponseEntity.status(HttpStatus.OK).body(channelInfoService.getChannelList());

	}
	@PostMapping("/create/channel")
	public ResponseEntity<ResultDto> channelTest(@RequestBody CreateChannelDto createChannelDto){

		
		return ResponseEntity.status(HttpStatus.OK).body(fabricService.createChannel(createChannelDto));
		

	}

}
