package com.brchain.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType;
import org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.dto.BlockDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.TransactionDto;
import com.brchain.core.dto.chaincode.CcInfoChannelDto;
import com.brchain.core.dto.chaincode.CcInfoDto;
import com.brchain.core.dto.chaincode.CcInfoPeerDto;
import com.brchain.core.dto.chaincode.InstallCcDto;
import com.brchain.core.dto.chaincode.InstantiateCcDto;
import com.brchain.core.dto.channel.ChannelHandleDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.dto.channel.ChannelInfoPeerDto;
import com.brchain.core.dto.channel.CreateChannelDto;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;
import com.brchain.core.util.Util;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FabricService {

	private final FabricClient fabricClient;
	private final SshClient sshClient;

	private final ContainerService containerService;
	private final ChaincodeService chaincodeService;
	private final ChannelService channelService;
	private final DockerService dockerService;
	private final BlockService blockService;
	private final TransactionService transactionService;

	private final SimpMessagingTemplate webSocket;

	private final Environment environment;

	private final Util util;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
			JSONObject conJson = new JSONObject();
			JSONObject returnJson = new JSONObject();

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
//					containerService.saveConInfo(dockerService.createContainer(dto));
					returnJson = dockerService.createContainer(dto);
					conJson.put(returnJson.get("container_name"), returnJson);

					// setup 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					ConInfoDto setupContainer = new ConInfoDto();
					setupContainer.setOrgName(dto.getOrgName());
					setupContainer.setOrgType(dto.getOrgType());
					setupContainer.setConPort(dto.getConPort());
					setupContainer.setConCnt(dto.getConCnt());
					setupContainer.setConType("setup_" + dto.getOrgType());

					if (dto.getOrgType().equals("orderer")) {

						setupContainer.setOrdererPorts(ordererPorts);
						setupContainer.setPeerOrgs(containerService.findConInfoByConType("ca", "peer"));

					}

					Thread.sleep(5000);

					logger.info("[조직생성] 도커 컨테이너 생성 -> " + setupContainer.getOrgName() + " 조직의 "
							+ setupContainer.getConType() + " 컨테이너 생성");
					logger.info(setupContainer.toString());
					returnJson = dockerService.createContainer(setupContainer);
					conJson.put(returnJson.get("container_name"), returnJson);
//					dockerService.createContainer(setupContainer);
					Thread.sleep(5000);

					conInfoDtoArr.add(i + 1, setupContainer);

				} else if (dto.getConType().equals("peer")) {

					// 컨테이너 생성 함수 호출
					dto.setGossipBootAddr(gossipBootAddress);
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType()
							+ dto.getConNum() + " 컨테이너 생성");
					logger.info(dto.toString());
//					containerService.saveConInfo(dockerService.createContainer(dto));
					returnJson = dockerService.createContainer(dto);
					conJson.put(returnJson.get("container_name"), returnJson);

					// couchdb 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					if (dto.isCouchdbYn()) {

						ConInfoDto couchdbContainer = new ConInfoDto();
						couchdbContainer.setOrgName(dto.getOrgName());
						couchdbContainer.setOrgType(dto.getOrgType());
						couchdbContainer.setConNum(dto.getConNum());
						couchdbContainer.setConType("couchdb");

						logger.info("[조직생성] 도커 컨테이너 생성 -> " + couchdbContainer.getOrgName() + " 조직의 "
								+ couchdbContainer.getConType() + couchdbContainer.getConNum() + " 컨테이너 생성");

//						containerService.saveConInfo(dockerService.createContainer(couchdbContainer));
//						dockerService.createContainer(couchdbContainer);
						returnJson = dockerService.createContainer(couchdbContainer);
						conJson.put(returnJson.get("container_name"), returnJson);

					}

				} else {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + dto.getOrgName() + " 조직의 " + dto.getConType() + " 컨테이너 생성");
//					dto.setPeerOrgs(containerService.findConInfoByConType("ca", "peer"));
					dto.setConsoOrgs(containerService.findConInfoByConType("ca", "peer"));
					logger.info(dto.toString());

//					containerService.saveConInfo(dockerService.createContainer(dto));
					returnJson = dockerService.createContainer(dto);
					conJson.put(returnJson.get("container_name"), returnJson);

				}

				Thread.sleep(2000);
				i++;
			}

			// 생성된 조직의 docker-compose yaml file 생성
			util.createYamlFile(conInfoDtoArr.get(0).getOrgName(), conJson);

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

			logger.error(e.getMessage());
			e.printStackTrace();
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

		Util util = new Util();

		JSONObject conJson = new JSONObject();
		JSONObject returnJson = new JSONObject();

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

			returnJson = dockerService.createContainer(conInfoDto);
			conJson.put(returnJson.get("container_name"), returnJson);

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

			for (FabricMemberDto peerDto : peerDtoArr) {
				FabricMemberDto ordererDto = ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size()));

				if (!containerService.isMemOfConso(ordererDto.getOrgName(), peerDto.getOrgName())) {

					JSONObject genesisJson = fabricClient.getChannelConfig(ordererDto, "testchainid");
					JSONObject testJson = util.createOrgJson(peerDto);

					logger.info(genesisJson.toString());
					logger.info(testJson.toString());

					// 시스템 채널 컨소시움 추가

					JSONObject modifiedJson = util.modifyConsoConfig(genesisJson, testJson, "", peerDto.getOrgName());

					File updateFile = fabricClient.createUpdateFile(ordererDto, "testchainid", genesisJson,
							modifiedJson);

					fabricClient.setUpdate(ordererDto, ordererDto, "testchainid", updateFile);

					containerService.updateConsoOrgs(ordererDto.getOrgName(), peerDto.getOrgName());

				}
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
			channelInfoDto.setAppAdminPolicyType("ImplicitMeta");
			channelInfoDto.setAppAdminPolicyValue("ANY Admins");
			channelInfoDto.setChannelAdminPolicyType("ImplicitMeta");
			channelInfoDto.setChannelAdminPolicyValue("ANY Admins");
			channelInfoDto.setOrdererAdminPolicyType("ImplicitMeta");
			channelInfoDto.setOrdererAdminPolicyValue("ANY Admins");
			channelInfoDto.setBatchTimeout("1s");
			channelInfoDto.setBatchSizeAbsolMax(81920);
			channelInfoDto.setBatchSizeMaxMsg(20);
			channelInfoDto.setBatchSizePreferMax(20480);
			channelService.saveChannelInfo(channelInfoDto);

			util.createYamlFile(createChannelDto.getChannelName(), conJson);

			logger.info("[채널생성] 종료");

			logger.info("[채널가입] 시작");

			joinChannel(peerDtoArr, ordererDtoArr, createChannelDto.getChannelName());

			logger.info("[채널가입] 종료");

			logger.info("[채널생성] 종료");

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success create channel", null);

	}

	/**
	 * 채널 가입 서비스
	 * 
	 * @param peerDtoArr    가입할 피어 관련 DTO 배열
	 * @param ordererDtoArr 오더러 관련 DTO 배열
	 * @param channelName   채널명
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
	 * @throws TransactionException
	 */

	public void joinChannel(ArrayList<FabricMemberDto> peerDtoArr, ArrayList<FabricMemberDto> ordererDtoArr,
			String channelName) throws InvalidArgumentException, ProposalException, CryptoException,
			ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
			InvocationTargetException, IOException, TransactionException {

		for (FabricMemberDto peerDto : peerDtoArr) {

			// 클라이언트 생성
			HFClient client = fabricClient.createClient(peerDto);

			// 채널 조인
			fabricClient.joinChannel(client, peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					channelName);

			ChannelInfoPeerDto channelInfoPeerDto = new ChannelInfoPeerDto();

			channelInfoPeerDto.setAnchorYn(false);
			channelInfoPeerDto.setChannelInfoDto(channelService.findChannelInfoByChannelName(channelName));
			channelInfoPeerDto.setConInfoDto(containerService.findConInfoByConName(peerDto.getConName()));

			// 채널 가입한 피어정보 저장
			channelService.saveChannelInfoPeer(channelInfoPeerDto);

		}

	}

	/**
	 * 체인코드 설치 서비스 (1.4.x 버전)
	 * 
	 * @param installCcDto 체인코드 설치 관련 DTO
	 * 
	 * @return 결과 DTO(체인코스 설치 결과)
	 */

	public ResultDto installChaincode(InstallCcDto installCcDto) {
		logger.info("[체인코드 설치] 시작");
		logger.info("[체인코드 설치] InstallCcDto : " + installCcDto);

		// 체인코드를 설치할 FabricMembetDto(peer) 생성
		ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", installCcDto.getOrgName());

		FabricMemberDto peerDto = null;

		for (FabricMemberDto peer : peerDtoArr) {
			if (peer.getConNum() == installCcDto.getConNum()) {
				peerDto = peer;

			}
		}

		try {

			// 체인코드 설치
			fabricClient.installChaincode(peerDto, "test-cc", installCcDto.getCcVersion());
			fabricClient.installChaincodeToPeer(peerDto, installCcDto.getCcName(), installCcDto.getCcVersion());

			CcInfoPeerDto ccInfoPeerDto = new CcInfoPeerDto();

			// 설치한 체인코드 정보 조회
			CcInfoDto ccInfoDto = chaincodeService.findCcInfoByCcName(installCcDto.getCcName());

			// 체인코드를 설치한 컨테이너 정보 조회
			ConInfoDto conInfoDto = containerService.findConInfoByConName(peerDto.getConName());

			ccInfoPeerDto.setCcVersion(installCcDto.getCcVersion());
			ccInfoPeerDto.setCcInfoDto(ccInfoDto);
			ccInfoPeerDto.setConInfoDto(conInfoDto);

			// 체인코드 설치한 피어정보 저장
			chaincodeService.saveCcnInfoPeer(ccInfoPeerDto);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		logger.info("[체인코드 설치] 종료");

		return util.setResult("0000", true, "Success install chaincode", null);
	}

	/**
	 * 체인코드 인스턴스화 서비스 (1.4.x 버전)
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

			// 채인코드 인스턴스화를 진항항 채널 정보 조회
			ChannelInfoDto channelInfoDto = channelService
					.findChannelInfoByChannelName(instantiateCcDto.getChannelName());

			// 인스턴스화를 진행할 체인코드 정보 조회
			CcInfoDto ccInfoDto = chaincodeService.findCcInfoByCcName(instantiateCcDto.getCcName());

			logger.info("[체인코드 인스턴스화] channelInfo : " + channelInfoDto);

			// 체인코드 인스턴스화를 진행할 피어조회
			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService
					.findChannelInfoPeerByChannelInfo(channelInfoDto);

			logger.info("[체인코드 인스턴스화] channelInfoPeerDtoArr : " + channelInfoPeerDtoArr);

			// 체인코드 인스턴스화를 진행할 FabricMembetDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr
					.get((int) (Math.random() * channelInfoPeerDtoArr.size())).getConInfoDto().getOrgName());

			// 체인코드 인스턴스화를 진행할 FabricMembetDto(peer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfoDto.getOrderingOrg());

			CcInfoChannelDto ccInfoChannelDto;
			try {

				// 이미 인스턴스화가 진행된 체인코드인지 조회
				ccInfoChannelDto = chaincodeService.findCcInfoChannelByChannelInfoAndCcInfo(channelInfoDto, ccInfoDto);

				// 조회가 되면 업데이트 진행
				fabricClient.instantiateChaincode(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
						instantiateCcDto.getChannelName(), instantiateCcDto.getCcName(),
						instantiateCcDto.getCcVersion(), instantiateCcDto.getCcLang(), true);

				ccInfoChannelDto.setCcVersion(instantiateCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 업데이트
				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);

			} catch (IllegalArgumentException e) {

				// 조회가 안되면 인스턴스화 진행
				fabricClient.instantiateChaincode(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
						instantiateCcDto.getChannelName(), instantiateCcDto.getCcName(),
						instantiateCcDto.getCcVersion(), instantiateCcDto.getCcLang(), false);

				ccInfoChannelDto = new CcInfoChannelDto();
				ccInfoChannelDto.setCcInfoDto(ccInfoDto);
				ccInfoChannelDto.setChannelInfoDto(channelInfoDto);
				ccInfoChannelDto.setCcVersion(instantiateCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 저장
				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);
			}

			logger.info("[체인코드 인스턴스화] 종료");

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success instantiate chaincode", null);

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

			// 이벤트 리슨을 등록할 채널 정보 조회
			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);

			// 이벤트 리슨을 등록할 피어 정보 조회
			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService
					.findChannelInfoPeerByChannelInfo(channelInfoDto);

			// 이벤트 리슨을 등록할 FabricMembetDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr
					.get((int) (Math.random() * channelInfoPeerDtoArr.size())).getConInfoDto().getOrgName());

			// 이벤트 리슨을 등록할 FabricMembetDto(orderer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfoDto.getOrderingOrg());

//			Optional<ChannelHandleEntity> channelHandleEntity = channelService.findChannelHandleByChannel(channelName);
//			ChannelHandleDto channelHandleDto = new ChannelHandleDto();
//
//			if (channelHandleEntity.isPresent()) {
//				throw new Exception("already registered event listener");
//
//			} else {
//
//				String handle = fabricClient.registerEventListener(
//						peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
//						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName,
//						createBlockListener(channelName),
//						channelInfoDto.getChannelBlock() < 1 ? 0 : channelInfoDto.getChannelBlock() - 1);
//
//				channelHandleDto.setChannelName(channelName);
//				channelHandleDto.setHandle(handle);
//				channelService.saveChannelHandle(channelHandleDto);
//
//			}
			ChannelHandleDto channelHandleDto;
			try {

				// 이벤트 리슨이 등록된 채널인지 채널 핸들 조회
				channelHandleDto = channelService.findChannelHandleByChannel(channelName);

				// 이벤트 리슨이 등록된 채널이면 에러 발생
				throw new Exception("already registered event listener");

			} catch (IllegalArgumentException e) {

				channelHandleDto = new ChannelHandleDto();

				// 이벤트 리슨 등록
				String handle = fabricClient.registerEventListener(
						peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName,
						createBlockListener(channelName),
						channelInfoDto.getChannelBlock() < 1 ? 0 : channelInfoDto.getChannelBlock() - 1);

				channelHandleDto.setChannelName(channelName);
				channelHandleDto.setHandle(handle);

				// 채널 핸들 정보 저장
				channelService.saveChannelHandle(channelHandleDto);
			}

			logger.info("[채널 블럭 이벤트 등록] 종료");

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success Instantiate chaincode", null);
	}

	/**
	 * 채널 블록 이벤트 리스너 삭제 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 블록 이벤트 삭제 결과)
	 */

	public ResultDto unregisterEventListener(String channelName) {

		try {

			// 이벤트 리슨을 삭제할 채널 정보 조회
			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);

			// 이벤트 리슨을 삭제할 피어 정보 조회
			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService
					.findChannelInfoPeerByChannelInfo(channelInfoDto);

			// 이벤트 리슨을 삭제할 FabricMemberDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr
					.get((int) (Math.random() * channelInfoPeerDtoArr.size())).getConInfoDto().getOrgName());

			// 이벤트 리슨을 삭제할 FabricMemberDto(orderer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfoDto.getOrderingOrg());

			// 삭제할 채널 핸들 조회
			String channelHandle = channelService.findChannelHandleByChannel(channelName).getHandle();

			// 이벤트 리슨을 삭제
			fabricClient.unregisterEventListener(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
					ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName, channelHandle);

			// 채널 핸들 삭제
			channelService.deleteChannelHandle(channelName);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success Unregister Block EventListener", null);
	}

	/**
	 * 앵커피어 설정 서비스
	 * 
	 * @param channelName 채널 이름
	 * @param conName     컨테이너 이름
	 * 
	 * @return 결과 DTO(앵커피어 설정 결과)
	 */

	public ResultDto setAnchorPeer(String channelName, String conName) {

		try {

			// 앵커피어를 등록할 컨테이너 정보 조회
			ConInfoDto conInfoDto = containerService.findConInfoByConName(conName);

			// 앵커피어를 등록할 채널정보 조회
			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);

			// 앵커피어를 등록할 채널에 피어 정보 조회??
			ChannelInfoPeerDto channelInfoPeerDto = channelService
					.findChannelInfoPeerByChannelNameAndConName(channelInfoDto, conInfoDto).get(0);

			// 조회한 피어에 앵커피어 설정이 되어있으면 에러발샐
			if (channelInfoPeerDto.isAnchorYn()) {
				throw new Exception(conName + " is already anchor peer");
			}

			// 앵커피어를 등록한 FabricMemberDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr(conInfoDto.getOrgType(),
					conInfoDto.getOrgName());
			FabricMemberDto peerDto = null;

			for (FabricMemberDto peerDto2 : peerDtoArr) {
				if (peerDto2.getConName().equals(conName)) {
					peerDto = peerDto2;
				}
			}

			// 앵커피어를 등록을 진행할 FabricMemberDto(orderer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer",
					channelInfoDto.getOrderingOrg());

			// wallet 생성
			fabricClient.createWallet(peerDto);
			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(1000);

			// 채널 생성 함수 시작
			logger.info("[test] 시작");
//			fabricClient.test(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),channelName);
			fabricClient.setAnchorConfig(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					channelName);

			channelInfoPeerDto.setAnchorYn(true);

			// 채널에 가인된 피어 정보 업데이트
			channelService.saveChannelInfoPeer(channelInfoPeerDto);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success update anchor", null);

	}

	/**
	 * 블럭 이벤트 리슨용 리스너 생성 함수
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 생성한 이벤트 리스너
	 */

	private BlockListener createBlockListener(String channelName) {

		// 블록 리스너 생성
		BlockListener blockListener = new BlockListener() {

			@Override
			public void received(BlockEvent blockEvent) {

				// 채널 정보 조
				ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);
				BlockDto blockDto;

				try {

					// 이벤트로 받은 blockDataHash이 있는지 조회
					blockDto = blockService.findBlockByBlockDataHash(Hex.encodeHexString(blockEvent.getDataHash()));

				} catch (IllegalArgumentException e) {

					// 조회가 안되면 리슨받은 블록 정보 저장
					blockDto = new BlockDto();
					blockDto.setBlockDataHash(Hex.encodeHexString(blockEvent.getDataHash()));
					blockDto.setBlockNum((int) blockEvent.getBlockNumber());
					blockDto.setPrevDataHash(Hex.encodeHexString(blockEvent.getPreviousHash()));
					blockDto.setTxCount(blockEvent.getTransactionCount());
					blockDto.setChannelInfoDto(channelInfoDto);

					blockService.saveBLock(blockDto);
				}

				// 블록 내 트렌젝션 순회
				for (EnvelopeInfo envelopeInfo : blockEvent.getEnvelopeInfos()) {

					try {
						// 이벤트로 받은 txID가 있는지 조회
						transactionService.findBlockByTxId(envelopeInfo.getTransactionID());

					} catch (IllegalArgumentException e) {

						// 조회가 안되면 트렌젝션 정보 저장
						TransactionDto transactionDto = new TransactionDto();
						transactionDto.setTxID(envelopeInfo.getTransactionID());
						transactionDto.setCreatorId(envelopeInfo.getCreator().getMspid());
						transactionDto.setTxType(envelopeInfo.getType().toString());
						transactionDto.setTimestamp(envelopeInfo.getTimestamp());
						transactionDto.setBlockDto(blockDto);
						transactionDto.setChannelInfoDto(channelInfoDto);

						if (envelopeInfo.getType() == EnvelopeType.TRANSACTION_ENVELOPE) {
							TransactionEnvelopeInfo transactionEnvelopeInfo = (TransactionEnvelopeInfo) envelopeInfo;

							for (TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo
									.getTransactionActionInfos()) {

								transactionDto.setCcName(transactionActionInfo.getChaincodeIDName());
								transactionDto.setCcVersion(transactionActionInfo.getChaincodeIDVersion());

								JSONObject argsJson = new JSONObject();

								for (int i = 1; i < transactionActionInfo.getChaincodeInputArgsCount(); i++) {

									argsJson.put("arg" + i, new String(transactionActionInfo.getChaincodeInputArgs(i)));

								}

								transactionDto.setCcArgs(argsJson.toString());

							}
							transactionService.saveTransaction(transactionDto);
						}
					}

				}

				// 채널정보 조회
				System.out.println("channel Block : " + blockService.countBychannelBlock(channelInfoDto)
						+ " channel Tx : " + transactionService.countBychannelTransaction(channelInfoDto)
						+ " current BLock : " + blockEvent.getBlockNumber());
				channelInfoDto.setChannelBlock(blockService.countBychannelBlock(channelInfoDto));
				channelInfoDto.setChannelTx(transactionService.countBychannelTransaction(channelInfoDto));
				channelService.saveChannelInfo(channelInfoDto);

				try {

					// 웹소캣 연결된 클라이언트에게 이벤트 전송
					webSocket.convertAndSend("/event",
							blockEvent.getBlockNumber() + " in " + blockEvent.getChannelId());

				} catch (MessagingException | InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		};

		return blockListener;
	}

	/**
	 * 체인코드 업로드 서비스
	 * 
	 * @param ccFile 체인코드 파일 이름
	 * @param ccName 체인코드 이름
	 * @param ccDesc 체인코드 설명
	 * @param ccLang 체인코드 언어
	 * 
	 * @return 체인코드 업로드 결과 DTO
	 */

	public ResultDto ccFileUpload(MultipartFile ccFile, String ccName, String ccDesc, String ccLang, String ccVersion) {

		try {

			// 파일로 변경 작업
			InputStream inputStream = ccFile.getInputStream();
			File file = new File(System.getProperty("user.dir") + "/chaincode/src/");

			if (!file.exists()) {
				try {

					file.mkdirs();

				} catch (Exception e) {

					return util.setResult("9999", false, e.getMessage(), null);

				}

			} else {

			}
			OutputStream outputStream = new FileOutputStream(new File(
					System.getProperty("user.dir") + "/chaincode/src/"+ ccFile.getOriginalFilename()));
			int i;

			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}

			outputStream.close();
			inputStream.close();

			String ccPath =fabricClient.packageChaincodeWithLifecycle(ccName, ccVersion);

			// 디비에 저장(CCINFO)
			CcInfoDto ccInfoDto = new CcInfoDto();

			ccInfoDto.setCcName(ccName);
			ccInfoDto.setCcDesc(ccDesc);
			ccInfoDto.setCcLang(ccLang);
			ccInfoDto.setCcPath(ccPath);

			chaincodeService.saveCcInfo(ccInfoDto);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success chaincode file upload", null);
	}
	


}
