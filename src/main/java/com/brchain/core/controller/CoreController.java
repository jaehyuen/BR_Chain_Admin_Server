package com.brchain.core.controller;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.channel.CreateChannelDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.service.ContainerService;
import com.brchain.core.service.DockerService;
import com.brchain.core.service.FabricService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/core/")
@RequiredArgsConstructor
public class CoreController {

	private final DockerService dockerService;
	private final ContainerService containerService;
	private final FabricService fabricService;
	
		
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

		return ResponseEntity.status(HttpStatus.OK).body(containerService.getOrgList(orgType));

	}
	
	@GetMapping("/members")
	public ResponseEntity<ResultDto> getMemberList(@RequestParam(value = "orgName") String orgName) {
		
		return ResponseEntity.status(HttpStatus.OK).body(containerService.getMemberList(orgName));

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

		return ResponseEntity.status(HttpStatus.OK).body(containerService.canUseConPort(port));

	}

//	@GetMapping("/test/config")
//	public ResponseEntity<ResultDto> configTest(@RequestParam(value = "channelName") String channelName) {
//
//		return ResponseEntity.status(HttpStatus.OK).body(fabricService.configTest(channelName));
//
//	}


}
