package com.brchain.core.controller;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.ChannelInfoDto;
import com.brchain.core.dto.ConInfoDto;
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
	FabricService fabricService;

	@Autowired
	Environment environment;

	@Autowired
	FabricClient fabricClient;

	@Autowired
	SshClient sshClient;

	ConInfoDto conInfoDto;

	@GetMapping("/container")
	public String getContainerInfo() throws DockerException, InterruptedException, JSchException {

		logger.info("[컨테이너 조회] 시작");
		dockerService.getContainerInfo();
		logger.info("[컨테이너 조회] 종료");

		return "";

	}

	@PostMapping("/create/org")
	public String createContainer(@RequestBody CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr)
			throws DockerException, InterruptedException, SftpException, IOException, JSchException {

		logger.info("[조직생성] 시작");
		fabricService.createOrg(conInfoDtoArr);
		logger.info("[조직생성] 종료");

		return "";

	}

	@GetMapping("/remove")
	public String removeContainer()
			throws DockerException, InterruptedException, JSchException, SftpException, IOException {

		logger.info("[컨테이너 삭제] 시작");
		dockerService.removeAllContainers();
		logger.info("[컨테이너 삭제] 종료");

		return "";

	}

	@PostMapping("/create/channel")
	public String channelTest(@RequestBody ChannelInfoDto channelInfoDto) throws Exception {

		logger.info("[채널생성] 시작");
		fabricService.createChannel(channelInfoDto);
		logger.info("[채널생성] 종료");

		return "";

	}

}
