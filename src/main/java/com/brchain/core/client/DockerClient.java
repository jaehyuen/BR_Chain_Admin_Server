package com.brchain.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.service.ConInfoService;
import com.brchain.core.util.ContainerSetting;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;

@Component
public class DockerClient {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ConInfoService conInfoService;

	@Autowired
	private SshClient sshClient;

	@Value("${brchain.ip}")
	private String ip;

	private String networkMode = "brchain-network";
//	final DockerClient docker = DefaultDockerClient.builder().uri("http://"+ip+":2375").apiVersion("v1.40")
//			.build();

	private final DefaultDockerClient docker = DefaultDockerClient.builder().uri("http://192.168.65.169:2375").apiVersion("v1.40")
			.build();

	/**
	 * 실행중인 컨테이너 조회 함수
	 * 
	 * @return 컨테이너 리스트
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	
	public List<Container> getRunningContainers() throws DockerException, InterruptedException {

		return docker.listContainers(ListContainersParam.allContainers());
		
	}

	
	/**
	 * 모든 컨테이너 리스트 조회 함수
	 * 
	 * @return 컨테이너 리스트 
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	
	public List<Container> getAllContainers() throws DockerException, InterruptedException {

		return docker.listContainers(ListContainersParam.allContainers());
		
	}
	
	
	/**
	 * 컨테이너 정지 및 삭제 함수
	 * 
	 * @param conId 삭제할 컨테이너 id
	 * 
	 * @return
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	
	public String removeContainer(String conId) throws DockerException, InterruptedException {
		
		docker.stopContainer(conId, 1);
		docker.removeContainer(conId);
	
		return "";
		
	}


	/**
	 * 컨테이너 생성 함수
	 * 
	 * @param createConDto 컨테이너 생성 관련 DTO
	 * 
	 * @return 공백
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * 
	 */

	public String createCon(ConInfoDto createConDto) throws DockerException, InterruptedException {

		// 도커 네트워크 체크로직
		// 네트워크 조회 에러시 네트워크 생성
		try {

			docker.inspectNetwork(networkMode);

		} catch (NetworkNotFoundException e) {

			NetworkConfig networkConfig = NetworkConfig.builder().checkDuplicate(true).attachable(true)
					.name(networkMode).build();
			docker.createNetwork(networkConfig);

		}

		ContainerSetting containerSetting;

		// 컨테이너 설정 객체 생성
		if (createConDto.getConType().equals("ca") || createConDto.getConType().contains("setup")) {

			containerSetting = new ContainerSetting(createConDto.getOrgName(), createConDto.getConType(),
					createConDto.getConPort(), createConDto.getConCnt());

		} else if (createConDto.getConType().equals("couchdb")) {

			containerSetting = new ContainerSetting(createConDto.getOrgName(), createConDto.getConType(), "",
					createConDto.getConNum());

		} else {

			containerSetting = new ContainerSetting(createConDto.getOrgName(), createConDto.getConType(),
					createConDto.getConPort(), createConDto.getConNum());

		}

		// 컨테이너명 설정
		String containerName = containerSetting.getContainerName();

		// 볼륨설정
		List<String> binds = containerSetting.setBinds();

		// 로깅설정
		LogConfig logconfig = LogConfig.create("none");

		// 포트 오픈설정
		String[] ports;

		if (createConDto.getConType().contains("setup") || createConDto.getConType().contains("couchdb")) {

			ports = new String[] {};

		} else {

			ports = new String[] { containerSetting.getPort() };
		}

		Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String portBind : ports) {
			List<PortBinding> hostPorts = new ArrayList<>();
			hostPorts.add(PortBinding.of("0.0.0.0", portBind));
			portBindings.put(portBind, hostPorts);
		}

		// extra hosts 설정 지금은 사용안함
//		List<String> extraHosts = containerenv.setExtraHosts();

		// hostconfig 빌더 생성
		HostConfig hostConfig = HostConfig.builder().binds(binds)
//				.logConfig(logconfig)
				.networkMode(networkMode).portBindings(portBindings)
//				.extraHosts(extraHosts)
				.build();

		// 포트 오픈 설정
		Set<String> exposedPorts = containerSetting.setExposedPort(ports);

		// 환경변수 설정
		List<String> containerEnv;

		if (createConDto.getConType().equals("peer")) {

			containerEnv = containerSetting.setContainerEnv(createConDto.getGossipBootAddress(),
					createConDto.isCouchdbYn());

		} else if (createConDto.getConType().equals("setup_channel")) {

			containerEnv = containerSetting.setContainerEnv(createConDto.getAnchorPeerSetting(), false);
			containerEnv.add("PEER_ORGS=" + createConDto.getPeerOrgs());

		} else if (createConDto.getConType().equals("setup_orderer")) {

			containerEnv = containerSetting.setContainerEnv(createConDto.getOrdererPorts(), false);
			containerEnv.add("PEER_ORGS=" + createConDto.getPeerOrgs());

		}

		else {

			containerEnv = containerSetting.setContainerEnv(createConDto.getOrdererPorts(), false);

		}

		// cmd 설정
		List<String> cmd = containerSetting.setCmd();

		// 볼륨 설정
		Map<String, Map> volumes = containerSetting.setVolumes();

		// 라벨 설정
		Map<String, String> labels = new HashMap<String, String>();

		ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).exposedPorts(exposedPorts)
				.env(containerEnv).cmd(cmd)
//				.volumes(volumes)
				.image(containerSetting.setImages()).labels(labels).domainname(containerName).build();

		// 컨테이너 생성
		ContainerCreation creation = docker.createContainer(containerConfig);

		String id = creation.id();

		ContainerInfo info = docker.inspectContainer(id);

		info.name();

		// 컨테이너 시작 및 이름 변경
		docker.startContainer(id);
		docker.renameContainer(id, containerName);

		ConInfoDto conInfoDto = new ConInfoDto();

		conInfoDto.setConId(id);
		conInfoDto.setConName(containerName);
		conInfoDto.setConType(createConDto.getConType());
		conInfoDto.setOrgName(createConDto.getOrgName());
		conInfoDto.setOrgType(createConDto.getOrgType());
		conInfoDto.setConPort(createConDto.getConPort());
		conInfoDto.setConsoOrgs(createConDto.getPeerOrgs());

		// 컨테이너 설정 객체 생성
		if (createConDto.getConType().equals("ca") || createConDto.getConType().contains("setup")) {

			conInfoDto.setConCnt(createConDto.getConCnt());

		} else if (createConDto.getConType().equals("couchdb")) {

			conInfoDto.setConNum(createConDto.getConNum());

		} else {

			conInfoDto.setConNum(createConDto.getConNum());
			conInfoDto.setGossipBootAddress(createConDto.getGossipBootAddress());
			conInfoDto.setCouchdbYn(createConDto.isCouchdbYn());

		}
		if (createConDto.getConType().equals("setup_orderer")) {

			conInfoDto.setOrdererPorts(createConDto.getOrdererPorts());

		}

		conInfoService.saveConInfo(conInfoDto);
		logger.info("[도커 컨테이너 생성 dto] " + conInfoDto);

		return "";
	}

	

}
