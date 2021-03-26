package com.brchain.core.container.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.client.DockerClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.DockerStatsDto;
import com.brchain.core.util.Util;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;
import com.spotify.docker.client.messages.ContainerInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DockerService {

	// 클라이언트
	private final DockerClient dockerClient;
	private final SshClient sshClient;

	// 서비스
	private final ContainerService containerService;

	private final Util util;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 모든 컨테이너 삭제 서비스
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto removeAllContainers() {

		try {

			List<Container> containers = dockerClient.loadAllContainers();

			for (Iterator<Container> iter = containers.iterator(); iter.hasNext();) {

				Container container = iter.next();

				ConInfoDto conInfoDto = null;

				try {
					conInfoDto = containerService.deleteConInfo(container.id());
				} catch (Exception e) {
					logger.info("디비에 없는 컨테이너");
				}

				logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
				dockerClient.removeContainer(container.id());

				if (conInfoDto != null) {
					sshClient.removeDir(conInfoDto.getOrgName(), conInfoDto.getConName());
				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success remove container", null);

	}

	/**
	 * 특정 컨테이너 삭제 서비스
	 * 
	 * @param conId 컨테이너 ID
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto removeContainer(String conId) {

		try {

			ConInfoDto conInfoDto = null;

			try {

				conInfoDto = containerService.deleteConInfo(conId);

			} catch (Exception e) {

				logger.info("디비에 없는 컨테이너");

			}

			logger.info("[컨테이너 삭제] 컨테이너 id : " + conId);
			dockerClient.removeContainer(conId);
			sshClient.removeDir(conInfoDto.getOrgName(), conInfoDto.getConName());

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success remove container", null);

	}

	/**
	 * 특정 조직 컨테이너 삭제 서비스
	 * 
	 * @return 결과 DTO(삭제 결과)
	 */

	public ResultDto removeOrgContainers(String orgName) {

		try {

			List<Container> containers = dockerClient.loadAllContainers();

			for (Iterator<Container> iter = containers.iterator(); iter.hasNext();) {

				Container container = iter.next();

				ConInfoDto conInfoDto = null;

				if (container.names().get(0).contains(orgName)) {
					try {
						conInfoDto = containerService.deleteConInfo(container.id());
					} catch (Exception e) {
						logger.info("디비에 없는 컨테이너");
					}

					logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
					dockerClient.removeContainer(container.id());
					sshClient.removeDir(conInfoDto.getOrgName(), conInfoDto.getConName());

				}
			}

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success remove org", null);

	}

	/**
	 * 컨테이너 리스트 조회 서비스
	 * 
	 * @return 결과 DTO(조회 결과)
	 */

	public ResultDto getAllContainersInfo() {

		List<DockerStatsDto> dockerStatsDtoList = new ArrayList<DockerStatsDto>();

		try {

			List<Container> containerList = dockerClient.loadAllContainers();

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
				dockerStatsDto.setConName(container.names()
					.get(0));
				dockerStatsDto.setConCreated(new Date(container.created() * 1000).toString());
				dockerStatsDto.setConStatus(container.status());

				dockerStatsDtoList.add(dockerStatsDto);

			}

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success get all containers info", dockerStatsDtoList);
	}

	/**
	 * 컨테이너 생성 함수
	 * 
	 * @param createConDto 컨테이너 정보 DTO
	 * 
	 * @return 생성된 컨테이너 정보 JSON
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * 
	 */

	public JSONObject createContainer(ConInfoDto createConDto) throws DockerException, InterruptedException {

		ContainerInfo info = dockerClient.createContainer(createConDto);

		createConDto.setConId(info.id());
		createConDto.setConName(info.name().replace("/", ""));

		logger.info("[도커 컨테이너 생성 dto] " + createConDto);

		containerService.saveConInfo(createConDto);

		return util.createConJson(info);

	}

}
