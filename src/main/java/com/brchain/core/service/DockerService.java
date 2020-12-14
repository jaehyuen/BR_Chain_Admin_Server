package com.brchain.core.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brchain.core.client.DockerClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.util.Util;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DockerService {

	private final DockerClient dockerClient;
	private final SshClient sshClient;

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

				ConInfoEntity conInfoEntity = null;

				try {
					conInfoEntity = containerService.deleteConInfo(container.id());
				} catch (Exception e) {
					logger.info("디비에 없는 컨테이너");
				}

				logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
				dockerClient.removeContainer(container.id());

				if (conInfoEntity != null) {
					sshClient.removeDir(conInfoEntity.getOrgName(), conInfoEntity.getConName());
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

			ConInfoEntity conInfoEntity = null;

			try {

				conInfoEntity = containerService.deleteConInfo(conId);

			} catch (Exception e) {

				logger.info("디비에 없는 컨테이너");

			}

			logger.info("[컨테이너 삭제] 컨테이너 id : " + conId);
			dockerClient.removeContainer(conId);
			sshClient.removeDir(conInfoEntity.getOrgName(), conInfoEntity.getConName());

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

				ConInfoEntity conInfoEntity = null;

				if (container.names().get(0).contains(orgName)) {
					try {
						conInfoEntity = containerService.deleteConInfo(container.id());
					} catch (Exception e) {
						logger.info("디비에 없는 컨테이너");
					}

					logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
					dockerClient.removeContainer(container.id());
					sshClient.removeDir(conInfoEntity.getOrgName(), conInfoEntity.getConName());

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

		JSONArray resultJsonArr = new JSONArray();

		try {

			List<Container> containerList = dockerClient.loadAllContainers();

			for (Container container : containerList) {

				JSONObject resultJson = new JSONObject();
				ImmutableList<PortMapping> portList = container.ports();

				for (PortMapping port : portList) {

					if (!port.publicPort().equals(0)) {

						resultJson.put("conPort", port.publicPort());

					}

				}

				resultJson.put("conId", container.id());
				resultJson.put("conName", container.names().get(0));
				resultJson.put("conCreated", new Date(container.created() * 1000).toString());
				resultJson.put("conStatus", container.status());

				resultJsonArr.add(resultJson);

			}

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success get all containers info", resultJsonArr);
	}

	/**
	 * conDto 샘성 함수
	 * 
	 * @param createConDto
	 * 
	 * @return 생성한 conDto
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */

	public ConInfoDto createContainer(ConInfoDto createConDto) throws DockerException, InterruptedException {

		return dockerClient.createContainer(createConDto);

	}
}
