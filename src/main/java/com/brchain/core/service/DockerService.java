package com.brchain.core.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brchain.core.client.DockerClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.ResultDto;
import com.google.common.collect.ImmutableList;
import com.jcraft.jsch.JSchException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;

@Service
public class DockerService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DockerClient dockerClient;

	@Autowired
	private ConInfoService conInfoService;

	@Autowired
	private SshClient sshClient;

	/**
	 * 모든컨테이너 삭제 서비스
	 * 
	 * @return
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws JSchException
	 */

	public ResultDto removeAllContainers() {

		ResultDto resultDto = new ResultDto();

		try {
			List<Container> containers = dockerClient.getAllContainers();
			for (Iterator<Container> iter = containers.iterator(); iter.hasNext();) {

				Container container = iter.next();

				String orgName = "";
				String path = "";

				try {
					orgName = conInfoService.removeConInfo(container.id());
				} catch (Exception e) {
					logger.info("디비에 없는 컨테이너");
				}

				logger.info("[컨테이너 삭제] 컨테이너 id : " + container.id());
				dockerClient.removeContainer(container.id());
				sshClient.removeDir(orgName);

			}

		} catch (Exception e) {
			resultDto.setResultCode("9999");
			resultDto.setResultFlag(false);
			resultDto.setResultMessage(e.getMessage());
			return resultDto;
		}

		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success create org");

		return resultDto;

	}

	public ResultDto removeContainer(String conId) {

		ResultDto resultDto = new ResultDto();
		try {

			String orgName = "";
			String path = "";

			try {
				
				orgName = conInfoService.removeConInfo(conId);
				
			} catch (Exception e) {
				
				logger.info("디비에 없는 컨테이너");
			}

			logger.info("[컨테이너 삭제] 컨테이너 id : " + conId);
			dockerClient.removeContainer(conId);
			sshClient.removeDir(orgName);
			
		} catch (Exception e) {
			
			resultDto.setResultCode("9999");
			resultDto.setResultFlag(false);
			resultDto.setResultMessage(e.getMessage());
			return resultDto;
		}

		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success create org");

		return resultDto;

	}

	/**
	 * 컨테이너 리스트 조회 서비스
	 * 
	 * @return
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */

	public ResultDto getAllContainersInfo() {

		JSONArray resultJsonArr = new JSONArray();
		ResultDto resultDto = new ResultDto();

		try {
			List<Container> containerList = dockerClient.getAllContainers();

			for (Container container : containerList) {
				container.id();

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

			resultDto.setResultCode("9999");
			resultDto.setResultFlag(false);
			resultDto.setResultMessage(e.getMessage());
			return resultDto;

		}

		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success get container info");
		resultDto.setResultData(resultJsonArr);

		return resultDto;
	}

}
