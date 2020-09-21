package com.brchain.core.service;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.brchain.core.client.DockerClient;
import com.brchain.core.client.SshClient;
import com.jcraft.jsch.JSchException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;

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
	
	public String removeAllContainers() throws DockerException, InterruptedException, JSchException {

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

			logger.info("[컨테이너 삭제] 컨테이너 이름 : "+container.names().get(0));
			dockerClient.removeContainer(container.id());
			sshClient.removeDir(orgName);

		}
		
		return "";

	}
	
	
	/**
	 * 컨테이너 리스트 조회 서비스
	 * 
	 * @return
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 */
	
	public String getContainerInfo() throws DockerException, InterruptedException {
		
		return dockerClient.getRunningContainers().toString();
	}

}
