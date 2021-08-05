package com.brchain.core.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleApproveChaincodeDefinitionForMyOrgRequest;
import org.hyperledger.fabric.sdk.LifecycleChaincodePackage;
import org.hyperledger.fabric.sdk.LifecycleCommitChaincodeDefinitionProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleCommitChaincodeDefinitionRequest;
import org.hyperledger.fabric.sdk.LifecycleInstallChaincodeProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleInstallChaincodeRequest;
import org.hyperledger.fabric.sdk.LifecycleQueryChaincodeDefinitionProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleQueryInstalledChaincodesProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleQueryInstalledChaincodesProposalResponse.LifecycleQueryInstalledChaincodesResult;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryLifecycleQueryChaincodeDefinitionRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.UpdateChannelConfiguration;
import org.hyperledger.fabric.sdk.UpgradeProposalRequest;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.brchain.common.exception.BrchainException;
import com.brchain.core.fabric.dto.FabricNodeDto;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.BrchainUser;
import com.brchain.core.util.JsonUtil;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

/**
 * Fabric Node와 연결을 위한 클라이언트 클래스
 * 
 * @author jaehyeon
 *
 */
@Component
@RequiredArgsConstructor
public class FabricClient {

	private final Environment    environment;
	private final SshClient      sshClient;

	private final Util           util;
	private final JsonUtil       jsonUtil;

	@Value("${brchain.sourcedir}")
	private String               sourceDir;

	@Value("${brchain.logdir}")
	private String               logDir;

	@Value("${brchain.datadir}")
	private String               dataDir;

	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private Map<String, Network> networkMap = new HashMap<String, Network>();

	private Logger               logger     = LoggerFactory.getLogger(this.getClass());

	/**
	 * 패브릭 네트워크 연결 함수
	 * 
	 * @param channelName 채널 이름
	 * @param orgName     조직 이름
	 * @param fabricJson  connection.json
	 * 
	 * @return Network
	 * 
	 */

	
	public Network connectNetwork(String channelName, String orgName, JSONObject connectionJson) {

		logger.info("[fabric 네트워크 연결 시작] 조직 이름 :" + orgName + ", 채널 이름 : " + channelName);
		logger.info("[fabric 네트워크 연결 시작] connectionJson "+connectionJson.toString().replace("\\", ""));

		InputStream     is         = new ByteArrayInputStream(connectionJson.toString().replace("\\", "").getBytes());

		// 파라미터 설정
		Path            walletPath = Paths.get("wallet");
		Wallet          wallet;
		Gateway.Builder builder    = null;
		
		try {

			wallet  = Wallets.newFileSystemWallet(walletPath);

			builder = Gateway.createBuilder();

			builder.identity(wallet, orgName)
				.networkConfig(is)
				.discovery(false);

		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.WALLET_CREATE_ERROR);
		}

		Gateway gateway = builder.connect();
		Network network = gateway.getNetwork(channelName);

		channelMap.put(channelName, network.getChannel());
		networkMap.put(channelName, network);

		logger.info("[fabric 네트워크 연결 완료]");

		return network;
	}

	/**
	 * Wallet 생성 함수
	 * 
	 * @param fabricNodeDto 조직 정보
	 * 
	 */

	public void createWallet(FabricNodeDto fabricNodeDto) {

		try {

			String     orgName     = fabricNodeDto.getOrgName();                                                                   // 조직명
			String     orgType     = fabricNodeDto.getOrgType();                                                                   // 조직 타입(peer, orderer)

			String     certificate = new String(                                                                                   // 인증서 파일 경로
					Files.readAllBytes(Paths.get("crypto-config/" + orgType + "Organizations/org" + orgName
							+ ".com/users/Admin@org" + orgName + ".com/msp/signcerts/cert.pem")));

			String     keyPath     = "crypto-config/" + orgType + "Organizations/org" + orgName + ".com/users/Admin@org"           // 키 파일 경로
					+ orgName + ".com/msp/keystore/server.key";

			PrivateKey key         = null;
			Wallet     wallet      = Wallets.newFileSystemWallet(Paths.get("wallet"));

			logger.debug("[월랫 생성 시작] 조직 이름 :" + orgName + ", 조직 타입 : " + orgType);

			// 기존 생성한 월랫이 없으면 리턴
			if (wallet.get(fabricNodeDto.getOrgName()) != null) {
				logger.debug("[월랫 생성] 이미 " + orgName + " 월랫이 생성됨");
				return;
			}

			// 프라이빗키 초기화
			try (FileInputStream isKey = new FileInputStream(keyPath);
					BufferedReader brKey = new BufferedReader(new InputStreamReader(isKey));) {

				StringBuilder keyBuilder = new StringBuilder();

				for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
					if (line.indexOf("PRIVATE") == -1) {
						keyBuilder.append(line);
					}
				}

				byte[]              encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
				PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
				KeyFactory          kf      = KeyFactory.getInstance("EC");
				
				key = kf.generatePrivate(keySpec);

			}

			X509Enrollment enrollment = new X509Enrollment(key, certificate);

			Identity       user       = Identities.newX509Identity(fabricNodeDto.getOrgMspId(), enrollment); // 2.2

			// 지갑에 추가
			wallet.put(fabricNodeDto.getOrgName(), user);
			logger.debug("[월랫 생성 완료]");

		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | CertificateException e) {
			throw new BrchainException(e, BrchainStatusCode.WALLET_CREATE_ERROR);

		}

	}

	/**
	 * 채널 생성 함수
	 * 
	 * @param peerDto     피어 조직정보
	 * @param ordererDto  오더러 조직정보
	 * @param channelName 채널 이름
	 * 
	 */

	public void createChannel(ArrayList<FabricNodeDto> peerDtoArr, FabricNodeDto ordererDto, String channelName) {

		try {

			logger.info("[fabric 채널 생성 시작] 채널 이름 :" + channelName);

			FabricNodeDto peerDto     = peerDtoArr.get((int) (Math.random() * peerDtoArr.size()));

			// 가저온 인증서 정보로 user 생성
			BrchainUser   userContext = createContext(peerDto);

			// 클라이언트 생성
			HFClient      client      = createClient(peerDto);

			logger.debug("[fabric 채널 생성] 클라이언트 생성완료");

			Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(),
					createFabricProperties(ordererDto));

			logger.debug("[fabric 채널 생성] 오더러설정 완료");

			// 채널 생성
			Channel channel = client.newChannel(channelName, orderer,
					new ChannelConfiguration(new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")),
					client.getChannelConfigurationSignature(
							new ChannelConfiguration(
									new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")),
							userContext));

			channel.shutdown(true);

			logger.info("[fabric 채널 생성 완료]");

		} catch (InvalidArgumentException | TransactionException | IOException e) {
			throw new BrchainException(e, BrchainStatusCode.CHANNEL_CREATE_ERROR);
		}

	}

	/**
	 * 채널 가입 함수
	 * 
	 * @param client      HFClient 클라이언트
	 * @param peerDtoArr  가입할 피어 정보
	 * @param channelName 채널 이름
	 * 
	 */

	public void joinChannel(HFClient client, FabricNodeDto peerDto, FabricNodeDto ordererDto, String channelName) {

		try {

			logger.info("[fabric 채널 가입 시작] 채널 이름 :" + channelName + ", 가입 피어 : " + peerDto.getConName());

			// 오더러 정보 생성
			Properties ordererProps = createFabricProperties(ordererDto);
			Orderer    orderer      = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

			// 피어 정보 생성
			Properties peerProps    = createFabricProperties(peerDto);
			Peer       peer         = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

			// 채널 정보 생성
			Channel    channel      = client.newChannel(channelName);

			channel.addOrderer(orderer);
			channel.initialize();

			// 채널 조인
			channel.joinPeer(peer);

			logger.info("[fabric 채널 가입 완료] " + channelName + " 채널에  " + peerDto.getConName() + " 컨테이너 가입 완료");
			channel.shutdown(true);

		} catch (InvalidArgumentException | TransactionException | ProposalException e) {
			throw new BrchainException(e, BrchainStatusCode.CHANNEL_JOIN_ERROR);

		}

	}

	/**
	 * UserContext 생성 함수
	 * 
	 * @param fabricNodeDto 생성할 UserContext 정보
	 * 
	 * @return BrchainUser
	 * 
	 */

	public BrchainUser createContext(FabricNodeDto fabricNodeDto) {

		try {
			// wallet에서 피어의 인증서 정보를 가져옴
			Wallet       wallet   = Wallets.newFileSystemWallet(Paths.get("wallet"));
			X509Identity identity = (X509Identity) wallet.get(fabricNodeDto.getOrgName());

			StringWriter sw       = new StringWriter();

			// 암호키 설정
			sw.write("-----BEGIN CERTIFICATE-----\n");
			sw.write(DatatypeConverter.printBase64Binary(identity.getCertificate()
				.getEncoded())
				.replaceAll("(.{64})", "$1\n"));
			sw.write("\n-----END CERTIFICATE-----\n");

			X509Enrollment enrollment = new X509Enrollment(identity.getPrivateKey(), sw.toString());

			// 가저온 인증서 정보로 user 생성
			return new BrchainUser(fabricNodeDto.getOrgName(), fabricNodeDto.getOrgName(), fabricNodeDto.getOrgMspId(), enrollment);

		} catch (IOException | CertificateEncodingException e) {
			throw new BrchainException(e, BrchainStatusCode.FABRIC_CONTEXT_ERROR);
		}

	}

	/**
	 * HFClient 클라이언트 생성 함수
	 * 
	 * @param fabricNodeDto 생성할 클라이언트 정보
	 * 
	 * @return HFClient 클라이언트
	 * 
	 */

	public HFClient createClient(FabricNodeDto fabricNodeDto) {

		try {
			CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();

			// 클라이언트 생성
			HFClient    client      = HFClient.createNewInstance();
			client.setCryptoSuite(cryptoSuite);

			// UserConText 생성
			BrchainUser userContext = createContext(fabricNodeDto);
			client.setUserContext(userContext);

			return client;
		} catch (CryptoException | InvalidArgumentException | ClassNotFoundException | IllegalAccessException
				| InstantiationException | NoSuchMethodException | InvocationTargetException e) {
			throw new BrchainException(e, BrchainStatusCode.FABRIC_CLIENT_ERROR);
		}

	}

	/**
	 * 채널 설정 가져오기 함수
	 * 
	 * @param fabricNodeDto 채널에 접근할 정보
	 * @param channelName   채널 이름
	 * 
	 * @return 채널 설정 json
	 * @throws InterruptedException
	 * 
	 */

	public JSONObject getChannelConfig(FabricNodeDto fabricNodeDto, String channelName) throws InterruptedException {

		FileOutputStream outputStream = null;

		try {

			logger.info("[fabric 채널 설정 가져오기 시작] 채널 이름 :" + channelName);

			JSONParser jsonParser = new JSONParser();

			HFClient   client     = createClient(fabricNodeDto);

			// 채널 생성요청할 맴버 정보 생성
			Properties props      = createFabricProperties(fabricNodeDto);

			Channel    channel    = client.newChannel(channelName);

			byte[]     configBlock;

			// 설정 파일 가져오기(Byte Array)
			if (fabricNodeDto.getOrgType().equals("peer")) {

				Peer peer = client.newPeer(fabricNodeDto.getConName(), fabricNodeDto.getConUrl(), props);

				channel.addPeer(peer);
				configBlock = channel.getChannelConfigurationBytes(createContext(fabricNodeDto), peer);

			} else {

				Orderer orderer = client.newOrderer(fabricNodeDto.getConName(), fabricNodeDto.getConUrl(), props);

				channel.addOrderer(orderer);
				configBlock = channel.getChannelConfigurationBytes(createContext(fabricNodeDto), orderer);

			}

			// 설정 파일 가져오기(Byte Array) to File
			String path     = "channel-artifacts/" + fabricNodeDto.getOrgName() + "/";
			String fileName = channelName + "_config";

			// 폴더 생성
			util.createFolder(System.getProperty("user.dir") + "/" + path);

//			File   file     = new File(System.getProperty("user.dir") + "/" + path);
//
//			if (!file.exists()) {
//
//				file.mkdirs();
//
//			}

			outputStream = new FileOutputStream(new File(path + fileName + ".pb"));

			outputStream.write(configBlock);

			// 로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
			if (environment.getActiveProfiles()[0].equals("local")) {
				sshClient.uploadFile(path, fileName + ".pb");
			}

			// json 변경
			String command = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName
					+ ".pb --type common.Config > " + sourceDir + "/" + path + fileName + ".json";

			if (environment.getActiveProfiles()[0].equals("local")) {
				sshClient.execCommand(command);

				// 변경된 파일 다운로드
				sshClient.downloadFile(path, fileName + ".json");
				channel.shutdown(true);
			} else {
				util.execute(command);
			}

			logger.info("[fabric 채널 설정 가져오기 완료]");

			return (JSONObject) jsonParser.parse(new FileReader(System.getProperty("user.dir") + "/" + path + fileName + ".json"));

		} catch (InvalidArgumentException | TransactionException | IOException | ParseException e) {
			throw new BrchainException(e, BrchainStatusCode.GET_CHANNEL_CONFIG_ERROR);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new BrchainException(e, BrchainStatusCode.GET_CHANNEL_CONFIG_ERROR);
			}
		}

	}

	/**
	 * 업데이트 파일 생성 함수
	 * 
	 * @param fabricNodeDto  오더러에 접근할 맴버정보 DTO
	 * @param channelName    채널 이름
	 * @param config         기존 설정
	 * @param modifiedConfig 변경한 설정
	 * 
	 * @return 업데이트 파일
	 * @throws InterruptedException
	 * 
	 */

	public File createUpdateFile(FabricNodeDto fabricNodeDto, String channelName, JSONObject config,
			JSONObject modifiedConfig) throws InterruptedException {

		logger.info("[채널 업데이트 파일 생성 시작] 채널 이름 : " + channelName);

		String path     = "channel-artifacts/" + fabricNodeDto.getOrgName() + "/";
		String fileName = channelName + "_modified_config";

		// 폴더 생성
		util.createFolder(System.getProperty("user.dir") + "/" + path);

//		File   file     = new File(System.getProperty("user.dir") + "/" + path);
//
//		if (!file.exists()) {
//
//			file.mkdirs();
//
//		}

		try (FileWriter jsonFile = new FileWriter(path + fileName + ".json")) {

			jsonFile.write(modifiedConfig.toJSONString());
			jsonFile.flush();
			jsonFile.close();

		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.UPDATE_CHANNEL_CONFIG_ERROR);
		}

		// 로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.uploadFile(path, fileName + ".json");
		}

		// proto encode
		String command = "configtxlator proto_encode --input " + sourceDir + "/" + path + fileName + ".json"
				+ " --type common.Config --output " + sourceDir + "/" + path + fileName + ".pb";

		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		// 업데이트 계산
		command = "configtxlator compute_update --channel_id " + channelName + " --original " + sourceDir + "/" + path
				+ channelName + "_config.pb --updated " + sourceDir + "/" + path + fileName + ".pb --output "
				+ sourceDir + "/" + path + channelName + "_config_update.pb";
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		// proto decode
		fileName = channelName + "_config_update";
		command  = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName
				+ ".pb --type common.ConfigUpdate > " + sourceDir + "/" + path + fileName + ".json";
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		// 변경된 파일 다운로드
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.downloadFile(path, fileName + ".json");
			sshClient.downloadFile(path, fileName + ".pb");
		} else {
			util.execute(command);
		}

		logger.info("[채널 업데이트 파일 생성 완료]");

		return new File(System.getProperty("user.dir") + "/" + path + fileName + ".pb");

	}

	/**
	 * 업데이트 반영 함수
	 * 
	 * @param fabricNodeDto 업데이트를 진행할 맴버정보 DTO
	 * @param channelName   채널 이름
	 * @param updateFile    업데이트파일(pb)
	 * 
	 */

	public void setUpdate(FabricNodeDto fabricNodeDto, FabricNodeDto ordererDto, String channelName, File updateFile) {
		try {

			logger.info("[채널 업데이트 반영 시작] 채널 이름 : " + channelName + ", 오더러 : " + ordererDto.getConName() + ", 서명 노드 : "
					+ fabricNodeDto.getConName());

			HFClient   client  = createClient(ordererDto);

			// 채널 설정 변경 요청할 정보 생성
			Properties props   = createFabricProperties(ordererDto);

			Orderer    orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), props);
			Channel    channel = client.newChannel(channelName)
				.initialize();

			channel.addOrderer(orderer);

			// 업데이트 설정 생성
			UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateFile);
			BrchainUser                user                       = createContext(fabricNodeDto);

			// 업데이트 승인
			byte[]                     signers                    = channel.getUpdateChannelConfigurationSignature(updateChannelConfiguration, user);
			
			user = createContext(ordererDto);

			// 업데이트 리퀘스트 전송
			channel.updateChannelConfiguration(user, updateChannelConfiguration, orderer, signers);
			channel.shutdown(true);

			logger.info("[채널 업데이트 반영 완료]");

		} catch (InvalidArgumentException | TransactionException | IOException e) {
			throw new BrchainException(e, BrchainStatusCode.UPDATE_CHANNEL_CONFIG_ERROR);
		}

	}

	/**
	 * 체인코드 설치 함수 1.4
	 * 
	 * @deprecated fabric 1.4 버전에서만 지원
	 * 
	 * @param peerDto   설치할 피어 정보 DTO
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @throws Exception
	 * 
	 */

	public void installChaincodeToPeer(FabricNodeDto peerDto, String ccName, String ccVersion) throws Exception {

		// 클라이언트 생성
		HFClient               client                 = createClient(peerDto);

		// 체인코드 ID 생성
		ChaincodeID.Builder    chaincodeIDBuilder     = ChaincodeID.newBuilder()
			.setName(ccName)
			.setVersion(ccVersion)
			.setPath(ccName + "/");

		ChaincodeID            chaincodeID            = chaincodeIDBuilder.build();

		// 체인코드 설치 request 생성
		InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();

		installProposalRequest.setChaincodeID(chaincodeID);
		installProposalRequest.setUserContext(client.getUserContext());
		installProposalRequest.setChaincodeSourceLocation(new File(System.getProperty("user.dir") + "/chaincode"));
		installProposalRequest.setChaincodeVersion(ccVersion);

		// 설치할 피어 정보 생성
		List<Peer> peers = new ArrayList<Peer>();
		Peer       peer  = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), createFabricProperties(peerDto));
		peers.add(peer);

		logger.info("[체인코드 설치] 트렌젝션 생성 및 전송");
		Collection<ProposalResponse> responses = client.sendInstallProposal(installProposalRequest, peers);

		for (ProposalResponse response : responses) {

			if (response.getStatus()
				.name()
				.equals("FAILURE")) {
				throw new Exception("fail to install chaincode");
			}
		}

	}

	/**
	 * 프로퍼티 생성 함수
	 * 
	 * @param fabricNodeDto 프로퍼티를 생성할 맴버 DTO
	 * 
	 * @return fabricMember 프로퍼티
	 * 
	 */

	private Properties createFabricProperties(FabricNodeDto fabricNodeDto) {

		Properties props = new Properties();
		props.put("pemFile", "crypto-config/ca-certs/ca.org" + fabricNodeDto.getOrgName() + ".com-cert.pem");
		props.put("hostnameOverride", fabricNodeDto.getConName());

		return props;
	}

	/**
	 * 체인코드 인스턴스화 함수 1.4
	 * 
	 * @deprecated fabric 1.4 버전에서만 지원
	 * 
	 * @param peerDto     인스턴스화를 진행할 피어 정보 DTO
	 * @param ordererDto  인스턴스화를 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param ccName      체인코드 이름
	 * @param ccVersion   체인코드 버전
	 * @param ccLang      체인코드 언어
	 * 
	 * @throws Exception
	 * 
	 */

	public void instantiateChaincode(FabricNodeDto peerDto, FabricNodeDto ordererDto, String channelName, String ccName,
			String ccVersion, String ccLang, boolean upgrade) throws Exception {

		// 클라이언트 생성
		HFClient   client       = createClient(peerDto);

		// 오더러 정보 생성
		Properties ordererProps = createFabricProperties(ordererDto);
		Orderer    orderer      = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties peerProps    = createFabricProperties(peerDto);
		Peer       peer         = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

		// 채널 정보 생성
		Channel    channel      = client.newChannel(channelName);
		channel.addPeer(peer);

		channel.addOrderer(orderer);
		channel.initialize();

		// 체인코드 ID 생성
		ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder()
			.setName(ccName)
			.setVersion(ccVersion)
			.setPath(ccName + "/");

		ChaincodeID         chaincodeID        = chaincodeIDBuilder.build();

		Map<String, byte[]> tm                 = new HashMap<>();
		tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
		tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));

		TransactionRequest proposalRequest = null;

		// 체인코드 인스턴스화 리퀘스트 생성
		if (!upgrade) {
			proposalRequest = client.newInstantiationProposalRequest();
		} else {

			proposalRequest = client.newUpgradeProposalRequest();
		}

		proposalRequest.setChaincodeID(chaincodeID);
		proposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
		proposalRequest.setFcn("");
		proposalRequest.setArgs("");
		proposalRequest.setProposalWaitTime(300000);
//		proposalRequest.setTransientMap(tm);
		Collection<ProposalResponse> responses = null;

		// 리퀘스트 결과 오더러로 전송
		if (!upgrade) {
			responses = channel.sendInstantiationProposal((InstantiateProposalRequest) proposalRequest);
		} else {

			responses = channel.sendUpgradeProposal((UpgradeProposalRequest) proposalRequest);
		}

		for (ProposalResponse response : responses) {
			System.out.println(response.getMessage());
			if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
				channel.sendTransaction(responses, channel.getOrderers())
					.thenApply(transactionEvent -> {
						System.out.println(transactionEvent.isValid());
						System.out.println(transactionEvent.getTransactionID());

						return null;
					});
			} else {
				throw new Exception(response.getMessage());
			}

		}
		channel.shutdown(true);

	}

	/**
	 * 채널 이벤트 리스너 등록 함수
	 * 
	 * @param channelName 채널 이름
	 * @param listener    이벤트 리스너
	 * 
	 */

	public void registerEventListener(String channelName, Consumer<BlockEvent> listener) {

		logger.info("[이벤트 리스너 등록] 채널 이름 : " + channelName);

		// 이벤트 리슨용 채널 생성
		Network network = networkMap.get(channelName);

		// 블록 이벤트 리스너 등록
		network.addBlockListener(listener);

	}

	/**
	 * 앵커 피어 설정 함수
	 * 
	 * @param peerDto     설정할 피어 정보
	 * @param ordererDto  오더러 정보
	 * @param channelName 채널 이름
	 * 
	 * @throws InterruptedException
	 * 
	 */

	public void setAnchorConfig(FabricNodeDto peerDto, FabricNodeDto ordererDto, String channelName)
			throws InterruptedException {

		logger.info("[앵커피어 설정 시작] 채널 이름 : " + channelName + ", 설정할 피어 : " + peerDto.getConName());

		// 채널 설정 조회
		JSONObject genesisJson = getChannelConfig(peerDto, channelName);
		logger.debug("[앵커피어 설정] 기존 설정 : " + genesisJson);

		// 앵커피어 설정 추가
		JSONObject modifiedJson = jsonUtil.addAnchorConfig(genesisJson, jsonUtil.createAnchorJson(peerDto), "", peerDto);
		logger.debug("[앵커피어 설정] 변경된 설정 : " + modifiedJson.toString());

		// 파일 업데이트
		File updateFile = createUpdateFile(peerDto, channelName, genesisJson, modifiedJson);

		// 업데이트 반영
		setUpdate(peerDto, ordererDto, channelName, updateFile);
		logger.info("[앵커피어 설정 완료]");

	}

	/**
	 * 체인코드 설치 함수
	 * 
	 * @param peerDto   설치할 피어정보
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 */

	public void installChaincode(FabricNodeDto peerDto, String ccName, String ccVersion) {

		try {
			logger.info("[체인코드 설치 시작] 체인코드 이름 : " + ccName + ", 체인코드 버전 : " + ccVersion + ", 설치할 피어 : "
					+ peerDto.getConName());

			// 클라이언트 생성
			HFClient   client = createClient(peerDto);

			// 설치할 피어 정보 생성
			List<Peer> peers  = new ArrayList<Peer>();
			Peer       peer   = client.newPeer(peerDto.getConName(), peerDto.getConUrl(),
					createFabricProperties(peerDto));
			peers.add(peer);

			// 체인코드 설치 함수 시작
			installChaincodeWithLifecycle(client, peers, ccName, ccVersion);

			logger.info("[체인코드 설치 완료]");

		} catch (InvalidArgumentException | ProposalException | IOException e) {
			throw new BrchainException(e, BrchainStatusCode.CHAINCODE_INSTALL_ERROR);
		}

	}

	/**
	 * 체인코드 활성화 함수
	 * 
	 * @param peerDtoArr
	 * @param ordererDto
	 * @param channelName
	 * @param orgs
	 * @param ccName
	 * @param ccVersion
	 *  
	 * @throws Exception
	 * 
	 */
	
	public void activeChaincode(List<FabricNodeDto> peerDtoArr, FabricNodeDto ordererDto, String channelName,
			List<String> orgs, String ccName, String ccVersion) throws InterruptedException  {
		
		try {

			logger.info("[체인코드 활성화 시작] 채널 이름 : " + channelName + ", 체인코드 이름 : " + ccName + ", 체인코드 버전 : " + ccVersion);

			HFClient   client       = createClient(peerDtoArr.get(0));

			// 오더러 정보 생성
			Properties ordererProps = createFabricProperties(ordererDto);
			Orderer    orderer      = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

			// 피어 정보 생성
			Properties peerProps    = createFabricProperties(peerDtoArr.get(0));
			Peer       peer         = client.newPeer(peerDtoArr.get(0).getConName(), peerDtoArr.get(0).getConUrl(), peerProps);

			Channel    channel      = client.newChannel(channelName);

			Collection<LifecycleCommitChaincodeDefinitionProposalResponse> response     = new ArrayList<LifecycleCommitChaincodeDefinitionProposalResponse>();

			channel.addPeer(peer);
			channel.addOrderer(orderer);
			channel.initialize();

			long   sequence  = getChaincodeSequence(client, channel, ccName);
			String packageId = verifyChaincodeInstalled(client, channel.getPeers(), ccName, ccVersion);

			channel.removePeer(peer);

			// 체인코드 승인 확인 함수 시작
			for (String org : orgs) {
				for (FabricNodeDto peerDto : peerDtoArr) {
					if (org.equals(peerDto.getOrgName())) {
						client    = createClient(peerDto);
						peerProps = createFabricProperties(peerDto);
						peer      = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);
					}

				}

				logger.debug("Org : " + org + ", peer : " + peer.getName());
				channel.addPeer(peer);

				// 체인코드 승인 함수 시작
				approveChaincodeWithLifecycle(client, channel, ccName, packageId, ccVersion, sequence);
				channel.removePeer(peer);

			}

			Thread.sleep(1000);

			client    = createClient(peerDtoArr.get(0));
			peerProps = createFabricProperties(peerDtoArr.get(0));
			peer      = client.newPeer(peerDtoArr.get(0).getConName(), peerDtoArr.get(0).getConUrl(), peerProps);

			channel.addPeer(peer);

			// 체인코드 승인 함수 시작
			response.addAll(commitChaincodeWithLifecycle(client, channel, ccName, packageId, ccVersion, sequence));

			Thread.sleep(1000);
			channel.sendTransaction(response);
			// 체인코드 커밋 함수 시작

			logger.info("[체인코드 활성화 완료]");

			channel.shutdown(false);
			
		} catch (InvalidArgumentException | ProposalException | TransactionException e) {
			throw new BrchainException(e, BrchainStatusCode.CHAINCODE_INSTALL_ERROR);
		}
	}

	/**
	 * 체인코드 패키지 함수
	 * 
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @return 체인코드 경로
	 * 
	 * @throws Exception
	 */

	public String packageChaincodeWithLifecycle(String ccName, String ccVersion) {

		try {
			logger.info("[체인코드 라이프 사이클 패키징] 시작");

			Path                      metadataSourcePath        = null;

			// lifecycleChaincode 패키징
			LifecycleChaincodePackage lifecycleChaincodePackage = LifecycleChaincodePackage.fromSource(
					ccName + "_" + ccVersion, Paths.get(System.getProperty("user.dir") + "/chaincode/"),
					TransactionRequest.Type.GO_LANG, ccName+"/go/", metadataSourcePath);
			String                    ccPath                    = System.getProperty("user.dir") + "/chaincode/package/"
					+ ccName + "_v" + ccVersion + ".tar";

			lifecycleChaincodePackage.toFile(Paths.get(ccPath), StandardOpenOption.CREATE);

			logger.info("[체인코드 라이프 사이클 패키징] getLabel() : " + lifecycleChaincodePackage.getLabel());
			logger.info("[체인코드 라이프 사이클 패키징] getPath() : " + lifecycleChaincodePackage.getPath());
			logger.info("[체인코드 라이프 사이클 패키징] getType() : " + lifecycleChaincodePackage.getType());

			logger.info("[체인코드 라이프 사이클 패키징] 종료");

			return ccPath;
			
		} catch (InvalidArgumentException | IOException e) {
			throw new BrchainException(e, BrchainStatusCode.CHAINCDOE_PACKAGE_ERROR);
		}

	}

	/**
	 * 체인코드 설치 함수
	 * 
	 * @param client    HFClient 클라이언트
	 * @param peers     설치할 피어
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @throws IOException
	 * @throws InvalidArgumentException
	 * @throws ProposalException
	 * 
	 */

	private void installChaincodeWithLifecycle(HFClient client, List<Peer> peers, String ccName, String ccVersion)
			throws InvalidArgumentException, IOException, ProposalException {

		logger.debug("[체인코드 라이프 사이클 설치] 시작 : " + peers.get(0).getName());

		LifecycleChaincodePackage        lifecycleChaincodePackage = LifecycleChaincodePackage.fromFile(
				new File(System.getProperty("user.dir") + "/chaincode/package/" + ccName + "_v" + ccVersion + ".tar"));

		// lifecycleChaincode 설치 리퀘스트 생성
		LifecycleInstallChaincodeRequest installProposalRequest    = client.newLifecycleInstallChaincodeRequest();
		installProposalRequest.setLifecycleChaincodePackage(lifecycleChaincodePackage);

		// lifecycleChaincode 설치 리퀘스트 피어로 전송
		Collection<LifecycleInstallChaincodeProposalResponse> responses = client
			.sendLifecycleInstallChaincodeRequest(installProposalRequest, peers);

		// 설치 완료후 체인코드 패키지 ID 파싱
		for (LifecycleInstallChaincodeProposalResponse response : responses) {
			if (response.getStatus() == ProposalResponse.Status.SUCCESS) {

				logger.debug("[체인코드 라이프 사이클 설치] Successful install proposal response Txid : "
						+ response.getTransactionID() + " from peer " + response.getPeer().getName());

				logger.debug("[체인코드 라이프 사이클 설치] package ID : " + response.getPackageId());

			}

		}

		logger.debug("[체인코드 라이프 사이클 설치] 종료");

	}
	
	/**
	 * 체인코드 설치 확인 함수
	 * 
	 * @param client     HFClient 클라이언트
	 * @param collection peer collecton
	 * @param ccName     체인코드 이름
	 * @param ccVersion  체인코드 버전
	 * 
	 * @return 설치된 체인코드 패키지 ID
	 * 
	 * @throws ProposalException 
	 * @throws InvalidArgumentException 
	 * 
	 */

	private String verifyChaincodeInstalled(HFClient client, Collection<Peer> collection, String ccName,
			String ccVersion) throws InvalidArgumentException, ProposalException {

		logger.debug("[체인코드 설치 확인 시작] 체인코드 이름 : " + ccName+", 클라이언트 정보 : "+client.getUserContext().getName());

		String                                                        packageId = "";
		// 체인코드 설치확인 리퀘스트 피어로 전송
		Collection<LifecycleQueryInstalledChaincodesProposalResponse> results   = client
			.sendLifecycleQueryInstalledChaincodes(client.newLifecycleQueryInstalledChaincodesRequest(), collection);

		// 설치된 체인코드 확인
		for (LifecycleQueryInstalledChaincodesProposalResponse peerResults : results) {

			String peerName = peerResults.getPeer().getName();

			for (LifecycleQueryInstalledChaincodesResult lifecycleQueryInstalledChaincodesResult : peerResults
				.getLifecycleQueryInstalledChaincodesResult()) {

				if (lifecycleQueryInstalledChaincodesResult.getLabel()
					.equals(ccName + "_" + ccVersion)) {
					logger.debug("[체인코드 설치 확인] Peer : " + peerName);
					logger.debug("[체인코드 설치 확인] getLabel : "
							+ lifecycleQueryInstalledChaincodesResult.getLabel());
					logger.debug("[체인코드 설치 확인] getPackageId() : "
							+ lifecycleQueryInstalledChaincodesResult.getPackageId());
					packageId = lifecycleQueryInstalledChaincodesResult.getPackageId();
				}

			}
		}

		logger.debug("[체인코드 설치 확인 완료] 체인코드 패키지 ID  : " + packageId);
		
		return packageId;
	}

	/**
	 * 체인코드 현재 시퀀스 조회 함수
	 * 
	 * @param client        HFClient 클라이언트
	 * @param channel       패브릭 채널
	 * @param chaincodeName 체인코드 이름
	 * 
	 * @return 조회한 체인코드의 현재 시퀀스 번호
	 * 
	 * @throws InvalidArgumentException 
	 * 
	 */

	private long getChaincodeSequence(HFClient client, Channel channel, String chaincodeName) throws InvalidArgumentException {

		logger.debug("[체인코드 시퀀스 조회 시작] 채널 이름 : " + channel.getName() + ", 체인코드 이름 : " + chaincodeName);

		long sequence = 1L;

		// 현재 체인코드가 없으면 (namespace test-cc is not defined) 에러

		try {

			// 체인코드 시퀀스 확인 리퀘스트 생성
			QueryLifecycleQueryChaincodeDefinitionRequest queryLifecycleQueryChaincodeDefinitionRequest = client
				.newQueryLifecycleQueryChaincodeDefinitionRequest();
			queryLifecycleQueryChaincodeDefinitionRequest.setChaincodeName(chaincodeName);

			// 생성한 리퀘스트 전송
			Collection<LifecycleQueryChaincodeDefinitionProposalResponse> firstQueryDefininitions = channel
				.lifecycleQueryChaincodeDefinition(queryLifecycleQueryChaincodeDefinitionRequest, channel.getPeers());

			for (LifecycleQueryChaincodeDefinitionProposalResponse firstDefinition : firstQueryDefininitions) {

				logger.debug("[체인코드 시퀀스 조회] chaincode name : " + chaincodeName);
				logger.debug("[체인코드 시퀀스 조회] sequence : " + firstDefinition.getSequence());

				// 조회된 시퀀스 증가
				sequence = firstDefinition.getSequence() + 1L;

				logger.debug("[체인코드 시퀀스 조회] next sequence : " + sequence);
			}

			


		} catch (ProposalException e) {

			// 시퀀스 조회가 안될떄 시퀀스 번호를 1로 설정
			if (e.getMessage().contains("namespace " + chaincodeName + " is not defined")) {

				logger.debug("[체인코드 시퀀스 조회] sequence : x");
				logger.debug("[체인코드 시퀀스 조회] next sequence : " + sequence);
				logger.debug("[체인코드 시퀀스 조회 종료]");

				return sequence;
			}
			
			// 시퀀스 조회 에러가 아닐때
			throw new BrchainException(e, BrchainStatusCode.CHAINCODE_ACTIVE_ERROR);
		}

		return sequence;
	}

	/**
	 * 체인코드 승인 함수
	 * 
	 * @param client           HFClient 클라이언트
	 * @param channel          패브릭 채널
	 * @param chaincodeName    체인코드 이름
	 * @param packageID        체인코드 패키지 아이디
	 * @param chaincodeVersion 체인코드 버전
	 * @param sequence         체인코드 시퀀스
	 * 
	 * @throws InvalidArgumentException 
	 * @throws ProposalException 
	 * 
	 */

	private void approveChaincodeWithLifecycle(HFClient client, Channel channel, String chaincodeName, String packageID,
			String chaincodeVersion, long sequence) throws InvalidArgumentException, ProposalException {

		logger.debug("[체인코드 승인 시작] 체인코드 이름 : " + chaincodeName + ", 체인코드 버전 : " + chaincodeVersion + ", 시퀀스 번호 : "
				+ sequence);
		logger.debug("[체인코드 승인 시작] 체인코드 패키지 ID : " + packageID);
		logger.debug("[체인코드 승인 시작] 클라이언트 정보 : " + client.getUserContext().getName());

		// 체인코드 PDC 설정파일 설정
//		ChaincodeCollectionConfiguration                   collectionConfig                                   = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));

		// 체인코드 승인 리퀘스트 생성
		LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeDefinitionForMyOrgRequest = client
			.newLifecycleApproveChaincodeDefinitionForMyOrgRequest();

		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setSequence(sequence);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeName(chaincodeName);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeVersion(chaincodeVersion);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setInitRequired(false);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setPackageId(packageID);
		
		//체인코드 PDC 설정파일 설정
//		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeCollectionConfiguration(collectionConfig);

		// 생성한 리퀘스트 전송
		Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse = channel
			.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeDefinitionForMyOrgRequest,
					channel.getPeers());

		for (LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse response : lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse) {

			logger.debug("[체인코드 승인] message : " + response.getMessage());
			logger.debug("[체인코드 승인] status : " + response.getStatus());
		}

		// 체인코드 승인 결과 오더러로 전송
		channel.sendTransaction(lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse);

		logger.debug("[체인코드 승인 완료]");

	}

//	/**
//	 * 체인코드 승인 확인 함수
//	 * 
//	 * @param client           HFClient 클라이언트
//	 * @param channel          패브릭 채널
//	 * @param chaincodeName    체인코드 이름
//	 * @param packageID        체인코드 패키지 아이디
//	 * @param chaincodeVersion 체인코드 버전
//	 * @param sequence         체인코드 시퀀스
//	 * 
//	 * @throws Exception
//	 */
//
//	public void verifyChaincodeApproved(HFClient client, Channel channel, String chaincodeName, String packageID,
//			String chaincodeVersion, long sequence) throws Exception {
//
//		System.out.println("[verifyChaincodeApproved()] Start Verify Chaincode Approved");
//
//		// 체인코드 PDC 설정파일 설정
////		ChaincodeCollectionConfiguration     collectionConfig                     = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));
//
//		// 체인코드 승인 확인 리퀘스트 생성
//		LifecycleCheckCommitReadinessRequest lifecycleCheckCommitReadinessRequest = client
//			.newLifecycleSimulateCommitChaincodeDefinitionRequest();
//
//		lifecycleCheckCommitReadinessRequest.setSequence(sequence);
//		lifecycleCheckCommitReadinessRequest.setChaincodeName(chaincodeName);
//		lifecycleCheckCommitReadinessRequest.setChaincodeVersion(chaincodeVersion);
//		lifecycleCheckCommitReadinessRequest.setInitRequired(false);
////		lifecycleCheckCommitReadinessRequest.setChaincodeCollectionConfiguration(collectionConfig);
//
//		// 생성한 리퀘스트 전송
//		Collection<LifecycleCheckCommitReadinessProposalResponse> lifecycleSimulateCommitChaincodeDefinitionProposalResponse = channel
//			.sendLifecycleCheckCommitReadinessRequest(lifecycleCheckCommitReadinessRequest, channel.getPeers());
//
//		for (LifecycleCheckCommitReadinessProposalResponse response : lifecycleSimulateCommitChaincodeDefinitionProposalResponse) {
//
//			// 승인 완료한 조직리스트 확인
//			System.out.println("[verifyChaincodeApproved()] 승인한 조직 배열 : " + response.getApprovedOrgs());
//		}
//
//		System.out.println("[verifyChaincodeApproved()] Finish Verify Chaincode Approved");
//		System.out.println("");
//		System.out.println("");
//	}

	/**
	 * 체인코드 커밋 함수
	 * 
	 * @param client           HFClient 클라이언트
	 * @param channel          패브릭 채널
	 * @param chaincodeName    체인코드 이름
	 * @param packageID        체인코드 패키지 아이디
	 * @param chaincodeVersion 체인코드 버전
	 * @param sequence         체인코드 시퀀스
	 * 
	 * @return
	 * 
	 * @throws InvalidArgumentException 
	 * @throws ProposalException 
	 * 
	 */

	private Collection<LifecycleCommitChaincodeDefinitionProposalResponse> commitChaincodeWithLifecycle(HFClient client,
			Channel channel, String chaincodeName, String packageID, String chaincodeVersion, long sequence) throws InvalidArgumentException, ProposalException
			 {

		System.out.println("[commitChaincodeWithLifecycle()] Start Commit Chaincode With LifeCycle");

		// 체인코드 PDC 설정파일 설정
//		ChaincodeCollectionConfiguration          collectionConfig                          = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));

		// 체인코드 커밋 리퀘스트 생성
		LifecycleCommitChaincodeDefinitionRequest lifecycleCommitChaincodeDefinitionRequest = client
			.newLifecycleCommitChaincodeDefinitionRequest();

		lifecycleCommitChaincodeDefinitionRequest.setSequence(sequence);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeVersion(chaincodeVersion);
		lifecycleCommitChaincodeDefinitionRequest.setInitRequired(false);
//		lifecycleCommitChaincodeDefinitionRequest.setChaincodeCollectionConfiguration(collectionConfig);

		// 생성한 리퀘스트 전송
		return channel.sendLifecycleCommitChaincodeDefinitionProposal(lifecycleCommitChaincodeDefinitionRequest, channel.getPeers());

	}
	
	public void setRemoveOrgConfig(FabricNodeDto peerDto, FabricNodeDto ordererDto, String channelName, String orgName) throws InterruptedException {
		logger.info("[조직 삭제 설정 시작] 채널 이름 : " + channelName + ", 설정할 피어 : " + peerDto.getConName() + ", 삭제할 조직 : " + orgName);

		// 채널 설정 조회
		JSONObject configJson = getChannelConfig(ordererDto, channelName);
		logger.debug("[조직 삭제 설정] 기존 설정 : " + configJson);

		// 조직 삭제 설정 추가
		JSONObject modifiedJson = jsonUtil.modifyOrgConfig(configJson, "", orgName);
		logger.debug("[조직 삭제 설정] 변경된 설정 : " + modifiedJson.toString());

		// 파일 업데이트
		File updateFile = createUpdateFile(peerDto, channelName, configJson, modifiedJson);

		// 업데이트 반영
		setUpdate(peerDto, ordererDto, channelName, updateFile);
		logger.info("[조직 삭제 설정 완료]");
	}
	


};
