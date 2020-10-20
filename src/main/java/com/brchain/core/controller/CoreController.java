package com.brchain.core.controller;

import java.util.concurrent.CopyOnWriteArrayList;

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
import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.service.CcInfoService;
import com.brchain.core.service.ChannelInfoService;
import com.brchain.core.service.ConInfoService;
import com.brchain.core.service.DockerService;
import com.brchain.core.service.FabricService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/")
public class CoreController {

	@Autowired
	DockerService dockerService;

	@Autowired
	ConInfoService conInfoService;

	@Autowired
	ChannelInfoService channelInfoService;
	
	@Autowired
	CcInfoService ccInfoService;

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
	
	@GetMapping("/members")
	public ResponseEntity<ResultDto> getMemberList(@RequestParam(value = "orgName") String orgName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(conInfoService.getMemberList(orgName));

	}
	

	@GetMapping("/remove")
	public ResponseEntity<ResultDto> removeContainer(@RequestParam(value = "conId", required = false) String conId,
			@RequestParam(value = "orgName", required = false) String orgName) {

		if (conId != null && conId.equals("")) {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeAllContainers());

		} else if (conId != null) {
			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeContainer(conId));

		} else {

			return ResponseEntity.status(HttpStatus.OK).body(dockerService.removeOrgContainers(orgName));
		}

	}

	@GetMapping("/check/port")
	public ResponseEntity<ResultDto> portCheck(@RequestParam(value = "port") String port) {

		return ResponseEntity.status(HttpStatus.OK).body(conInfoService.checkConPort(port));

	}


}
