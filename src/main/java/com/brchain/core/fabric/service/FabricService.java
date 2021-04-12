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
import com.brchain.core.chaincode.dto.ActiveCcDto;
import com.brchain.core.chaincode.dto.CcInfoChannelDto;
import com.brchain.core.chaincode.dto.CcInfoDto;
import com.brchain.core.chaincode.dto.CcInfoPeerDto;
import com.brchain.core.chaincode.dto.InstallCcDto;
import com.brchain.core.chaincode.service.ChaincodeService;
import com.brchain.core.channel.dto.ChannelHandleDto;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.channel.dto.ChannelInfoPeerDto;
import com.brchain.core.channel.dto.CreateChannelDto;
import com.brchain.core.channel.service.ChannelService;
import com.brchain.core.client.FabricClient;
import com.brchain.core.client.SshClient;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.CreateOrgConInfoDto;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.container.service.DockerService;
import com.brchain.core.fabric.dto.FabricMemberDto;
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

	private Logger                      logger = LoggerFactory.getLogger(this.getClass());
		
	@PostConstruct
	private void init() throws Exception {

		List<ChannelInfoDto>  channelInfoDtoList = new ArrayList<ChannelInfoDto>();
		List<FabricMemberDto> peerDtoArr         = new ArrayList<FabricMemberDto>();
		List<FabricMemberDto> ordererDtoArr      = new ArrayList<FabricMemberDto>();
		List<String>          orgs               = new ArrayList<String>();

		Channel               channel;

		channelInfoDtoList.addAll(channelService.findChannelInfoList());

		for (ChannelInfoDto channelInfoDto : channelInfoDtoList) {

			orgs.addAll(containerService.findOrgsInChannel(channelInfoDto.getChannelName()));

			for (String org : orgs) {
				peerDtoArr.addAll(containerService.createMemberDtoArr("peer", org));
			}

			ordererDtoArr.addAll(containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg()));

			Network newwork = fabricClient.connectNetwork(channelInfoDto.getChannelName(), orgs.get(0), util.createFabrcSetting(channelInfoDto.getChannelName(), ordererDtoArr, peerDtoArr, orgs));

			channel = newwork.getChannel();

			for (int i = channelInfoDto.getChannelBlock(); i < channel.queryBlockchainInfo()
				.getHeight(); i++) {

				blockService.inspectBlock(channel.queryBlockByNumber(i), channelInfoDto);

				channelInfoDto.setChannelBlock(blockService.countBychannelBlock(channelInfoDto));
				channelInfoDto.setChannelTx(transactionService.countBychannelTransaction(channelInfoDto));
				channelService.saveChannelInfo(channelInfoDto);

			}

			fabricClient.testRegisterEventListener(channelInfoDto.getChannelName(), createBlockListener(channelInfoDto.getChannelName()));

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

	public ResultDto createOrg(ArrayList<CreateOrgConInfoDto> createOrgConInfoDtoArr) {

		logger.info("[조직생성] 시작");

		try {
			String                           ordererPorts      = "";
			String                           gossipBootAddress = "";
			JSONObject                       conJson           = new JSONObject();
			JSONObject                       returnJson        = new JSONObject();
			CopyOnWriteArrayList<ConInfoDto> conInfoDtoArr     = new CopyOnWriteArrayList<ConInfoDto>();

			// 컨테이너 생성시 필요한 변수 선언
			for (CreateOrgConInfoDto createOrgConInfoDto : createOrgConInfoDtoArr) {

				if (createOrgConInfoDto.getConType()
					.equals("orderer")) {

					ordererPorts = ordererPorts + createOrgConInfoDto.getConPort() + " ";

				}

				if (createOrgConInfoDto.getConType()
					.equals("peer")) {

					gossipBootAddress = gossipBootAddress + createOrgConInfoDto.getConType() + createOrgConInfoDto.getConNum() + ".org" + createOrgConInfoDto.getOrgName() + ".com:" + createOrgConInfoDto.getConPort() + " ";
				}
				conInfoDtoArr.add(ConInfoDto.builder()
					.conType(createOrgConInfoDto.getConType())
					.conPort(createOrgConInfoDto.getConPort())
					.orgName(createOrgConInfoDto.getOrgName())
					.orgType(createOrgConInfoDto.getOrgType())
					.conNum(createOrgConInfoDto.getConNum())
					.conCnt(createOrgConInfoDto.getConCnt())
					.build());
			}

			int i = 0;

			for (ConInfoDto conInfoDto : conInfoDtoArr) {

				if (conInfoDto.getConType()
					.equals("ca")) {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType() + " 컨테이너 생성");
//					containerService.saveConInfo(dockerService.createContainer(dto));
					returnJson = dockerService.createContainer(conInfoDto);
					conJson.put(returnJson.get("container_name"), returnJson);

					// setup 컨테이너 정보 생성 및 컨테이너 생성 함수 호출
					ConInfoDto setupContainer = new ConInfoDto();
					setupContainer.setOrgName(conInfoDto.getOrgName());
					setupContainer.setOrgType(conInfoDto.getOrgType());
					setupContainer.setConPort(conInfoDto.getConPort());
					setupContainer.setConCnt(conInfoDto.getConCnt());
					setupContainer.setConType("setup_" + conInfoDto.getOrgType());

					if (conInfoDto.getOrgType()
						.equals("orderer")) {

						setupContainer.setOrdererPorts(ordererPorts);
						setupContainer.setPeerOrgs(containerService.findConInfoByConType("ca", "peer"));

					}

					Thread.sleep(5000);

					logger.info("[조직생성] 도커 컨테이너 생성 -> " + setupContainer.getOrgName() + " 조직의 " + setupContainer.getConType() + " 컨테이너 생성");
					logger.info(setupContainer.toString());
					returnJson = dockerService.createContainer(setupContainer);
					conJson.put(returnJson.get("container_name"), returnJson);
					Thread.sleep(5000);

					conInfoDtoArr.add(i + 1, setupContainer);

				} else if (conInfoDto.getConType()
					.equals("peer")) {

					// 컨테이너 생성 함수 호출
					conInfoDto.setGossipBootAddr(gossipBootAddress);
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType() + conInfoDto.getConNum() + " 컨테이너 생성");
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

						logger.info("[조직생성] 도커 컨테이너 생성 -> " + couchdbContainer.getOrgName() + " 조직의 " + couchdbContainer.getConType() + couchdbContainer.getConNum() + " 컨테이너 생성");

						returnJson = dockerService.createContainer(couchdbContainer);
						conJson.put(returnJson.get("container_name"), returnJson);

					}

				} else {

					// 컨테이너 생성 함수 호출
					logger.info("[조직생성] 도커 컨테이너 생성 -> " + conInfoDto.getOrgName() + " 조직의 " + conInfoDto.getConType() + " 컨테이너 생성");
					conInfoDto.setConsoOrgs(containerService.findConInfoByConType("ca", "peer"));
					logger.info(conInfoDto.toString());

					returnJson = dockerService.createContainer(conInfoDto);
					conJson.put(returnJson.get("container_name"), returnJson);

				}

				Thread.sleep(2000);
				i++;
			}

			// 생성된 조직의 docker-compose yaml file 생성
			util.createYamlFile(conInfoDtoArr.get(0)
				.getOrgName(), conJson);

			String path = null;

			// 로컬 개발시 실서버에서 생성된 인증서,트렌젝션 다운로드
			if (environment.getActiveProfiles()[0].equals("local")) {

				if (conInfoDtoArr.get(0)
					.getOrgType()
					.equals("peer")) {

					path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0)
						.getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0)
								.getOrgName()
							+ ".com/msp/keystore/";
					sshClient.downloadFile(path, "server.key");

					path = "crypto-config/peerOrganizations/org" + conInfoDtoArr.get(0)
						.getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0)
								.getOrgName()
							+ ".com/msp/signcerts/";
					sshClient.downloadFile(path, "cert.pem");

				} else {

					path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0)
						.getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0)
								.getOrgName()
							+ ".com/msp/keystore/";
					sshClient.downloadFile(path, "server.key");
					path = "crypto-config/ordererOrganizations/org" + conInfoDtoArr.get(0)
						.getOrgName() + ".com/users/Admin@org"
							+ conInfoDtoArr.get(0)
								.getOrgName()
							+ ".com/msp/signcerts/";
					sshClient.downloadFile(path, "cert.pem");

				}
				path = "crypto-config/ca-certs/";
				sshClient.downloadFile(path, "ca.org" + conInfoDtoArr.get(0)
					.getOrgName() + ".com-cert.pem");
			}

			ArrayList<FabricMemberDto> fabricMemberDto = containerService.createMemberDtoArr(conInfoDtoArr.get(0)
				.getOrgType(),
					conInfoDtoArr.get(0)
						.getOrgName());
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
			ArrayList<FabricMemberDto> peerDtoArr = new ArrayList<FabricMemberDto>();

			for (String org : createChannelDto.getPeerOrgs()) {
				peerDtoArr.addAll(containerService.createMemberDtoArr("peer", org));
			}
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer", createChannelDto.getOrderingOrg());

			logger.info("[채널생성] peerDtoArr : " + peerDtoArr);
			logger.info("[채널생성] ordererDtoArr : " + ordererDtoArr);

			// 채널 생성시 필요한 wallet 생성
			fabricClient.createWallet(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())));

			for (FabricMemberDto peerDto : peerDtoArr) {

				fabricClient.createWallet(peerDto);
			}

			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(5000);
			// 로컬 개발시 채널생성 setup 컨테이너 기동하면서 생성된 채널트렌젝션 다운로드
			if (environment.getActiveProfiles()[0].equals("local")) {

				path = "channel-artifacts/" + conInfoDto.getOrgName() + "/";
				sshClient.downloadFile(path, conInfoDto.getOrgName() + ".tx");

			}

			for (FabricMemberDto peerDto : peerDtoArr) {
				FabricMemberDto ordererDto = ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size()));

				if (!containerService.isMemOfConso(ordererDto.getOrgName(), peerDto.getOrgName())) {

					JSONObject genesisJson = fabricClient.getChannelConfig(ordererDto, "testchainid");
					JSONObject testJson    = util.createOrgJson(peerDto);

					logger.info(genesisJson.toString());
					logger.info(testJson.toString());

					// 시스템 채널 컨소시움 추가

					JSONObject modifiedJson = util.modifyConsoConfig(genesisJson, testJson, "", peerDto.getOrgName());

					File       updateFile   = fabricClient.createUpdateFile(ordererDto, "testchainid", genesisJson, modifiedJson);

					fabricClient.setUpdate(ordererDto, ordererDto, "testchainid", updateFile);

					containerService.updateConsoOrgs(ordererDto.getOrgName(), peerDto.getOrgName());

				}
			}

			// 채널 생성 함수 시작
			logger.info("[채널생성] 시작");

			fabricClient.createChannel(peerDtoArr, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), createChannelDto.getChannelName());

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

			fabricClient.connectNetwork(channelInfoDto.getChannelName(), createChannelDto.getPeerOrgs()
				.get(0), util.createFabrcSetting(channelInfoDto.getChannelName(), ordererDtoArr, peerDtoArr, createChannelDto.getPeerOrgs()));

			fabricClient.testRegisterEventListener(createChannelDto.getChannelName(), createBlockListener(channelInfoDto.getChannelName()));

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
	 * @throws InterruptedException 
	 */

	public void joinChannel(ArrayList<FabricMemberDto> peerDtoArr, ArrayList<FabricMemberDto> ordererDtoArr, String channelName)
			throws InvalidArgumentException, ProposalException, CryptoException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException, TransactionException, InterruptedException {

		for (FabricMemberDto peerDto : peerDtoArr) {

			// 클라이언트 생성
			HFClient client = fabricClient.createClient(peerDto);

			// 채널 조인
			fabricClient.joinChannel(client, peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName);

			ChannelInfoPeerDto channelInfoPeerDto = new ChannelInfoPeerDto();

			channelInfoPeerDto.setAnchorYn(false);
			channelInfoPeerDto.setChannelInfoDto(channelService.findChannelInfoByChannelName(channelName));
			channelInfoPeerDto.setConInfoDto(containerService.findConInfoByConName(peerDto.getConName()));

			// 채널 가입한 피어정보 저장
			channelService.saveChannelInfoPeer(channelInfoPeerDto);

		}

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

		// 체인코드를 설치할 FabricMembetDto(peer) 생성
		ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", installCcDto.getOrgName());

		FabricMemberDto            peerDto    = null;

		for (FabricMemberDto peer : peerDtoArr) {
			if (peer.getConNum() == installCcDto.getConNum()) {
				peerDto = peer;

			}
		}

		try {

			// 체인코드 설치
			fabricClient.installChaincode(peerDto, installCcDto.getCcName(), installCcDto.getCcVersion());

			CcInfoPeerDto ccInfoPeerDto = new CcInfoPeerDto();

			// 설치한 체인코드 정보 조회
			CcInfoDto     ccInfoDto     = chaincodeService.findCcInfoById(installCcDto.getId());

			// 체인코드를 설치한 컨테이너 정보 조회
			ConInfoDto    conInfoDto    = containerService.findConInfoByConName(peerDto.getConName());

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

	public ResultDto activeChaincode(ActiveCcDto activeCcDto) throws Exception {

		logger.info("[체인코드 활성화] 시작");
		logger.info("[체인코드 활성화] activeCcDto : " + activeCcDto);

		logger.info("[체인코드 활성화] activeCcDto.getChannelName() : " + activeCcDto.getChannelName());
		
		try {

			CcInfoDto             ccInfoDto         = chaincodeService.findCcInfoById(activeCcDto.getId());
			ChannelInfoDto        channelInfoDto    = channelService.findChannelInfoByChannelName(activeCcDto.getChannelName());
			List<String>          orgs              = containerService.findOrgsInChannel(activeCcDto.getChannelName());
			List<FabricMemberDto> peerDtoArr        = new ArrayList<FabricMemberDto>();
			List<CcInfoPeerDto>   ccInfoPeerDtoList = new ArrayList<CcInfoPeerDto>();
			List<FabricMemberDto> ordererDtoArr     = new ArrayList<FabricMemberDto>();

			for (String org : orgs) {
				peerDtoArr.addAll(containerService.createMemberDtoArr("peer", org));
			}

			ccInfoPeerDtoList.addAll(chaincodeService.findByccInfoId(activeCcDto.getId()));
			ordererDtoArr.addAll(containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg()));
			System.out.println(ccInfoPeerDtoList);

			for (FabricMemberDto peerDto : peerDtoArr) {
				boolean flag = false;
				for (CcInfoPeerDto ccInfoPeerDto : ccInfoPeerDtoList) {
					if (peerDto.getConName()
						.equals(ccInfoPeerDto.getConInfoDto()
							.getConName())) {
						flag = true;
					}

				}

				if (!flag) {
					System.out.println(peerDto.getConName() + " 여기에 체인코드 설치 안됨");
					InstallCcDto installCcDto = new InstallCcDto();

					installCcDto.setCcName(activeCcDto.getCcName());
					installCcDto.setCcVersion(activeCcDto.getCcVersion());
					installCcDto.setId(activeCcDto.getId());
					installCcDto.setOrgName(peerDto.getOrgName());
					installCcDto.setConNum(peerDto.getConNum());

					installChaincode(installCcDto);

				}
			}

			CcInfoChannelDto ccInfoChannelDto;

			fabricClient.activeChaincode(peerDtoArr, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), activeCcDto.getChannelName(), orgs, activeCcDto.getCcName(), activeCcDto.getCcVersion());
			logger.info("[체인코드 인스턴스화] 종료");
			
			try {

				// 이미 인스턴스화가 진행된 체인코드인지 조회
//				ccInfoChannelDto = chaincodeService.findCcInfoChannelByChannelInfoAndCcInfo(channelInfoDto, ccInfoDto);
				ccInfoChannelDto = chaincodeService.findByChannelNameAndCcName(activeCcDto.getChannelName(), activeCcDto.getCcName());
				fabricClient.activeChaincode(peerDtoArr,ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), activeCcDto.getChannelName(), orgs, activeCcDto.getCcName(), activeCcDto.getCcVersion());

				ccInfoChannelDto.setCcVersion(activeCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 업데이트
				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);

			} catch (Exception e) {


				ccInfoChannelDto = new CcInfoChannelDto();
				ccInfoChannelDto.setCcInfoDto(ccInfoDto);
				ccInfoChannelDto.setChannelInfoDto(channelInfoDto);
				ccInfoChannelDto.setCcVersion(activeCcDto.getCcVersion());

				// 채널에 활성화된 체인코드정보 저장
				chaincodeService.saveCcInfoChannel(ccInfoChannelDto);
			}
			
			
		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success instantiate chaincode", null);

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
//			ArrayList<FabricMemberDto> peerDtoArr    = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr.get((int) (Math.random() * channelInfoPeerDtoArr.size()))
//				.getConInfoDto()
//				.getOrgName());
//
//			// 체인코드 인스턴스화를 진행할 FabricMembetDto(peer) 생성
//			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg());
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

	public ResultDto registerEventListener(String channelName) {

		logger.info("[채널 블럭 이벤트 등록] 시작");
		logger.info("[채널 블럭 이벤트 등록] channelName : " + channelName);

		try {

			// 이벤트 리슨을 등록할 채널 정보 조회
			ChannelInfoDto                channelInfoDto        = channelService.findChannelInfoByChannelName(channelName);

			// 이벤트 리슨을 등록할 피어 정보 조회
//			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto);
			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto.getChannelName());

			// 이벤트 리슨을 등록할 FabricMembetDto(peer) 생성
			ArrayList<FabricMemberDto>    peerDtoArr            = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr.get((int) (Math.random() * channelInfoPeerDtoArr.size()))
				.getConInfoDto()
				.getOrgName());

			// 이벤트 리슨을 등록할 FabricMembetDto(orderer) 생성
			ArrayList<FabricMemberDto>    ordererDtoArr         = containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg());

			ChannelHandleDto              channelHandleDto;
			try {

				// 이벤트 리슨이 등록된 채널인지 채널 핸들 조회
				channelHandleDto = channelService.findChannelHandleByChannel(channelName);

				// 이벤트 리슨이 등록된 채널이면 에러 발생
				throw new Exception("already registered event listener");

			} catch (IllegalArgumentException e) {

				channelHandleDto = new ChannelHandleDto();

				// 이벤트 리슨 등록
//				String handle = fabricClient.registerEventListener(
//						peerDtoArr.get((int) (Math.random() * peerDtoArr.size())),
//						ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName,
//						createBlockListener(channelName),
//						channelInfoDto.getChannelBlock() < 1 ? 0 : channelInfoDto.getChannelBlock() - 1);

				String handle = "zz";
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

		return util.setResult("0000", true, "Success register block event listener", null);
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
//			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto);
			ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelInfoDto.getChannelName());

			// 이벤트 리슨을 삭제할 FabricMemberDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr("peer", channelInfoPeerDtoArr.get((int) (Math.random() * channelInfoPeerDtoArr.size()))
				.getConInfoDto()
				.getOrgName());

			// 이벤트 리슨을 삭제할 FabricMemberDto(orderer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg());

			// 삭제할 채널 핸들 조회
			String channelHandle = channelService.findChannelHandleByChannel(channelName)
				.getHandle();

			// 이벤트 리슨을 삭제
			fabricClient.unregisterEventListener(peerDtoArr.get((int) (Math.random() * peerDtoArr.size())), ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName, channelHandle);

			// 채널 핸들 삭제
			channelService.deleteChannelHandle(channelName);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success unregister block event listener", null);
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
			ConInfoDto         conInfoDto         = containerService.findConInfoByConName(conName);

			// 앵커피어를 등록할 채널정보 조회
			ChannelInfoDto     channelInfoDto     = channelService.findChannelInfoByChannelName(channelName);

			// 앵커피어를 등록할 채널에 피어 정보 조회??
//			ChannelInfoPeerDto channelInfoPeerDto = channelService.findChannelInfoPeerByChannelNameAndConName(channelInfoDto, conInfoDto).get(0);
			ChannelInfoPeerDto channelInfoPeerDto = channelService.findChannelInfoPeerByChannelNameAndConName(channelInfoDto.getChannelName(), conInfoDto.getConName()).get(0);

//			System.out.println()
			// 조회한 피어에 앵커피어 설정이 되어있으면 에러발샐
			if (channelInfoPeerDto.isAnchorYn()) {
				throw new Exception(conName + " is already anchor peer");
			}

			// 앵커피어를 등록한 FabricMemberDto(peer) 생성
			ArrayList<FabricMemberDto> peerDtoArr = containerService.createMemberDtoArr(conInfoDto.getOrgType(), conInfoDto.getOrgName());
			FabricMemberDto            peerDto    = null;

			for (FabricMemberDto peerDto2 : peerDtoArr) {
				if (peerDto2.getConName()
					.equals(conName)) {
					peerDto = peerDto2;
				}
			}

			// 앵커피어를 등록을 진행할 FabricMemberDto(orderer) 생성
			ArrayList<FabricMemberDto> ordererDtoArr = containerService.createMemberDtoArr("orderer", channelInfoDto.getOrderingOrg());

			// wallet 생성
			fabricClient.createWallet(peerDto);
			fabricClient.createWallet(ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())));
			Thread.sleep(1000);

			// 채널 생성 함수 시작
			logger.info("[test] 시작");
//			fabricClient.test(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())),channelName);
			fabricClient.setAnchorConfig(peerDto, ordererDtoArr.get((int) (Math.random() * ordererDtoArr.size())), channelName);

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

	private Consumer<BlockEvent> createBlockListener(String channelName) {

		Consumer<BlockEvent> blockEventListener = blockEvent -> {

			// 채널 정보 조회
			ChannelInfoDto channelInfoDto = channelService.findChannelInfoByChannelName(channelName);
			try {
				blockService.inspectBlock(blockEvent, channelInfoDto);
			} catch (InvalidProtocolBufferException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			channelInfoDto.setChannelBlock(blockService.countBychannelBlock(channelInfoDto));
			channelInfoDto.setChannelTx(transactionService.countBychannelTransaction(channelInfoDto));
			channelService.saveChannelInfo(channelInfoDto);

			try {

				// 웹소캣 연결된 클라이언트에게 이벤트 전송
				webSocket.convertAndSend("/event", blockEvent.getBlockNumber() + " in " + blockEvent.getChannelId());

			} catch (MessagingException | InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
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
//					// TODO Auto-generated catch block
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
//					// TODO Auto-generated catch block
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

	public ResultDto ccFileUpload(MultipartFile ccFile, String ccName, String ccDesc, String ccLang, String ccVersion) {

		try {

			// 파일로 변경 작업
			InputStream inputStream = ccFile.getInputStream();
			File        file        = new File(System.getProperty("user.dir") + "/chaincode/src/");

			if (!file.exists()) {
				try {

					file.mkdirs();

				} catch (Exception e) {

					return util.setResult("9999", false, e.getMessage(), null);

				}

			} else {

			}

			util.makeFolder(System.getProperty("user.dir") + "/chaincode/src/");
			util.makeFolder(System.getProperty("user.dir") + "/chaincode/package/");

			OutputStream outputStream = new FileOutputStream(new File(System.getProperty("user.dir") + "/chaincode/src/" + ccFile.getOriginalFilename() + "_v" + ccVersion));
			int          i;

			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}

			outputStream.close();
			inputStream.close();

			util.unZip(System.getProperty("user.dir") + "/chaincode/src/", ccFile.getOriginalFilename() + "_v" + ccVersion, System.getProperty("user.dir") + "/chaincode/src/");

			String    ccPath    = fabricClient.packageChaincodeWithLifecycle(ccName, ccVersion);

			// 디비에 저장(CCINFO)
			CcInfoDto ccInfoDto = new CcInfoDto();

			ccInfoDto.setCcName(ccName);
			ccInfoDto.setCcDesc(ccDesc);
			ccInfoDto.setCcLang(ccLang);
			ccInfoDto.setCcPath(ccPath);
			ccInfoDto.setCcVersion(ccVersion);

			chaincodeService.saveCcInfo(ccInfoDto);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success chaincode file upload", null);
	}

}
