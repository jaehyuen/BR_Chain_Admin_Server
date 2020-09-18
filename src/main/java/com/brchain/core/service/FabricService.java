package com.brchain.core.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.brchain.core.client.DockerClient;
import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.ChannelInfoDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;

@Service
public class FabricService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DockerClient dockerClient;

	@Autowired
	private ConInfoService conInfoService;

	@Autowired
	private Environment environment;

	@Autowired
	private FabricClient fabricClient;

	@Autowired
	private SshClient sshClient;

	
	/**
	 * 조직 생성 서비스
	 * 
	 * @param conInfoDtoArr 컨테이너 관련 DTO
	 * @return
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws SftpException
	 * @throws IOException
	 * @throws JSchException
	 */

	public String createOrg(CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr)
			throws DockerException, InterruptedException, SftpException, IOException, JSchException {
		
		String ordererPorts = "";
		String gossipBootAddress = "";

		// 컨테이너 생성시 필요한 변수 선언
		for (ConInfoDto dto : conInfoDtoArr) {

			if (dto.getConType().equals("orderer")) {

				ordererPorts = ordererPorts + dto.getConPort() + " ";

			}

			if (dto.getConType().equals("peer")) {

				gossipBootAddress = gossipBootAddress + dto.getConType() + dto.getConNum() + ".org" + dto.getOrgName()
						+ ".com:" + dto.getConPort() + " ";
			}
		}

		int i = 0;

		for (ConInfoDto dto : conInfoDtoArr) {

			if (dto.getConType().equals("ca")) {

				// 컨테이너 생성 함수 호출
				logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + " 컨테이너 생성");
				dockerClient.createCon(dto);

				// setup 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
				ConInfoDto setupContainer = new ConInfoDto();
				setupContainer.setOrgName(dto.getOrgName());
				setupContainer.setOrgType(dto.getOrgType());
				setupContainer.setConPort(dto.getConPort());
				setupContainer.setConCnt(dto.getConCnt());
				setupContainer.setConType("setup_" + dto.getOrgType());

				if (dto.getOrgType().equals("orderer")) {

					setupContainer.setOrdererPorts(ordererPorts);
					setupContainer.setPeerOrgs(conInfoService.selectByConType("ca", "peer"));

				}

				Thread.sleep(5000);

				logger.info("[조직생성] 도커 컨테이너 생성 -> " + setupContainer.getOrgName() + " 조직의 "
						+ setupContainer.getConType() + " 컨테이너 생성");
				logger.info(setupContainer.toString());
				dockerClient.createCon(setupContainer);
				Thread.sleep(5000);

				conInfoDtoArr.add(i + 1, setupContainer);

			} else if (dto.getConType().equals("peer")) {

				// 컨테이너 생성 함수 호출
				dto.setGossipBootAddress(gossipBootAddress);
				logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + dto.getConNum()
						+ " 컨테이너 생성");
				logger.info(dto.toString());
				dockerClient.createCon(dto);

				// couchdb 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
				if (dto.isCouchdbYn()) {
					ConInfoDto couchdbContainer = new ConInfoDto();
					couchdbContainer.setOrgName(dto.getOrgName());
					couchdbContainer.setOrgType(dto.getOrgType());
					couchdbContainer.setConNum(dto.getConNum());
					couchdbContainer.setConType("couchdb");

					logger.info("[조직생성] 도커 컨테이너 생성 -> " + couchdbContainer.getOrgName() + " 조직의 "
							+ couchdbContainer.getConType() + couchdbContainer.getConNum() + " 컨테이너 생성");
					dockerClient.createCon(couchdbContainer);

				}
				
			} else {

				// 컨테이너 생성 함수 호출
				logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + " 컨테이너 생성");
				dto.setPeerOrgs(conInfoService.selectByConType("ca", "peer"));
				logger.info(dto.toString());
				dockerClient.createCon(dto);

			}

			Thread.sleep(2000);
			i++;
		}

		String path = null;

		// 로컬 개발시 실서버에서 생성된 인증서,트렌젝션 다운로드
		if (environment.getActiveProfiles()[0].equals("local")) {

			if (conInfoDtoArr.get(0).getOrgType().equals("peer")) {

				path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0).getOrgName()
						+ ".com/users/Admin@org" + conInfoDtoArr.get(0).getOrgName() + ".com/msp/keystore/";
				sshClient.downloadFile(path, "server.key");

				path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0).getOrgName()
						+ ".com/users/Admin@org" + conInfoDtoArr.get(0).getOrgName() + ".com/msp/signcerts/";
				sshClient.downloadFile(path, "cert.pem");

			} else {

				path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0).getOrgName()
						+ ".com/users/Admin@org" + conInfoDtoArr.get(0).getOrgName() + ".com/msp/keystore/";
				sshClient.downloadFile(path, "server.key");
				path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0).getOrgName()
						+ ".com/users/Admin@org" + conInfoDtoArr.get(0).getOrgName() + ".com/msp/signcerts/";
				sshClient.downloadFile(path, "cert.pem");

			}
			path = "crypto-config/ca-certs/";
			sshClient.downloadFile(path, "ca.org" + conInfoDtoArr.get(0).getOrgName() + ".com-cert.pem");
		}

		return "";
	}

	
	/**
	 * 채널 생성 서비스
	 * 
	 * @param channelInfoDto 채널 관련 DTO
	 * @return
	 * @throws Exception
	 */

	public String createChannel(ChannelInfoDto channelInfoDto) throws Exception {

		logger.info("[채널생성] " + channelInfoDto.getChannelName());
		logger.info("[채널생성] CreateChannelVo : " + channelInfoDto);

		ConInfoDto conInfoDto = new ConInfoDto();

		String orgs = "";
		String path = "";

		for (String test : channelInfoDto.getOrgToJoin()) {
			orgs = orgs + test + " ";
		}

		// 채널생성 setup 컨테이너 기동
		conInfoDto.setOrgName(channelInfoDto.getChannelName());
		conInfoDto.setPeerOrgs(orgs);
		conInfoDto.setOrgType(channelInfoDto.getChannelName());
		conInfoDto.setConType("setup_channel");
		conInfoDto.setConPort("");
		conInfoDto.setConCnt(0);
		logger.info("[채널생성] conInfoDto : " + conInfoDto);
		dockerClient.createCon(conInfoDto);

		// 채널 생성시 필요한 fabricMemvber(peer, orderer) Dto 생성
		ArrayList<FabricMemberDto> peerDtoArr = new ArrayList<FabricMemberDto>();

		for (int i = 0; i < channelInfoDto.getOrgToJoin().length; i++) {

			peerDtoArr.addAll(conInfoService.createMemberDtoArr("peer", channelInfoDto.getOrgToJoin()[i]));

		}
		ArrayList<FabricMemberDto> ordererDtoArr = conInfoService.createMemberDtoArr("orderer",
				channelInfoDto.getOrdererToJoin());

		logger.info("[채널생성] peerDtoArr : " + peerDtoArr);
		logger.info("[채널생성] ordererDtoArr : " + ordererDtoArr);

		// 채널 생성시 필요한 wallet 생성
		fabricClient.createWallet(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())));

		for (FabricMemberDto peerDto : peerDtoArr) {

			fabricClient.createWallet(peerDto);
		}

		fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
		Thread.sleep(100);
		// 로컬 개발시 채널생성 setup 컨테이너 기동하면서 생성된 채널트렌젝션 다운로드
		if (environment.getActiveProfiles()[0].equals("local")) {

			path = "channel-artifacts/" + conInfoDto.getOrgName() + "/";
			sshClient.downloadFile(path, conInfoDto.getOrgName() + ".tx");

		}

		// 채널 생성 함수 시작
		logger.info("[채널생성] 시작");
		fabricClient.createChannel(peerDtoArr,
				ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelInfoDto.getChannelName());
		logger.info("[채널생성] 종료");

		logger.info("[채널가입] 시작");

		joinChannel(peerDtoArr, ordererDtoArr, channelInfoDto.getChannelName());

		logger.info("[채널가입] 종료");

		return "";

	}

	
	/**
	 * 채널 가입 서비스
	 * 
	 * @param peerDtoArr    가입할 피어 관련 DTO 배열
	 * @param ordererDtoArr 오더러 관련 DTO 배열
	 * @param channelName   채널명
	 * @return
	 * @throws InvalidArgumentException
	 * @throws ProposalException
	 * @throws CryptoException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */

	public String joinChannel(ArrayList<FabricMemberDto> peerDtoArr, ArrayList<FabricMemberDto> ordererDtoArr,
			String channelName) throws InvalidArgumentException, ProposalException, CryptoException,
			ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
			InvocationTargetException, IOException {

		for (FabricMemberDto peerDto : peerDtoArr) {

			HFClient client = fabricClient.createClient(peerDto);
			fabricClient.joinChannel(client, peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					channelName);

		}

		return "";
	}


}
