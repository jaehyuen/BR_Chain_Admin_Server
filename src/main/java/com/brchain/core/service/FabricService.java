package com.brchain.core.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import com.brchain.core.dto.CcInfoChannelDto;
import com.brchain.core.dto.CcInfoPeerDto;
import com.brchain.core.dto.ChannelHandleDto;
import com.brchain.core.dto.ChannelInfoDto;
import com.brchain.core.dto.ChannelInfoPeerDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.CreateChannelDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.InstallCcDto;
import com.brchain.core.dto.InstantiateCcDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;
import com.brchain.core.util.Util;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FabricService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DockerClient dockerClient;

	@Autowired
	private ContainerService containerService;

	@Autowired
	private ChaincodeService chaincodeService;

	@Autowired
	private ChannelService channelService;

	@Autowired
	private Environment environment;

	@Autowired
	private FabricClient fabricClient;

	@Autowired
	private SshClient sshClient;

	@Autowired
	private Util util;

	/**
	 * 조직 생성 서비스
	 * 
	 * @param conInfoDtoArr 컨테이너 관련 DTO
	 * 
	 * @return 결과 DTO(조직생성 결과)
	 * 
	 */

	public ResultDto createOrg(CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr) {

		logger.info("[조직생성] 시작");

		try {
			String ordererPorts = "";
			String gossipBootAddress = "";

			// 컨테이너 생성시 필요한 변수 선언
			for (ConInfoDto dto : conInfoDtoArr) {

				if (dto.getConType().equals("orderer")) {

					ordererPorts = ordererPorts + dto.getConPort() + " ";

				}

				if (dto.getConType().equals("peer")) {

					gossipBootAddress = gossipBootAddress + dto.getConType() + dto.getConNum() + ".org"
							+ dto.getOrgName() + ".com:" + dto.getConPort() + " ";
				}
			}

			int i = 0;

			for (ConInfoDto dto : conInfoDtoArr) {

				if (dto.getConType().equals("ca")) {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + " 컨테이너 생성");
					dockerClient.createContainer(dto);

					// setup 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					ConInfoDto setupContainer = new ConInfoDto();
					setupContainer.setOrgName(dto.getOrgName());
					setupContainer.setOrgType(dto.getOrgType());
					setupContainer.setConPort(dto.getConPort());
					setupContainer.setConCnt(dto.getConCnt());
					setupContainer.setConType("setup_" + dto.getOrgType());

					if (dto.getOrgType().equals("orderer")) {

						setupContainer.setOrdererPorts(ordererPorts);
						setupContainer.setPeerOrgs(containerService.selectByConType("ca", "peer"));

					}

					Thread.sleep(5000);

					logger.info("[조직생성] 도커 컨테이너 생성 -> " + setupContainer.getOrgName() + " 조직의 "
							+ setupContainer.getConType() + " 컨테이너 생성");
					logger.info(setupContainer.toString());
					dockerClient.createContainer(setupContainer);
					Thread.sleep(5000);

					conInfoDtoArr.add(i + 1, setupContainer);

				} else if (dto.getConType().equals("peer")) {

					// 컨테이너 생성 함수 호출
					dto.setGossipBootAddress(gossipBootAddress);
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType()
							+ dto.getConNum() + " 컨테이너 생성");
					logger.info(dto.toString());
					dockerClient.createContainer(dto);

					// couchdb 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					if (dto.isCouchdbYn()) {

						ConInfoDto couchdbContainer = new ConInfoDto();
						couchdbContainer.setOrgName(dto.getOrgName());
						couchdbContainer.setOrgType(dto.getOrgType());
						couchdbContainer.setConNum(dto.getConNum());
						couchdbContainer.setConType("couchdb");

						logger.info("[조직생성] 도커 컨테이너 생성 -> " + couchdbContainer.getOrgName() + " 조직의 "
								+ couchdbContainer.getConType() + couchdbContainer.getConNum() + " 컨테이너 생성");
						dockerClient.createContainer(couchdbContainer);

					}

				} else {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + " 컨테이너 생성");
					dto.setPeerOrgs(containerService.selectByConType("ca", "peer"));
					logger.info(dto.toString());
					dockerClient.createContainer(dto);

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

			ArrayList<FabricMemberDto> fabricMemberDto = containerService
					.createMemberDtoArr(conInfoDtoArr.get(0).getOrgType(), conInfoDtoArr.get(0).getOrgName());
			fabricClient.createWallet(fabricMemberDto.get((int) (Math.random() * fabricMemberDto.size())));

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		logger.info("[조직생성] 종료");

		return util.setResult("0000", true, "Success create org", null);
	}

	/**
	 * 채널 생성 서비스
	 * 
	 * @param createChannelDto 채널 관련 DTO
	 * 
	 * @return 결과 DTO(채널 생성 결과)
	 */

	public ResultDto createChannel(CreateChannelDto createChannelDto) {

		logger.info("[채널생성] 시작");
		logger.info("[채널생성] " + createChannelDto.getChannelName());
		logger.info("[채널생성] CreateChannelVo : " + createChannelDto);

		try {

			ConInfoDto conInfoDto = new ConInfoDto();

			String orgs = "";
			String path = "";

			for (String org : createChannelDto.getPeerOrgs()) {
				orgs = orgs + org + " ";
			}

			// 채널생성 setup 컨테이너 기동
			conInfoDto.setOrgName(createChannelDto.getChannelName());
			conInfoDto.setPeerOrgs(orgs);
			conInfoDto.setOrgType(createChannelDto.getChannelName());
			conInfoDto.setConType("setup_channel");
			conInfoDto.setConPort("");
			conInfoDto.setConCnt(0);
			logger.info("[채널생성] conInfoDto : " + conInfoDto);
			dockerClient.createContainer(conInfoDto);

			// 채널 생성시 필요한 fabricMemvber(peer, orderer) Dto 생성
			ArrayList<FabricMemberDto> peerDtoArr = new ArrayList<FabricMemberDto>();

			for (int i = 0; i < createChannelDto.getPeerOrgs().length; i++) {

				peerDtoArr.addAll(containerService.createMemberDtoArr("peer", createChannelDto.getPeerOrgs()[i]));

			}
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					createChannelDto.getOrderingOrg());

			logger.info("[채널생성] peerDtoArr : " + peerDtoArr);
			logger.info("[채널생성] ordererDtoArr : " + ordererDtoArr);

			// 채널 생성시 필요한 wallet 생성
			fabricClient.createWallet(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())));

			for (FabricMemberDto peerDto : peerDtoArr) {

				fabricClient.createWallet(peerDto);
			}

			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(1000);
			// 로컬 개발시 채널생성 setup 컨테이너 기동하면서 생성된 채널트렌젝션 다운로드
			if (environment.getActiveProfiles()[0].equals("local")) {

				path = "channel-artifacts/" + conInfoDto.getOrgName() + "/";
				sshClient.downloadFile(path, conInfoDto.getOrgName() + ".tx");

			}

			// 채널 생성 함수 시작
			logger.info("[채널생성] 시작");
			fabricClient.createChannel(peerDtoArr, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					createChannelDto.getChannelName());

			ChannelInfoDto channelInfoDto = new ChannelInfoDto();
			channelInfoDto.setChannelName(createChannelDto.getChannelName());
			channelInfoDto.setOrderingOrg(createChannelDto.getOrderingOrg());
			channelInfoDto.setChannelTx(0);
			channelInfoDto.setChannelBlock(0);
			channelService.saveChannelInfo(channelInfoDto);

			logger.info("[채널생성] 종료");

			logger.info("[채널가입] 시작");

			joinChannel(peerDtoArr, ordererDtoArr, createChannelDto.getChannelName());

			logger.info("[채널가입] 종료");

			logger.info("[채널생성] 종료");

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		logger.info("[조직생성] 종료");

		return util.setResult("0000", true, "Success create channel", null);

	}

	/**
	 * 채널 가입 서비스
	 * 
	 * @param peerDtoArr    가입할 피어 관련 DTO 배열
	 * @param ordererDtoArr 오더러 관련 DTO 배열
	 * @param channelName   채널명
	 * 
	 * @return
	 * 
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

			ChannelInfoPeerDto channelInfoPeerDto = new ChannelInfoPeerDto();

			channelInfoPeerDto.setAnchorYn(false);
			channelInfoPeerDto.setChannelInfoEntity(channelService.findChannelInfoByChannelName(channelName));
			channelInfoPeerDto.setConInfoEntity(containerService.selectByConName(peerDto.getConName()).toEntity());

			channelService.saveChannelInfoPeer(channelInfoPeerDto);

		}

		return "";
	}

	/**
	 * 체인코드 설치 서비스
	 * 
	 * @param installCcDto 체인코드 설치 관련 DTO
	 * 
	 * @return 결과 DTO(체인코스 설치 결과)
	 */

	public ResultDto installChaincode(InstallCcDto installCcDto) {
		logger.info("[체인코드 설치] 시작");
		logger.info("[체인코드 설치] InstallCcDto : " + installCcDto);

		ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", installCcDto.getOrgName());

		FabricMemberDto peerDto = null;
		String ccLang = chaincodeService.getCcLang(installCcDto.getCcName());

		for (FabricMemberDto peer : peerDtoArr) {
			if (peer.getConNum() == installCcDto.getConNum()) {
				peerDto = peer;

			}
		}

		try {

			fabricClient.installChaincodeToPeer(peerDto, installCcDto.getCcName(), installCcDto.getCcVersion());

			CcInfoPeerDto ccInfoPeerDto = new CcInfoPeerDto();

			ccInfoPeerDto.setCcVersion(installCcDto.getCcVersion());
			ccInfoPeerDto.setCcInfoEntity(chaincodeService.findCcInfoByCcName(installCcDto.getCcName()));
			ccInfoPeerDto.setConInfoEntity(containerService.selectByConName(peerDto.getConName()).toEntity());

			chaincodeService.saveCcnInfoPeer(ccInfoPeerDto);

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		logger.info("[조직생성] 종료");

		return util.setResult("0000", true, "Success install chaincode", null);
	}

	/**
	 * 체인코드 인스턴스화 서비스
	 * 
	 * @param instantiateCcDto 체인코드 인스턴스화 관련 DTO
	 * 
	 * @return 결과 DTO(체인코드 인스턴스화 결과)
	 */

	public ResultDto instantiateChaincode(InstantiateCcDto instantiateCcDto) {

		logger.info("[체인코드 인스턴스화] 시작");
		logger.info("[체인코드 인스턴스화] InstantiateCcDto : " + instantiateCcDto);

		logger.info("[체인코드 인스턴스화] instantiateCcDto.getChannelName() : " + instantiateCcDto.getChannelName());

		try {

			ChannelInfoEntity channelInfo = channelService
					.findChannelInfoByChannelName(instantiateCcDto.getChannelName());

			logger.info("[체인코드 인스턴스화] channelInfo : " + channelInfo);
			ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelService
					.findChannelInfoPeerByChannelName(channelInfo);

			logger.info("[체인코드 인스턴스화] channelInfoPeerArr : " + channelInfoPeerArr);
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerArr
					.get((int) (Math.random() * channelInfoPeerArr.size())).getConInfoEntity().getOrgName());
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfo.getOrderingOrg());

			fabricClient.instantiateChaincode(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
					ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), instantiateCcDto.getChannelName(),
					instantiateCcDto.getCcName(), instantiateCcDto.getCcVersion(), instantiateCcDto.getCcLang());

			CcInfoChannelDto ccInfoChannelDto = new CcInfoChannelDto();

			ccInfoChannelDto.setCcInfoEntity(chaincodeService.findCcInfoByCcName(instantiateCcDto.getCcName()));
			ccInfoChannelDto.setChannelInfoEntity(channelInfo);
			ccInfoChannelDto.setCcVersion(instantiateCcDto.getCcVersion());

			chaincodeService.saveCcInfoChannel(ccInfoChannelDto);

			logger.info("[체인코드 인스턴스화] 종료");

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success install chaincode", null);

	}

	/**
	 * 채널 블록 이벤트 등록 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 블록 이벤트 등록 결과)
	 */

	public ResultDto registerEventListener(String channelName) {

		logger.info("[채널 블럭 이벤트 등록] 시작");
		logger.info("[채널 블럭 이벤트 등록] channelName : " + channelName);

		try {

			ChannelInfoEntity channelInfo = channelService.findChannelInfoByChannelName(channelName);

			ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelService
					.findChannelInfoPeerByChannelName(channelInfo);

			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerArr
					.get((int) (Math.random() * channelInfoPeerArr.size())).getConInfoEntity().getOrgName());
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfo.getOrderingOrg());

			String handle = fabricClient.registerEventListener(
					peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
					ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName);

			ChannelHandleDto channelHandleDto = new ChannelHandleDto();
			channelHandleDto.setChannelName(channelName);
			channelHandleDto.setHandle(handle);

			channelService.saveChannelHandle(channelHandleDto);

			logger.info("[채널 블럭 이벤트 등록] 종료");

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success Instantiate chaincode", null);
	}

	public ResultDto unregisterEventListener(String channelName) {

		try {

			ChannelInfoEntity channelInfo = channelService.findChannelInfoByChannelName(channelName);

			ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelService
					.findChannelInfoPeerByChannelName(channelInfo);

			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerArr
					.get((int) (Math.random() * channelInfoPeerArr.size())).getConInfoEntity().getOrgName());
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfo.getOrderingOrg());

			fabricClient.unregisterEventListener(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
					ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName,
					channelService.findChannelHandleByChannelInfo(channelName).getHandle());

			channelService.deleteChannelHandle(channelName);

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success Register Block EventListener", null);
	}

}
