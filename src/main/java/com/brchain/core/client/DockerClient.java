package com.brchain.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brchain.common.exception.BrchainException;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.container.CaContainer;
import com.brchain.core.util.container.CouchContainer;
import com.brchain.core.util.container.OrdererContainer;
import com.brchain.core.util.container.PeerContainer;
import com.brchain.core.util.container.SetupContainer;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.IpamConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DockerClient {

	private final CaContainer      caContainer;
	private final PeerContainer    peerContainer;
	private final CouchContainer   couchContainer;
	private final OrdererContainer ordererContainer;
	private final SetupContainer   setupContainer;

	@Value("${brchain.ip}")
	private String                 ip;

	@Value("${brchain.networkmode}")
	private String                 networkMode;

	private DefaultDockerClient    docker;

	@PostConstruct
	public void initDocker() {
		String url     = "http://" + ip + ":2375";
		String version = "v1.40";

		docker = DefaultDockerClient.builder()
			.uri(url)
			.apiVersion(version)
			.build();

		// 네트워크 조회 에러시 네트워크 생성
		try {

			docker.inspectNetwork(networkMode);

		} catch (NetworkNotFoundException e) {

			// 도커 네트워크 설정
			ArrayList<IpamConfig> configList = new ArrayList<IpamConfig>();

			IpamConfig            config     = IpamConfig.create("123.123.123.0/24", "123.123.123.0/24",
					"123.123.123.1");
			configList.add(config);

			Ipam          ipam          = Ipam.builder()
				.driver("default")
				.config(configList)
				.build();

			NetworkConfig networkConfig = NetworkConfig.builder()
				.checkDuplicate(true)
				.attachable(true)
				.name(networkMode)
				.ipam(ipam)
				.build();

			// 도커 네트워크 생성
			try {
				docker.createNetwork(networkConfig);
			} catch (DockerException | InterruptedException e1) {
				throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
			}

		} catch (DockerException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
		}

	}

	/**
	 * 실행중인 컨테이너 조회 함수
	 * 
	 * @return 컨테이너 리스트
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	public List<Container> loadRunningContainers() {
		try {

			return docker.listContainers(ListContainersParam.withStatusRunning());

		} catch (DockerException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
		}

	}

	/**
	 * 모든 컨테이너 리스트 조회 함수
	 * 
	 * @return 컨테이너 리스트
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */

	public List<Container> loadAllContainers() {

		try {

			return docker.listContainers(ListContainersParam.allContainers());

		} catch (DockerException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
		}

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

	public void removeContainer(String conId) {
		try {

			docker.stopContainer(conId, 1);
			docker.removeContainer(conId);

		} catch (DockerException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
		}

	}

	/**
	 * 컨테이너 생성 함수
	 * 
	 * @param createConDto 컨테이너 생성 관련 DTO
	 * 
	 * @return 생성된 컨테이너 정보
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * 
	 */

	public ContainerInfo createContainer(ConInfoDto createConDto) {

		try {

			String                                    conType           = createConDto.getConType();
			String                                    orgName           = createConDto.getOrgName();
			String                                    conPort           = createConDto.getConPort();
			String                                    gossipBootAddr    = createConDto.getGossipBootAddr();
			String                                    anchorPeerSetting = createConDto.getAnchorPeerSetting();
			String                                    ordererPorts      = createConDto.getOrdererPorts();

			int                                       conNum            = createConDto.getConNum();

			boolean                                   couchdbYn         = createConDto.isCouchdbYn();

			com.brchain.core.util.container.Container container;

			if (conType.equals("ca")) {
				container = caContainer;
			} else if (conType.equals("peer")) {
				container = peerContainer;
			} else if (conType.equals("orderer")) {
				container = ordererContainer;
			} else if (conType.equals("couchdb")) {
				container = couchContainer;
			} else {
				container = setupContainer;
			}

			container.initSetting(orgName, conType, conPort, conNum);

			// 컨테이너명 설정
			String       containerName = container.getContainerName();

			// 볼륨설정
			List<String> binds         = container.getBinds();

			// 포트 오픈설정
			String[]     ports;

			if (conType.contains("setup") || conType.contains("couchdb")) {

				ports = new String[] {};

			} else {

				ports = new String[] { container.getPort() };
			}

			Map<String, List<PortBinding>> portBindings = createPortBinding(ports);

			// hostconfig 빌더 생성
			HostConfig   hostConfig   = HostConfig.builder()
				.binds(binds)
				.networkMode(networkMode)
				.portBindings(portBindings)
				.build();

			// 포트 오픈 설정
			Set<String>  exposedPorts = container.getExposedPort(ports);

			// 환경변수 설정
			List<String> containerEnv;

			if (conType.equals("peer")) {

				containerEnv = container.getContainerEnv(gossipBootAddr, couchdbYn);

			} else if (conType.equals("setup_channel")) {

				containerEnv = container.getContainerEnv(anchorPeerSetting, couchdbYn);
				containerEnv.add("PEER_ORGS=" + createConDto.getPeerOrgs());

			} 
//			else if (conType.equals("setup_orderer")) {
//
//				containerEnv = container.getContainerEnv(ordererPorts, couchdbYn);
//				containerEnv.add("PEER_ORGS=" + createConDto.getPeerOrgs());
//
//			}
			else {
				containerEnv = container.getContainerEnv(ordererPorts, couchdbYn);
				containerEnv.add("PEER_ORGS=" + createConDto.getPeerOrgs());
			}

			// cmd 설정
			List<String>        cmd             = container.getCmd();

			// 라벨 설정
			Map<String, String> labels          = new HashMap<String, String>();

			ContainerConfig     containerConfig = ContainerConfig.builder()
				.hostConfig(hostConfig)
				.exposedPorts(exposedPorts)
				.env(containerEnv)
				.cmd(cmd)
				.image(container.getImages())
				.labels(labels)
				.domainname(containerName)
				.build();

			// 컨테이너 생성
			ContainerCreation   creation        = docker.createContainer(containerConfig);

			String              id              = creation.id();

			// 컨테이너 시작 및 이름 변경
			docker.startContainer(id);
			docker.renameContainer(id, containerName);
			ContainerInfo info = docker.inspectContainer(id);
			return info;
		} catch (DockerException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.DOCKER_CONNECTION_ERROR);
		}

	}

	private Map<String, List<PortBinding>> createPortBinding(String[] ports) {
		
		Map<String, List<PortBinding>> portBindings = new HashMap<>();
		
		for (String portBind : ports) {
			List<PortBinding> hostPorts = new ArrayList<>();
			hostPorts.add(PortBinding.of("0.0.0.0", portBind));
			portBindings.put(portBind, hostPorts);
		}

		return portBindings;
	}

}
