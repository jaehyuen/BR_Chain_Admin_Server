package com.brchain.core.fabric.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
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

import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.BrchainException;
import com.brchain.core.chaincode.dto.ActiveCcDto;
import com.brchain.core.chaincode.dto.InstallCcDto;
import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.service.ChaincodeService;
import com.brchain.core.channel.dto.CreateChannelDto;
import com.brchain.core.channel.entitiy.ChannelHandleEntity;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.service.ChannelService;
import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.CreateOrgConInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.container.service.DockerService;
import com.brchain.core.fabric.dto.FabricNodeDto;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.JsonUtil;
import com.brchain.core.util.Util;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FabricService {

	private final FabricClient          fabricClient;
	private final SshClient             sshClient;

	private final ContainerService      containerService;
	private final ChaincodeService      chaincodeService;
	private final ChannelService        channelService;
	private final DockerService         dockerService;
	private final BlockService          blockService;
	private final TransactionService    transactionService;

	private final SimpMessagingTemplate webSocket;

	private final Environment           environment;

	private final Util                  util;
	private final JsonUtil              jsonUtil;

	private Logger                      logger = LoggerFactory.getLogger(this.getClass());

	@PostConstruct
	private void init() {

		List<ChannelInfoEntity> channelInfoEntityList = new ArrayList<ChannelInfoEntity>();
		List<FabricNodeDto>     peerDtoArr            = new ArrayList<FabricNodeDto>();
		List<FabricNodeDto>     ordererDtoArr         = new ArrayList<FabricNodeDto>();
		List<String>            orgs                  = new ArrayList<String>();

		Channel                 channel;

		channelInfoEntityList.addAll(channelService.findChannelInfoList());

		for (ChannelInfoEntity channelInfoEntity : channelInfoEntityList) {

			orgs.addAll(containerService.findOrgsInChannel(channelInfoEntity.getChannelName()));

			for (String org : orgs) {
				peerDtoArr.addAll(containerService.createFabricNodeDtoArr("peer", org));
			}

			ordererDtoArr.addAll(containerService.createFabricNodeDtoArr("orderer", channelInfoEntity.getOrderingOrg()));

			for (FabricNodeDto peerDto : peerDtoArr) {

				fabricClient.createWallet(peerDto);
			}

			for (FabricNodeDto ordererDto : ordererDtoArr) {

				fabricClient.createWallet(ordererDto);
			}

			Network newwork;

			newwork = fabricClient.connectNetwork(channelInfoEntity.getChannelName(), orgs.get(0),
					jsonUtil.createFabrcSetting(channelInfoEntity.getChannelName(), ordererDtoArr, peerDtoArr, orgs));

			channel = newwork.getChannel();

			try {
				for (long i = channelInfoEntity.getChannelBlock(); i < channel.queryBlockchainInfo()
					.getHeight(); i++) {

					blockService.inspectBlock(channel.queryBlockByNumber(i), channelInfoEntity);

					channelInfoEntity.setChannelBlock(blockService.countByChannelName(channelInfoEntity.getChannelName()));
					channelInfoEntity.setChannelTx(transactionService.countByChannelName(channelInfoEntity.getChannelName()));
					channelService.saveChannelInfo(channelInfoEntity);

				}
			} catch (ProposalException | InvalidArgumentException e) {

				throw new BrchainException(e, BrchainStatusCode.FABRIC_QUERY_ERROR);

			}

			fabricClient.registerEventListener(channelInfoEntity.getChannelName(),
					createBlockListener(channelInfoEntity.getChannelName()));

			peerDtoArr.clear();
			ordererDtoArr.clear();
			orgs.clear();

		}

	}

	/**
	 * 조직 생성 서비스
	 * 
	 * @param conInfoDtoArr 컨테이너 관련 DTO
	 * 
	 * @return 결과 DTO(조직생성 결과)
	 * 
	 */

	public ResultDto<?> createOrg(ArrayList<CreateOrgConInfoDto> createOrgConInfoDtoArr) {

		logger.info("[조직생성] 시작");

		try {
			String                           ordererPorts      = "";
			String                           gossipBootAddress = "";
			JSONObject                       conJson           = new JSONObject();
			JSONObject                       returnJson        = new JSONObject();
			CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr     = new CopyOnWriteArrayList<ConInfoDto>();

			// 컨테이너 생성시 필요한 변수 선언
			for (CreateOrgConInfoDto createOrgConInfoDto : createOrgConInfoDtoArr) {

				if (createOrgConInfoDto.getConType().equals("orderer")) {

					ordererPorts = ordererPorts + createOrgConInfoDto.getConPort() + " ";

				}

				if (createOrgConInfoDto.getConType().equals("peer")) {

					gossipBootAddress = gossipBootAddress + createOrgConInfoDto.getConType()
							+ createOrgConInfoDto.getConNum() + ".org" + createOrgConInfoDto.getOrgName() + ".com:"
							+ createOrgConInfoDto.getConPort() + " ";
				}
				conInfoDtoArr.add(ConInfoDto.builder()
					.conType(createOrgConInfoDto.getConType())
					.conPort(createOrgConInfoDto.getConPort())
					.orgName(createOrgConInfoDto.getOrgName())
					.orgType(createOrgConInfoDto.getOrgType())
					.conNum(createOrgConInfoDto.getConNum())
					.conCnt(createOrgConInfoDto.getConCnt())
					.couchdbYn(createOrgConInfoDto.isCouchdbYn())
					.build());
			}

			int i = 0;

			for (ConInfoDto conInfoDto : conInfoDtoArr) {

				if (conInfoDto.getConType().equals("ca")) {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType()
							+ " 컨테이너 생성");
//					containerService.saveConInfo(dockerService.createContainer(dto));
					returnJson = dockerService.createContainer(conInfoDto);
					conJson.put(returnJson.get("container_name"), returnJson);

					// setup 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					ConInfoDto setupContainer = new ConInfoDto();
					setupContainer.setOrgName(conInfoDto.getOrgName());
					setupContainer.setOrgType(conInfoDto.getOrgType());
					setupContainer.setConPort(conInfoDto.getConPort());
					setupContainer.setConCnt(conInfoDto.getConCnt());
					setupContainer.setConNum(conInfoDto.getConNum());
					setupContainer.setConType("setup_" + conInfoDto.getOrgType());

					if (conInfoDto.getOrgType().equals("orderer")) {

						setupContainer.setOrdererPorts(ordererPorts);
						setupContainer.setPeerOrgs(containerService.findConInfoByConType("ca", "peer"));

					} else {
						setupContainer.setPeerOrgs(conInfoDto.getOrgName());
					}

					Thread.sleep(5000);

					logger.info("[조직생성] 도커 컨테이너 생성 -> " + setupContainer.getOrgName() + " 조직의 "
							+ setupContainer.getConType() + " 컨테이너 생성");
					logger.info(setupContainer.toString());
					returnJson = dockerService.createContainer(setupContainer);
					conJson.put(returnJson.get("container_name"), returnJson);
					Thread.sleep(5000);

					conInfoDtoArr.add(i + 1, setupContainer);

				} else if (conInfoDto.getConType().equals("peer")) {

					// 컨테이너 생성 함수 호출
					conInfoDto.setGossipBootAddr(gossipBootAddress);
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType()
							+ conInfoDto.getConNum() + " 컨테이너 생성");
					logger.info(conInfoDto.toString());
					returnJson = dockerService.createContainer(conInfoDto);
					conJson.put(returnJson.get("container_name"), returnJson);

					// couchdb 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					if (conInfoDto.isCouchdbYn()) {

						ConInfoDto couchdbContainer = new ConInfoDto();
						couchdbContainer.setOrgName(conInfoDto.getOrgName());
						couchdbContainer.setOrgType(conInfoDto.getOrgType());
						couchdbContainer.setConNum(conInfoDto.getConNum());
						couchdbContainer.setConType("couchdb");

						logger.info("[조직생성] 도커 컨테이너 생성 -> " + couchdbContainer.getOrgName() + " 조직의 "
								+ couchdbContainer.getConType() + couchdbContainer.getConNum() + " 컨테이너 생성");

						returnJson = dockerService.createContainer(couchdbContainer);
						conJson.put(returnJson.get("container_name"), returnJson);

					}

				} else {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType()
							+ " 컨테이너 생성");
					conInfoDto.setConsoOrgs(containerService.findConInfoByConType("ca", "peer"));
					logger.info(conInfoDto.toString());

					returnJson = dockerService.createContainer(conInfoDto);
					conJson.put(returnJson.get("container_name"), returnJson);

				}

				Thread.sleep(2000);
				i++;
			}

			// 생성된 조직의 docker-compose yaml file 생성
			jsonUtil.createYamlFile(conInfoDtoArr.get(0).getOrgName(), conJson);

			String path = null;

			// 로컬 개발시 실서버에서 생성된 인증서,트렌젝션 다운로드
			if (environment.getActiveProfiles()[0].equals("local")) {

				if (conInfoDtoArr.get(0).getOrgType().equals("peer")) {

					path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0).getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0).getOrgName() + ".com/msp/keystore/";
					
					sshClient.downloadFile(path, "server.key");

					path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0).getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0).getOrgName() + ".com/msp/signcerts/";
					
					sshClient.downloadFile(path, "cert.pem");

				} else {

					path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0).getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0).getOrgName() + ".com/msp/keystore/";
					
					sshClient.downloadFile(path, "server.key");
					
					path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0).getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0).getOrgName() + ".com/msp/signcerts/";
					
					sshClient.downloadFile(path, "cert.pem");

				}
				
				path = "crypto-config/ca-certs/";
				sshClient.downloadFile(path, "ca.org" + conInfoDtoArr.get(0).getOrgName() + ".com-cert.pem");
			}

			ArrayList<FabricNodeDto> FabricNodeDto = containerService.createFabricNodeDtoArr(conInfoDtoArr.get(0).getOrgType(), conInfoDtoArr.get(0).getOrgName());
			fabricClient.createWallet(FabricNodeDto.get((int) (Math.random() * FabricNodeDto.size())));

		} catch (InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.THREAD_ERROR);
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

	public ResultDto<?> createChannel(CreateChannelDto createChannelDto) {

		logger.info("[채널생성] 시작");
		logger.info("[채널생성] " + createChannelDto.getChannelName());
		logger.info("[채널생성] CreateChannelVo : " + createChannelDto);

		Util       util       = new Util();

		JSONObject conJson    = new JSONObject();
		JSONObject returnJson = new JSONObject();

		try {

			ConInfoDto conInfoDto = new ConInfoDto();

			String     orgs       = "";
			String     path       = "";

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
			ArrayList<FabricNodeDto> peerDtoArr = new ArrayList<FabricNodeDto>();

			for (String org : createChannelDto.getPeerOrgs()) {
				peerDtoArr.addAll(containerService.createFabricNodeDtoArr("peer", org));
			}
			ArrayList<FabricNodeDto> ordererDtoArr = containerService.createFabricNodeDtoArr("orderer",
					createChannelDto.getOrderingOrg());

			logger.info("[채널생성] peerDtoArr : " + peerDtoArr);
			logger.info("[채널생성] ordererDtoArr : " + ordererDtoArr);

			// 채널 생성시 필요한 wallet 생성
			fabricClient.createWallet(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())));

			for (FabricNodeDto peerDto : peerDtoArr) {

				fabricClient.createWallet(peerDto);
			}

			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(5000);
			// 로컬 개발시 채널생성 setup 컨테이너 기동하면서 생성된 채널트렌젝션 다운로드
			if (environment.getActiveProfiles()[0].equals("local")) {

				path = "channel-artifacts/" + conInfoDto.getOrgName() + "/";
				sshClient.downloadFile(path, conInfoDto.getOrgName() + ".tx");

			}

			for (FabricNodeDto peerDto : peerDtoArr) {
				FabricNodeDto ordererDto = ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size()));

				if (!containerService.isMemOfConso(ordererDto.getOrgName(), peerDto.getOrgName())) {

					JSONObject genesisJson = fabricClient.getChannelConfig(ordererDto, "testchainid");
					JSONObject testJson    = jsonUtil.createOrgJson(peerDto);

					logger.info(genesisJson.toString());
					logger.info(testJson.toString());

					// 시스템 채널 컨소시움 추가

					JSONObject modifiedJson = jsonUtil.modifyConsoConfig(genesisJson, testJson, "", peerDto.getOrgName());

					File       updateFile   = fabricClient.createUpdateFile(ordererDto, "testchainid", genesisJson,
							modifiedJson);

					fabricClient.setUpdate(ordererDto, ordererDto, "testchainid", updateFile);

					containerService.updateConsoOrgs(ordererDto.getOrgName(), peerDto.getOrgName());

				}
			}

			// 채널 생성 함수 시작
			logger.info("[채널생성] 시작");

			fabricClient.createChannel(peerDtoArr, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					createChannelDto.getChannelName());

			ChannelInfoEntity channelInfoEntity = new ChannelInfoEntity();
			channelInfoEntity.setChannelName(createChannelDto.getChannelName());
			channelInfoEntity.setOrderingOrg(createChannelDto.getOrderingOrg());
			channelInfoEntity.setChannelTx(0);
			channelInfoEntity.setChannelBlock(0);
			channelInfoEntity.setAppAdminPolicyType("ImplicitMeta");
			channelInfoEntity.setAppAdminPolicyValue("ANY Admins");
			channelInfoEntity.setChannelAdminPolicyType("ImplicitMeta");
			channelInfoEntity.setChannelAdminPolicyValue("ANY Admins");
			channelInfoEntity.setOrdererAdminPolicyType("ImplicitMeta");
			channelInfoEntity.setOrdererAdminPolicyValue("ANY Admins");
			channelInfoEntity.setBatchTimeout("1s");
			channelInfoEntity.setBatchSizeAbsolMax(81920);
			channelInfoEntity.setBatchSizeMaxMsg(20);
			channelInfoEntity.setBatchSizePreferMax(20480);
			channelService.saveChannelInfo(channelInfoEntity);

			jsonUtil.createYamlFile(createChannelDto.getChannelName(), conJson);

			logger.info("[채널생성] 종료");

			logger.info("[채널가입] 시작");

			joinChannel(peerDtoArr, ordererDtoArr, createChannelDto.getChannelName());

			logger.info("[채널가입] 종료");

			logger.info("[채널생성] 종료");

			fabricClient.connectNetwork(channelInfoEntity.getChannelName(), createChannelDto.getPeerOrgs()
				.get(0),
				jsonUtil.createFabrcSetting(channelInfoEntity.getChannelName(), ordererDtoArr, peerDtoArr,
							createChannelDto.getPeerOrgs()));

			fabricClient.registerEventListener(createChannelDto.getChannelName(),
					createBlockListener(channelInfoEntity.getChannelName()));

		} catch (InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.THREAD_ERROR);
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
	 * @throws InterruptedException
	 */

	public void joinChannel(ArrayList<FabricNodeDto> peerDtoArr, ArrayList<FabricNodeDto> ordererDtoArr,
			String channelName) {

		for (FabricNodeDto peerDto : peerDtoArr) {

			// 클라이언트 생성
			HFClient client = fabricClient.createClient(peerDto);

			// 채널 조인
			fabricClient.joinChannel(client, peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					channelName);

			ChannelInfoPeerEntity channelInfoPeerEntity = new ChannelInfoPeerEntity();

			channelInfoPeerEntity.setAnchorYn(false);
			channelInfoPeerEntity.setChannelInfoEntity(channelService.findChannelInfoByChannelName(channelName));
			channelInfoPeerEntity.setConInfoEntity(containerService.findConInfoByConName(peerDto.getConName()));

			// 채널 가입한 피어정보 저장
			channelService.saveChannelInfoPeer(channelInfoPeerEntity);

		}

	}

	/**
	 * 체인코드 설치 서비스
	 * 
	 * @param installCcDto 체인코드 설치 관련 DTO
	 * 
	 * @return 결과 DTO(체인코스 설치 결과)
	 */

	public ResultDto<?> installChaincode(InstallCcDto installCcDto) {
		logger.info("[체인코드 설치] 시작");
		logger.info("[체인코드 설치] InstallCcDto : " + installCcDto);

		// 체인코드를 설치할 FabricMembetDto(peer) 생성
		ArrayList<FabricNodeDto> peerDtoArr = containerService.createFabricNodeDtoArr("peer", installCcDto.getOrgName());

		FabricNodeDto            peerDto    = null;

		for (FabricNodeDto peer : peerDtoArr) {
			if (peer.getConNum() == installCcDto.getConNum()) {
				peerDto = peer;

			}
		}

		// 체인코드 설치
		fabricClient.installChaincode(peerDto, installCcDto.getCcName(), installCcDto.getCcVersion());

		CcInfoPeerEntity ccInfoPeerEntity = new CcInfoPeerEntity();

		// 설치한 체인코드 정보 조회
		CcInfoEntity     ccInfoEntity     = chaincodeService.findCcInfoById(installCcDto.getId());

		// 체인코드를 설치한 컨테이너 정보 조회
		ConInfoEntity    conInfoEntity    = containerService.findConInfoByConName(peerDto.getConName());

		ccInfoPeerEntity.setCcVersion(installCcDto.getCcVersion());
		ccInfoPeerEntity.setCcInfoEntity(ccInfoEntity);
		ccInfoPeerEntity.setConInfoEntity(conInfoEntity);

		// 체인코드 설치한 피어정보 저장
		chaincodeService.saveCcInfoPeer(ccInfoPeerEntity);

		logger.info("[체인코드 설치] 종료");

		return util.setResult("0000", true, "Success install chaincode", null);
	}

	public ResultDto<?> activeChaincode(ActiveCcDto activeCcDto)  {
		
		try {

			logger.info("[체인코드 활성화] 시작");
			logger.info("[체인코드 활성화] activeCcDto : " + activeCcDto);

			logger.info("[체인코드 활성화] activeCcDto.getChannelName() : " + activeCcDto.getChannelName());

			CcInfoEntity           ccInfoEntity         = chaincodeService.findCcInfoById(activeCcDto.getId());
			ChannelInfoEntity      channelInfoEntity    = channelService.findChannelInfoByChannelName(activeCcDto.getChannelName());
			List<String>           orgs                 = containerService.findOrgsInChannel(activeCcDto.getChannelName());
			List<FabricNodeDto>    peerDtoArr           = new ArrayList<FabricNodeDto>();
			List<CcInfoPeerEntity> ccInfoPeerEntityList = new ArrayList<CcInfoPeerEntity>();
			List<FabricNodeDto>    ordererDtoArr        = new ArrayList<FabricNodeDto>();

			for (String org : orgs) {
				peerDtoArr.addAll(containerService.createFabricNodeDtoArr("peer", org));
			}

			ccInfoPeerEntityList.addAll(chaincodeService.findByCcInfoId(activeCcDto.getId()));
			ordererDtoArr.addAll(containerService.createFabricNodeDtoArr("orderer", channelInfoEntity.getOrderingOrg()));
			System.out.println(ccInfoPeerEntityList);

			for (FabricNodeDto peerDto : peerDtoArr) {
				boolean flag = false;
				for (CcInfoPeerEntity ccInfoPeerEntity : ccInfoPeerEntityList) {
					if (peerDto.getConName().equals(ccInfoPeerEntity.getConInfoEntity().getConName())) {
						flag = true;
					}

				}

				if (!flag) {
					logger.debug(peerDto.getConName() + " 여기에 체인코드 설치 안됨");
					InstallCcDto installCcDto = new InstallCcDto();

					installCcDto.setCcName(activeCcDto.getCcName());
					installCcDto.setCcVersion(activeCcDto.getCcVersion());
					installCcDto.setId(activeCcDto.getId());
					installCcDto.setOrgName(peerDto.getOrgName());
					installCcDto.setConNum(peerDto.getConNum());

					installChaincode(installCcDto);

				}
			}

			CcInfoChannelEntity ccInfoChannelEntity;

			fabricClient.activeChaincode(peerDtoArr, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),
					activeCcDto.getChannelName(), orgs, activeCcDto.getCcName(), activeCcDto.getCcVersion());
			logger.info("[체인코드 인스턴스화] 종료");

			try {

				// 이미 인스턴스화가 진행된 체인코드인지 조회
				ccInfoChannelEntity = chaincodeService.findByChannelNameAndCcName(activeCcDto.getChannelName(), activeCcDto.getCcName());

				ccInfoChannelEntity.setCcVersion(activeCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 업데이트
				chaincodeService.saveCcInfoChannel(ccInfoChannelEntity);

			} catch (IllegalArgumentException e) {

				ccInfoChannelEntity = new CcInfoChannelEntity();
				ccInfoChannelEntity.setCcInfoEntity(ccInfoEntity);
				ccInfoChannelEntity.setChannelInfoEntity(channelInfoEntity);
				ccInfoChannelEntity.setCcVersion(activeCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 저장
				chaincodeService.saveCcInfoChannel(ccInfoChannelEntity);
			}

			return util.setResult("0000", true, "Success instantiate chaincode", null);
		} catch ( InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.THREAD_ERROR);

		}

	}

//	/**
//	 * 체인코드 인스턴스화 서비스 (1.4.x 버전)
//	 * 
//	 * @param instantiateCcDto 체인코드 인스턴스화 관련 DTO
//	 * 
//	 * @return 결과 DTO(체인코드 인스턴스화 결과)
//	 */
//
//	public ResultDto instantiateChaincode(ActiveCcDto instantiateCcDto) {
//
//		logger.info("[체인코드 인스턴스화] 시작");
//		logger.info("[체인코드 인스턴스화] InstantiateCcDto : " + instantiateCcDto);
//
//		logger.info("[체인코드 인스턴스화] instantiateCcDto.getChannelName() : " + instantiateCcDto.getChannelName());
//
//		try {
//
//			// 채인코드 인스턴스화를 진항항 채널 정보 조회
//			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(instantiateCcDto.getChannelName());
//
//			// 인스턴스화를 진행할 체인코드 정보 조회
//			CcInfoDto      ccInfoDto      = chaincodeService.findCcInfoById(instantiateCcDto.getId());
//
//			logger.info("[체인코드 인스턴스화] channelInfo : " + channelInfoDto);
//
//			// 체인코드 인스턴스화를 진행할 피어조회
////			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto);
//			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto.getChannelName());
//
//			logger.info("[체인코드 인스턴스화] channelInfoPeerDtoArr : " + channelInfoPeerDtoArr);
//
//			// 체인코드 인스턴스화를 진행할 FabricMembetDto(peer) 생성
//			ArrayList<FabricNodeDto> peerDtoArr    = containerService.createfabricNodeDtoArr("peer", channelInfoPeerDtoArr.get((int) (Math.random() * channelInfoPeerDtoArr.size()))
//				.getConInfoDto()
//				.getOrgName());
//
//			// 체인코드 인스턴스화를 진행할 FabricMembetDto(peer) 생성
//			ArrayList<FabricNodeDto> ordererDtoArr = containerService.createfabricNodeDtoArr("orderer", channelInfoDto.getOrderingOrg());
//
//			CcInfoChannelDto           ccInfoChannelDto;
//			try {
//
//				// 이미 인스턴스화가 진행된 체인코드인지 조회
////				ccInfoChannelDto = chaincodeService.findCcInfoChannelByChannelInfoAndCcInfo(channelInfoDto, ccInfoDto);
//				ccInfoChannelDto = chaincodeService.findByChannelNameAndCcName(instantiateCcDto.getChannelName(), instantiateCcDto.getId());
//
//				// 조회가 되면 업데이트 진행
//				fabricClient.instantiateChaincode(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())), ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), instantiateCcDto.getChannelName(), instantiateCcDto.getCcName(), instantiateCcDto.getCcVersion(),
//						instantiateCcDto.getCcLang(), true);
//
//				ccInfoChannelDto.setCcVersion(instantiateCcDto.getCcVersion());
//
//				// 채널에 활성화된 체인코드정보 업데이트
//				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);
//
//			} catch (IllegalArgumentException e) {
//
//				// 조회가 안되면 인스턴스화 진행
//				fabricClient.instantiateChaincode(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())), ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), instantiateCcDto.getChannelName(), instantiateCcDto.getCcName(), instantiateCcDto.getCcVersion(),
//						instantiateCcDto.getCcLang(), false);
//
//				ccInfoChannelDto = new CcInfoChannelDto();
//				ccInfoChannelDto.setCcInfoDto(ccInfoDto);
//				ccInfoChannelDto.setChannelInfoDto(channelInfoDto);
//				ccInfoChannelDto.setCcVersion(instantiateCcDto.getCcVersion());
//
//				// 채널에 활성화된 체인코드정보 저장
//				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);
//			}
//
//			logger.info("[체인코드 인스턴스화] 종료");
//
//		} catch (Exception e) {
//
//			logger.error(e.getMessage());
//			e.printStackTrace();
//			return util.setResult("9999", false, e.getMessage(), null);
//		}
//
//		return util.setResult("0000", true, "Success instantiate chaincode", null);
//
//	}

	/**
	 * 채널 블록 이벤트 등록 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 블록 이벤트 등록 결과)
	 */

	public ResultDto<?> registerEventListener(String channelName) {

		logger.info("[채널 블럭 이벤트 등록] 시작");
		logger.info("[채널 블럭 이벤트 등록] channelName : " + channelName);

		// 이벤트 리슨을 등록할 채널 정보 조회
		ChannelInfoEntity        channelInfoEntity     = channelService.findChannelInfoByChannelName(channelName);

		// 이벤트 리슨을 등록할 피어 정보 조회
//			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto);
		List<ChannelInfoPeerEntity> channelInfoPeerEntityList = channelService.findChannelInfoPeerByChannelInfo(channelInfoEntity.getChannelName());

//		// 이벤트 리슨을 등록할 FabricMembetDto(peer) 생성
//		ArrayList<FabricNodeDto> peerDtoArr            = containerService.createfabricNodeDtoArr("peer",
//				channelInfoPeerEntityList.get((int) (Math.random() * channelInfoPeerEntityList.size()))
//					.getConInfoEntity()
//					.getOrgName());
//
//		// 이벤트 리슨을 등록할 FabricMembetDto(orderer) 생성
//		ArrayList<FabricNodeDto> ordererDtoArr         = containerService.createfabricNodeDtoArr("orderer",channelInfoEntity.getOrderingOrg());
		ChannelHandleEntity         channelHandleEntity;
		
		try {

			// 이벤트 리슨이 등록된 채널인지 채널 핸들 조회
			channelHandleEntity = channelService.findChannelHandleByChannel(channelName);

			// 이벤트 리슨이 등록된 채널이면 에러 발생

			throw new BrchainException("already registered event listener",BrchainStatusCode.ALREADY_REGISTERED_LISTENER_ERROR);

		} catch (IllegalArgumentException e) {

			channelHandleEntity = new ChannelHandleEntity();

			// 이벤트 리슨 등록
//				String handle = fabricClient.registerEventListener(
//						peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
//						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName,
//						createBlockListener(channelName),
//						channelInfoDto.getChannelBlock() < 1 ? 0 : channelInfoDto.getChannelBlock() - 1);

			String handle = "zz";
			channelHandleEntity.setChannelName(channelName);
			channelHandleEntity.setHandle(handle);

			// 채널 핸들 정보 저장
			channelService.saveChannelHandle(channelHandleEntity);
		}

		logger.info("[채널 블럭 이벤트 등록] 종료");

		return util.setResult("0000", true, "Success register block event listener", null);
	}

	/**
	 * 채널 블록 이벤트 리스너 삭제 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 블록 이벤트 삭제 결과)
	 */

//	public ResultDto<?> unregisterEventListener(String channelName) {
//
//		try {
//
//			// 이벤트 리슨을 삭제할 채널 정보 조회
//			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);
//
//			// 이벤트 리슨을 삭제할 피어 정보 조회
////			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto);
//			List<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto.getChannelName());
//
//			// 이벤트 리슨을 삭제할 FabricNodeDto(peer) 생성
//			ArrayList<FabricNodeDto> peerDtoArr = containerService.createfabricNodeDtoArr("peer", channelInfoPeerDtoArr.get((int) (Math.random() * channelInfoPeerDtoArr.size()))
//				.getConInfoDto()
//				.getOrgName());
//
//			// 이벤트 리슨을 삭제할 FabricNodeDto(orderer) 생성
//			ArrayList<FabricNodeDto> ordererDtoArr = containerService.createfabricNodeDtoArr("orderer", channelInfoDto.getOrderingOrg());
//
//			// 삭제할 채널 핸들 조회
//			String channelHandle = channelService.findChannelHandleByChannel(channelName)
//				.getHandle();
//
//			// 이벤트 리슨을 삭제
//			fabricClient.unregisterEventListener(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())), ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName, channelHandle);
//
//			// 채널 핸들 삭제
//			channelService.deleteChannelHandle(channelName);
//
//		} catch (Exception e) {
//
//			logger.error(e.getMessage());
//			e.printStackTrace();
//			return util.setResult("9999", false, e.getMessage(), null);
//		}
//
//		return util.setResult("0000", true, "Success unregister block event listener", null);
//	}

	/**
	 * 앵커피어 설정 서비스
	 * 
	 * @param channelName 채널 이름
	 * @param conName     컨테이너 이름
	 * 
	 * @return 결과 DTO(앵커피어 설정 결과)
	 */

	public ResultDto<?> setAnchorPeer(String channelName, String conName) {

		try {

			// 앵커피어를 등록할 컨테이너 정보 조회
			ConInfoEntity         conInfoEntity         = containerService.findConInfoByConName(conName);

			// 앵커피어를 등록할 채널정보 조회
			ChannelInfoEntity     channelInfoEntity     = channelService.findChannelInfoByChannelName(channelName);

			// 앵커피어를 등록할 채널에 피어 정보 조회??
//			ChannelInfoPeerDto channelInfoPeerDto = channelService.findChannelInfoPeerByChannelNameAndConName(channelInfoDto, conInfoDto).get(0);
			ChannelInfoPeerEntity channelInfoPeerEntity = channelService
				.findChannelInfoPeerByChannelNameAndConName(channelInfoEntity.getChannelName(), conInfoEntity.getConName())
				.get(0);

//			System.out.println()
			// 조회한 피어에 앵커피어 설정이 되어있으면 에러발샐
			if (channelInfoPeerEntity.isAnchorYn()) {

				throw new BrchainException(conName + " is already anchor peer",
						BrchainStatusCode.ALREADY_AHCHOR_PEER_ERROR);
			}

			// 앵커피어를 등록한 FabricNodeDto(peer) 생성
			ArrayList<FabricNodeDto> peerDtoArr = containerService.createFabricNodeDtoArr(conInfoEntity.getOrgType(),conInfoEntity.getOrgName());
			FabricNodeDto            peerDto    = null;

			for (FabricNodeDto peerDto2 : peerDtoArr) {
				if (peerDto2.getConName()
					.equals(conName)) {
					peerDto = peerDto2;
				}
			}

			// 앵커피어를 등록을 진행할 FabricNodeDto(orderer) 생성
			ArrayList<FabricNodeDto> ordererDtoArr = containerService.createFabricNodeDtoArr("orderer",
					channelInfoEntity.getOrderingOrg());

			// wallet 생성
			fabricClient.createWallet(peerDto);
			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(1000);

			// 채널 생성 함수 시작
			logger.info("[test] 시작");
//			fabricClient.test(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),channelName);
			fabricClient.setAnchorConfig(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),channelName);

			channelInfoPeerEntity.setAnchorYn(true);

			// 채널에 가인된 피어 정보 업데이트
			channelService.saveChannelInfoPeer(channelInfoPeerEntity);

		} catch (InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.THREAD_ERROR);
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

	private Consumer<BlockEvent> createBlockListener(String channelName) {

		Consumer<BlockEvent> blockEventListener = blockEvent -> {

			// 채널 정보 조회
			ChannelInfoEntity channelInfoEntity = channelService.findChannelInfoByChannelName(channelName);
			blockService.inspectBlock(blockEvent, channelInfoEntity);

			channelInfoEntity.setChannelBlock(blockService.countByChannelName(channelName));
			channelInfoEntity.setChannelTx(transactionService.countByChannelName(channelInfoEntity.getChannelName()));
			channelService.saveChannelInfo(channelInfoEntity);

			try {

				// 웹소캣 연결된 클라이언트에게 이벤트 전송
				webSocket.convertAndSend("/event", blockEvent.getBlockNumber() + " in " + blockEvent.getChannelId());

			} catch (MessagingException | InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

		};

		return blockEventListener;

//		// 블록 리스너 생성
//		BlockListener blockListener = new BlockListener() {
//
//			@Override
//			public void received(BlockEvent blockEvent) {
//
//				// 채널 정보 조회
//				ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);
//				try {
//					blockService.inspectBlock(blockEvent, channelInfoDto);
//				} catch (InvalidProtocolBufferException e1) {
//					e1.printStackTrace();
//				}
//
//				channelInfoDto.setChannelBlock(blockService.countBychannelBlock(channelInfoDto));
//				channelInfoDto.setChannelTx(transactionService.countBychannelTransaction(channelInfoDto));
//				channelService.saveChannelInfo(channelInfoDto);
//
//				try {
//
//					// 웹소캣 연결된 클라이언트에게 이벤트 전송
//					webSocket.convertAndSend("/event",
//							blockEvent.getBlockNumber() + " in " + blockEvent.getChannelId());
//
//				} catch (MessagingException | InvalidProtocolBufferException e) {
//					e.printStackTrace();
//				}
//
//			}
//
//		};
//
//		return blockListener;
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

	public ResultDto<?> ccFileUpload(MultipartFile ccFile, String ccName, String ccDesc, String ccLang,
			String ccVersion) {

		try {

			// 파일로 변경 작업
			InputStream inputStream = ccFile.getInputStream();
			File        file        = new File(System.getProperty("user.dir") + "/chaincode/src/");

			if (!file.exists()) {

				file.mkdirs();

			} else {

			}

			util.createFolder(System.getProperty("user.dir") + "/chaincode/src/");
			util.createFolder(System.getProperty("user.dir") + "/chaincode/package/");

			OutputStream outputStream = new FileOutputStream(new File(System.getProperty("user.dir") + "/chaincode/src/"
					+ ccFile.getOriginalFilename() + "_v" + ccVersion));
			int          i;

			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}

			outputStream.close();
			inputStream.close();

			util.unZip(System.getProperty("user.dir") + "/chaincode/src/",
					ccFile.getOriginalFilename() + "_v" + ccVersion,
					System.getProperty("user.dir") + "/chaincode/src/");

			String    ccPath    = fabricClient.packageChaincodeWithLifecycle(ccName, ccVersion);

			// 디비에 저장(CCINFO)
			CcInfoEntity ccInfoEntity = new CcInfoEntity();

			ccInfoEntity.setCcName(ccName);
			ccInfoEntity.setCcDesc(ccDesc);
			ccInfoEntity.setCcLang(ccLang);
			ccInfoEntity.setCcPath(ccPath);
			ccInfoEntity.setCcVersion(ccVersion);

			chaincodeService.saveCcInfo(ccInfoEntity);
//
		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.CHAINCODE_UPLOAD_ERROR);

		}

		return util.setResult("0000", true, "Success chaincode file upload", null);
	}
	
	/**
	 * 조직 삭제 서비스 
	 * 
	 * @param orgName 삭제할 조직 이름
	 * 
	 * @return 삭제 결과 DTO
	 */
	public ResultDto<String> removeOrg(String orgName) {

		try {

			List<ChannelInfoEntity> channelInfoList = channelService.findChannelInfoPeerByOrgName(orgName);

			for (ChannelInfoEntity channelInfo : channelInfoList) {
				
				// 해당 채널의 오더링 조직의 dto 생성
				List<FabricNodeDto> ordererDtoList = containerService.createFabricNodeDtoArr("orderer",channelInfo.getOrderingOrg());
				
				// 해당 채널의 삭제할 조직을 제외한 조직 추출
				List<String>        peerOrg        = channelService.findOrgExcludedOrgName(channelInfo.getChannelName(),orgName);
				
				//추출한 조직의 dto 생성
				List<FabricNodeDto> peerDtoList    = containerService.createFabricNodeDtoArr("peer", peerOrg.get(0));

				// 조직 삭제 설정
				fabricClient.setRemoveOrgConfig(peerDtoList.get(0), ordererDtoList.get(0), channelInfo.getChannelName(),orgName);
				
				dockerService.removeOrgContainers(orgName);

			}

		} catch (InterruptedException e) {
			// TODO: handle exception
		}
		return util.setResult(BrchainStatusCode.SUCCESS, "Success remove org");

	}

}
