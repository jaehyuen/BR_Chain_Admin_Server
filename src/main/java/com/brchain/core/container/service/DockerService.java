package com.brchain.core.container.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.chaincode.service.ChaincodeService;
import com.brchain.core.channel.service.ChannelService;
import com.brchain.core.client.DockerClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.DockerStatsDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.JsonUtil;
import com.brchain.core.util.Util;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;
import com.spotify.docker.client.messages.ContainerInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DockerService {

	// 클라이언트
	private final DockerClient     dockerClient;
	private final SshClient        sshClient;

	// 서비스
	private final ContainerService containerService;
	private final ChannelService channelService;
	private final ChaincodeService chaincodeService;
	

	private final Util             util;
	private final JsonUtil         jsonUtil;

	private Logger                 logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 * 모든 컨테이너 삭제 서비스
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto<String> removeAllContainers() {

//		try {

		List<Container> containers = dockerClient.loadAllContainers();

		for (Iterator<Container> iter = containers.iterator(); iter.hasNext();) {

			Container  container  = iter.next();
			
			removeContainer(container.id());

//			ConInfoEntity conInfoEntity = null;
//
//			try {
//				conInfoEntity = containerService.deleteConInfo(container.id());
//			} catch (Exception e) {
//				logger.info("디비에 없는 컨테이너");
//			}
//
//			logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
//			dockerClient.removeContainer(container.id());
//
//			if (conInfoEntity != null) {
//				sshClient.removeDir(conInfoEntity.getOrgName(), conInfoEntity.getConName(), conInfoEntity.getConType());
//			}

		}

//		} catch (DockerException e) {
//			// 도커 관련 오류
//			logger.error(e.getMessage());
//			throw new BrchainException(e.getMessage(), e);
//
//		} catch (InterruptedException e) {
//			// 쓰레드 관련 오류
//			logger.error(e.getMessage());
//			throw new BrchainException(e.getMessage(), e);
//
//		} catch (JSchException e) {
//			// jsch 라이브러리 관련 오류
//			logger.error(e.getMessage());
//			throw new BrchainException(e.getMessage(), e);
//		}

		
		// Success remove container
		return util.setResult(BrchainStatusCode.SUCCESS, "Success remove container");

	}

	/**
	 * 
	 * 특정 컨테이너 삭제 서비스
	 * 
	 * @param conId 컨테이너 ID
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto<String> removeContainer(String conId) {

		ConInfoEntity conInfoEntity = null;
		logger.info("[컨테이너 삭제] 컨테이너 id : " + conId);
		dockerClient.removeContainer(conId);
		try {

			//삭제할 컨테이너 정보 조회
			conInfoEntity = containerService.findConInfoByConId(conId);
			
			//체인코드 정보(피어) 삭제
			chaincodeService.deleteCcInfoPeer(conInfoEntity.getConName());
			
			//채널 정보(피어) 삭제 
			channelService.deleteChannelInfoPeer(conInfoEntity.getConName());
			
			conInfoEntity = containerService.deleteConInfo(conId);
			
			sshClient.removeDir(conInfoEntity.getOrgName(), conInfoEntity.getConName(), conInfoEntity.getConType());

		} catch (IllegalArgumentException e) {

			logger.info("디비에 없는 컨테이너");

		}


		

		// Success remove container
		return util.setResult(BrchainStatusCode.SUCCESS, "Success remove container");
		

	}

	/**
	 * 조직 컨테이너 삭제 서비스
	 * 
	 * @param orgName 조직 이름
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto<String> removeOrgContainers(String orgName) {

		List<ConInfoEntity> conInfoList = containerService.findConInfoByOrgName(orgName);

		for (ConInfoEntity conInfo : conInfoList) {
			removeContainer(conInfo.getConId());
		}


		// Success remove org
		return util.setResult(BrchainStatusCode.SUCCESS, "Success remove org");

	}

	/**
	 * 컨테이너 리스트 조회 서비스
	 * 
	 * @return 결과 DTO(조회 결과)
	 */

	public ResultDto<List<DockerStatsDto>> getAllContainersInfo() {

		List<DockerStatsDto> dockerStatsList = new ArrayList<DockerStatsDto>();

		List<Container>      containerList   = dockerClient.loadAllContainers();

		for (Container container : containerList) {

			DockerStatsDto             dockerStatsDto = new DockerStatsDto();
			ImmutableList<PortMapping> portList       = container.ports();

			for (PortMapping port : portList) {

				if (!port.publicPort()
					.equals(0)) {

					dockerStatsDto.setConPort(port.publicPort());

				}

			}

			dockerStatsDto.setConId(container.id());
			dockerStatsDto.setConName(container.names().get(0));
			dockerStatsDto.setConCreated(new Date(container.created() * 1000).toString());
			dockerStatsDto.setConStatus(container.status());

			dockerStatsList.add(dockerStatsDto);

		}

		// Success get all containers info
		return util.setResult(BrchainStatusCode.SUCCESS, dockerStatsList);
	}

	/**
	 * 컨테이너 생성 함수
	 * 
	 * @param createConDto 컨테이너 정보 DTO
	 * 
	 * @return 생성된 컨테이너 정보 JSON
	 * 
	 */

	public JSONObject createContainer(ConInfoDto createConDto) {

		ContainerInfo info = dockerClient.createContainer(createConDto);

		createConDto.setConId(info.id());
		createConDto.setConName(info.name().replace("/", ""));

		logger.info("[도커 컨테이너 생성 dto] " + createConDto);

		containerService.saveConInfo(util.toEntity(createConDto));

		return jsonUtil.createConJson(info);

	}
	public ResultDto<String> rebootContainer(String conId){
		dockerClient.rebootContainer(conId);
		return util.setResult(BrchainStatusCode.SUCCESS, "Success remove org");
	}

}
