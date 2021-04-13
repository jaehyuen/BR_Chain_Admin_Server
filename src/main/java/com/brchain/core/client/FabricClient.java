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
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
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
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleApproveChaincodeDefinitionForMyOrgRequest;
import org.hyperledger.fabric.sdk.LifecycleChaincodePackage;
import org.hyperledger.fabric.sdk.LifecycleCheckCommitReadinessProposalResponse;
import org.hyperledger.fabric.sdk.LifecycleCheckCommitReadinessRequest;
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
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.brchain.core.fabric.dto.FabricMemberDto;
import com.brchain.core.util.BrchainUser;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FabricClient {

	private final Environment    environment;
	private final SshClient      sshClient;

	private final Util           util;

	@Value("${brchain.sourcedir}")
	private String               sourceDir;

	@Value("${brchain.logdir}")
	private String               logDir;

	@Value("${brchain.datadir}")
	private String               dataDir;

	private ArrayList<Channel>   channelListener = new ArrayList<Channel>();

	private Map<String, Channel> testChannelMap  = new HashMap<String, Channel>();
	private Map<String, Network> testNetworkMap  = new HashMap<String, Network>();

	private Logger               logger          = LoggerFactory.getLogger(this.getClass());

	/**
	 * 패브릭 네트워크 연결 함수 (테스트중)
	 * 
	 * @param channelName 채널명
	 * @param orgName     조직명
	 * @param fabricJson  connection.json
	 * 
	 * @return Network
	 * 
	 * @throws Exception
	 */

	public Network connectNetwork(String channelName, String orgName, JSONObject connectionJson) throws Exception {

		InputStream     is         = new ByteArrayInputStream(connectionJson.toString()
			.replace("\\", "")
			.getBytes());

		// 파라미터 설정

		Path            walletPath = Paths.get("wallet");
		Wallet          wallet     = Wallets.newFileSystemWallet(walletPath);          // 2.2
		Gateway.Builder builder    = Gateway.createBuilder();

		builder.identity(wallet, orgName)
			.networkConfig(is)
			.discovery(false);

		Gateway gateway = builder.connect();
		Network network = gateway.getNetwork(channelName);

		logger.info("[FabricHelper] Connection Success!");

		testChannelMap.put(channelName, network.getChannel());
		testNetworkMap.put(channelName, network);

		return network;
	}

	/**
	 * Wallet 생성 함수
	 * 
	 * @param memberDto 조직 정보
	 * 
	 * @throws Exception
	 */

	public void createWallet(FabricMemberDto memberDto) throws Exception {

		String         certPemFile = "crypto-config/ca-certs/ca.org" + memberDto.getOrgName() + "brord.com-cert.pem";
		PrivateKey     key         = null;
		String         certificate = null;
		InputStream    isKey       = null;
		BufferedReader brKey       = null;
		String         path        = null;
		Properties     props       = new Properties();

		props.put("pemFile", certPemFile);

		// ca client생성
		HFCAClient  caClient    = HFCAClient.createNewInstance(memberDto.getCaUrl(), props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault()
			.getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);

		// Wallet wallet = Wallet.createFileSystemWallet(Paths.get("wallet")); 1.4
		Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet")); // 2.2

//		// wallet경로에 인증서 체크 1.4
//		boolean walletExists = wallet.exists(memberDto.getOrgName());
//
//		if (walletExists) {
//			return;
//		}

		if (wallet.get(memberDto.getOrgName()) != null) { // 2.2
			return;
		}

		// 인증서 설정
		if (memberDto.getOrgType()
			.equals("peer")) {

			path        = "crypto-config/peerOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/keystore/server.key";
			certificate = new String(Files.readAllBytes(Paths.get("crypto-config/peerOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/signcerts/cert.pem")));
		} else {

			path        = "crypto-config/ordererOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/keystore/server.key";
			certificate = new String(Files.readAllBytes(Paths.get("crypto-config/ordererOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/signcerts/cert.pem")));
		}

		try {

			isKey = new FileInputStream(path);
			brKey = new BufferedReader(new InputStreamReader(isKey));
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

		} finally {

			isKey.close();
			brKey.close();

		}

		X509Enrollment enrollment = new X509Enrollment(key, certificate);

		// 인증서를 wallet형식으로 변환
//		Identity user = Identity.createIdentity(memberDto.getOrgMspId(), enrollment.getCert(), enrollment.getKey()); //1.4

		Identity       user       = Identities.newX509Identity(memberDto.getOrgMspId(), enrollment); // 2.2

		wallet.put(memberDto.getOrgName(), user);

	}

	/**
	 * 채널 생성 함수
	 * 
	 * @param peerDto     피어 조직정보
	 * @param ordererDto  오더러 조직정보
	 * @param channelName 채널명
	 * 
	 * @return HFClient
	 * 
	 * @throws Exception
	 */

	public HFClient createChannel(ArrayList<FabricMemberDto> peerDtoArr, FabricMemberDto ordererDto, String channelName) throws Exception {

		FabricMemberDto peerDto     = peerDtoArr.get((int) (Math.random() * peerDtoArr.size()));

		// 가저온 인증서 정보로 user 생성
		BrchainUser     userContext = createContext(peerDto);

		// 클라이언트 생성
		HFClient        client      = createClient(peerDto);
		Properties      props       = new Properties();

		logger.info("[채널생성] 클라이언트 생성완료");

		// 채널 생성요청할 오더러 점보 생성

		props.put("pemFile", "crypto-config/ca-certs/ca.org" + ordererDto.getOrgName() + ".com-cert.pem");
		props.put("hostnameOverride", ordererDto.getConName());

		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), props);

		logger.info("[채널생성] 오더러설정 완료");

		// 채널 생성
		Channel channel = client.newChannel(channelName, orderer, new ChannelConfiguration(new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")),
				client.getChannelConfigurationSignature(new ChannelConfiguration(new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")), userContext));

		channel.shutdown(true);

		return client;

	}

	/**
	 * 채널 가입 함수
	 * 
	 * @param client      HFClient 클라이언트
	 * @param peerDtoArr  가입할 피어 정보
	 * @param channelName 채널명
	 * 
	 * @throws InvalidArgumentException
	 * @throws ProposalException
	 * @throws IOException
	 * @throws TransactionException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws CryptoException
	 */

	public void joinChannel(HFClient client, FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName)
			throws InvalidArgumentException, ProposalException, CryptoException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException {

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

		logger.info("[채널가입] : " + peerDto.getConName() + " 컨테이너 " + channelName + " 채널 가입완료");
		channel.shutdown(true);

	}

	/**
	 * UserContext 생성 함수
	 * 
	 * @param memberDto 생성할 UserContext 정보
	 * 
	 * @return BrchainUser
	 * 
	 * @throws IOException
	 */

	public BrchainUser createContext(FabricMemberDto memberDto) throws IOException {

		// wallet에서 피어의 인증서 정보를 가져옴
		// Wallet wallet = Wallet.createFileSystemWallet(Paths.get("wallet")); //1.4
		Wallet       wallet   = Wallets.newFileSystemWallet(Paths.get("wallet"));
		X509Identity identity = (X509Identity) wallet.get(memberDto.getOrgName());

		StringWriter sw       = new StringWriter();
		try {
			sw.write("-----BEGIN CERTIFICATE-----\n");
			sw.write(DatatypeConverter.printBase64Binary(identity.getCertificate()
				.getEncoded())
				.replaceAll("(.{64})", "$1\n"));
			sw.write("\n-----END CERTIFICATE-----\n");
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}

		X509Enrollment enrollment = new X509Enrollment(identity.getPrivateKey(), sw.toString()); // 2.2

		// 가저온 인증서 정보로 user 생성
		return new BrchainUser(memberDto.getOrgName(), memberDto.getOrgName(), memberDto.getOrgMspId(), enrollment);

	}

	/**
	 * HFClient 클라이언트 생성 함수
	 * 
	 * @param memberDto 생성할 클라이언트 정보
	 * 
	 * @return HFClient 클라이언트
	 * 
	 * @throws IOException
	 * @throws CryptoException
	 * @throws InvalidArgumentException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */

	public HFClient createClient(FabricMemberDto memberDto) throws IOException, CryptoException, InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault()
			.getCryptoSuite();

		// 클라이언트 생성
		HFClient    client      = HFClient.createNewInstance();
		client.setCryptoSuite(cryptoSuite);

		// UserConText 생성
		BrchainUser userContext = createContext(memberDto);
		client.setUserContext(userContext);

		return client;

	}

	/**
	 * 이벤트 리슨용 채널 설정 함수
	 * 
	 * @param peerDto     피어 정보 DTO
	 * @param ordererDto  오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param startCnt    이벤트 리슨 시작 블록 번호
	 * 
	 * @return 설정된 채널
	 * 
	 * @throws InvalidArgumentException
	 * @throws CryptoException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws TransactionException
	 * 
	 */
	public Channel initChannel(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName, int startCnt)
			throws InvalidArgumentException, CryptoException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException, TransactionException {

		// 성공 여부 플래그?
		boolean flag  = true;
		// 인덱스정보?
		int     index = 0;

		// 채널 리스트에 채널이있는지 조회
		if (!channelListener.isEmpty()) {
			for (int i = 0; i < channelListener.size(); i++) {
				Channel channel = channelListener.get(i);
				if ((channel.getName()
					.equals(channelName))) {
					System.out.println("channel arr에 존재 : " + channelName);
					flag  = false;
					index = i;
				}
			}
		}

		Channel channel = null;

		// 채널이 없다면 등록
		if (flag) {

			// 클라이언트 생성
			HFClient client = createClient(peerDto);

			// 채널 등록
			channel = client.newChannel(channelName);

			// 피어 설정
			Properties          props = createFabricProperties(peerDto);
			Peer                peer  = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), props);

			// 이벤트 리슨 시작 위치 설정
			Channel.PeerOptions opt   = Channel.PeerOptions.createPeerOptions();

			opt.startEvents(startCnt);
			channel.addPeer(peer, opt);

			client = createClient(ordererDto);
			props  = createFabricProperties(ordererDto);

			Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), props);
			channel.addOrderer(orderer);

			channel.initialize();
			channelListener.add(channel);
		}

		if (channel == null) {
			channel = channelListener.get(index);
		}

		return channel;
	}

	/**
	 * 채널 설정 가져오기 함수(테스트중)
	 * 
	 * @param memberDto   채널에 접근할 정보
	 * @param channelName 채널명
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */

	public JSONObject getChannelConfig(FabricMemberDto memberDto, String channelName) throws Exception {

		JSONParser jsonParser = new JSONParser();

		HFClient   client     = createClient(memberDto);

		// 채널 생성요청할 맴버 정보 생성
		Properties props      = createFabricProperties(memberDto);

		Orderer    orderer    = client.newOrderer(memberDto.getConName(), memberDto.getConUrl(), props);
		Peer       peer       = client.newPeer(memberDto.getConName(), memberDto.getConUrl(), props);

		Channel    channel    = client.newChannel(channelName);

		byte[]     configBlock;

		// 설정 파일 가져오기(Byte Array)
		if (memberDto.getOrgType()
			.equals("peer")) {
			channel.addPeer(peer);
			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), peer);

		} else {
			channel.addOrderer(orderer);
			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), orderer);

		}

		// 설정 파일 가져오기(Byte Array) to File
		String path     = "channel-artifacts/" + memberDto.getOrgName() + "/";
		String fileName = channelName + "_config";

		try {

			File file = new File(System.getProperty("user.dir") + "/" + path);

			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

			FileOutputStream outputStream = new FileOutputStream(new File(path + fileName + ".pb"));
			outputStream.write(configBlock);
			outputStream.close();

		} catch (Throwable e) {

			e.printStackTrace(System.out);

		}

		// 로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.uploadFile(path, fileName + ".pb");
		}

		Thread.sleep(2000);

		// json 변경
		String command = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName + ".pb --type common.Config > " + sourceDir + "/" + path + fileName + ".json";
		logger.info("command :"+command);
		
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
			Thread.sleep(1000);

			// 변경된 파일 다운로드
			sshClient.downloadFile(path, fileName + ".json");
			channel.shutdown(true);
		} else {
			util.execute(command);
		}

		Thread.sleep(2000);
		return (JSONObject) jsonParser.parse(new FileReader(System.getProperty("user.dir") + "/" + path + fileName + ".json"));

	}

	/**
	 * 업데이트 파일 생성 함수
	 * 
	 * @param memberDto      오더러에 접근할 맴버정보 DTO
	 * @param channelName    채널명
	 * @param config         기존 설정
	 * @param modifiedConfig 변경한 설정
	 * 
	 * @return 업데이트 파일
	 * 
	 * @throws Exception
	 */

	@SuppressWarnings("unchecked")
	public File createUpdateFile(FabricMemberDto memberDto, String channelName, JSONObject config, JSONObject modifiedConfig) throws Exception {

		Util       util       = new Util();
		JSONParser jsonParser = new JSONParser();

		String     path       = "channel-artifacts/" + memberDto.getOrgName() + "/";
		String     fileName   = channelName + "_modified_config";

		try {

			File file = new File(System.getProperty("user.dir") + "/" + path);

			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

			FileWriter jsonFile = new FileWriter(path + fileName + ".json");
			jsonFile.write(modifiedConfig.toJSONString());
			jsonFile.flush();

		} catch (Throwable e) {

			e.printStackTrace(System.out);

		}

		// 로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.uploadFile(path, fileName + ".json");
		}

		// proto encode
		String command = "configtxlator proto_encode --input " + sourceDir + "/" + path + fileName + ".json" + " --type common.Config --output " + sourceDir + "/" + path + fileName + ".pb";

		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		// 업데이트 계산
		command = "configtxlator compute_update --channel_id " + channelName + " --original " + sourceDir + "/" + path + channelName + "_config.pb --updated " + sourceDir + "/" + path + fileName + ".pb --output " + sourceDir + "/" + path + channelName + "_config_update.pb";
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		// proto decode
		fileName = channelName + "_config_update";
		command  = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName + ".pb --type common.ConfigUpdate > " + sourceDir + "/" + path + fileName + ".json";
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.execCommand(command);
		} else {
			util.execute(command);
		}

		Thread.sleep(1000);

		// 변경된 파일 다운로드

		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.downloadFile(path, fileName + ".json");
			sshClient.downloadFile(path, fileName + ".pb");
		} else {
			util.execute(command);
		}

		return new File(System.getProperty("user.dir") + "/" + path + fileName + ".pb");

	}

	/**
	 * 업데이트 반영 함수
	 * 
	 * @param memberDto   업데이트를 진행할 맴버정보 DTO
	 * @param channelName 채널명
	 * @param updateFile  업데이트파일(pb)
	 * 
	 * @throws InvalidArgumentException
	 * @throws CryptoException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IOException
	 * @throws TransactionException
	 */

	public void setUpdate(FabricMemberDto memberDto, FabricMemberDto ordererDto, String channelName, File updateFile)
			throws InvalidArgumentException, CryptoException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException, TransactionException {

		HFClient   client  = createClient(ordererDto);

		// 채널 설정 변경 요청할 정보 생성
		Properties props   = createFabricProperties(ordererDto);

		Orderer    orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), props);
		Channel    channel = client.newChannel(channelName)
			.initialize();

		channel.addOrderer(orderer);

		// 업데이트 설정 생성
		UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateFile);
		BrchainUser                user                       = createContext(memberDto);

		// 업데이트 승인
		byte[]                     signers                    = channel.getUpdateChannelConfigurationSignature(updateChannelConfiguration, user);
		user = createContext(ordererDto);

		// 업데이트 리퀘스트 전송
		channel.updateChannelConfiguration(user, updateChannelConfiguration, orderer, signers);

		channel.shutdown(true);

	}

	/**
	 * 체인코드 설치 함수 1.4
	 * 
	 * @param peerDto   설치할 피어 정보 DTO
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * @throws Exception
	 */

	public void installChaincodeToPeer(FabricMemberDto peerDto, String ccName, String ccVersion) throws Exception {

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
	 * @param memberDto 프로퍼티를 생성할 맴버 DTO
	 * 
	 * @return
	 */

	public Properties createFabricProperties(FabricMemberDto memberDto) {

		Properties props = new Properties();
		props.put("pemFile", "crypto-config/ca-certs/ca.org" + memberDto.getOrgName() + ".com-cert.pem");
		props.put("hostnameOverride", memberDto.getConName());

		return props;
	}

	/**
	 * 체인코드 인스턴스화 함수
	 * 
	 * @param peerDto     인스턴스화를 진행할 피어 정보 DTO
	 * @param ordererDto  인스턴스화를 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param ccName      체인코드 이름
	 * @param ccVersion   체인코드 버전
	 * @param ccLang      체인코드 언어
	 * 
	 * @throws Exception
	 */

	public void instantiateChaincode(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName, String ccName, String ccVersion, String ccLang, boolean upgrade) throws Exception {

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
	 * 채널 블록 이벤트 등록 함수
	 * 
	 * @param peerDto     이벤트 등록을 진행할 피어 정보 DTO
	 * @param ordererDto  이벤트 등록을 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param listener    등록할 리스너
	 * @param startCnt    이벤트 시작 위치
	 * 
	 * @return 이벤트 핸들러
	 * 
	 * @throws Exception
	 */

	public String registerEventListener(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName, BlockListener listener, int startCnt) throws Exception {

		// 이벤트 리슨용 채널 생성
		Channel channel = initChannel(peerDto, ordererDto, channelName, startCnt);

		// 블록 이벤트 리스너 등록
		return channel.registerBlockListener(listener);

	}

	public void testRegisterEventListener(String channelName, Consumer<BlockEvent> listener) throws Exception {

		// 이벤트 리슨용 채널 생성
		Channel channel = testChannelMap.get(channelName);
		Network network = testNetworkMap.get(channelName);

		// 블록 이벤트 리스너 등록
		network.addBlockListener(listener);
//		return channel.registerBlockListener(listener);

	}

	/**
	 * 채널 블록 이벤트 삭제 함수
	 * 
	 * @param peerDto     이벤트 삭제를 진행할 피어 정보 DTO
	 * @param ordererDto  이벤트 삭제를 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param handle      이벤트 리스너 핸들
	 * 
	 * @throws Exception
	 */

	public void unregisterEventListener(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName, String handle) throws Exception {

		// 이벤트 리슨용 채널 생성
		Channel channel = initChannel(peerDto, ordererDto, channelName, 0);

		System.out.println("in Unregister Listenrt func");
		System.out.println("handle is " + handle);

		// 블록 이벤트 리스너 삭제
		channel.unregisterBlockListener(handle);
		channelListener.remove(channel);

	}

	public void setAnchorConfig(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName) throws Exception {
		Util       util        = new Util();

		// 채널 설정 조회
		JSONObject genesisJson = getChannelConfig(peerDto, channelName);
		logger.info("[앵커피어 설정] 기존 설정 : " + genesisJson);

		// 앵커피어 설정 추가
		JSONObject modifiedJson = util.modifyAnchorConfig(genesisJson, util.createAnchorJson(peerDto), "", peerDto);
		logger.info("[앵커피어 설정] 변경된 설정 : " + modifiedJson.toString());

		// 파일 업데이트
		File updateFile = createUpdateFile(peerDto, channelName, genesisJson, modifiedJson);

		setUpdate(peerDto, ordererDto, channelName, updateFile);

	}

	/*
	 * ########################################################################
	 * 
	 * TEST to 2.2 chaincode install
	 * 
	 * ########################################################################
	 */

	/**
	 * 체인코드 설치 함수
	 * 
	 * @param peerDto   설치할 피어정보
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @throws Exception
	 */

	public void installChaincode(FabricMemberDto peerDto, String ccName, String ccVersion) throws Exception {

		// 클라이언트 생성
		HFClient   client = createClient(peerDto);

//				// 설치할 피어 정보 생성
		List<Peer> peers  = new ArrayList<Peer>();
		Peer       peer   = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), createFabricProperties(peerDto));
		peers.add(peer);

		// 체인코드 설치 함수 시작
		installChaincodeWithLifecycle(client, peers, ccName, ccVersion);

	}

	public void activeChaincode(List<FabricMemberDto> peerDtoArr, FabricMemberDto ordererDto, String channelName, List<String> orgs, String ccName, String ccVersion) throws Exception {
		HFClient                                                       client       = createClient(peerDtoArr.get(0));

		// 오더러 정보 생성
		Properties                                                     ordererProps = createFabricProperties(ordererDto);
		Orderer                                                        orderer      = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties                                                     peerProps    = createFabricProperties(peerDtoArr.get(0));
		Peer                                                           peer         = client.newPeer(peerDtoArr.get(0).getConName(),peerDtoArr.get(0).getConUrl(),peerProps);

		Channel                                                        channel      = client.newChannel(channelName);
		Collection<LifecycleCommitChaincodeDefinitionProposalResponse> response     = new ArrayList<LifecycleCommitChaincodeDefinitionProposalResponse>();

		channel.addPeer(peer);
		channel.addOrderer(orderer);
		channel.initialize();

		long   sequence  = getChaincodeSequence(client, channel, ccName);
		String packageId = verifyChaincodeInstalled(client, channel.getPeers(), ccName, ccVersion);
		
		channel.removePeer(peer);

		System.out.println(sequence + "  sequencesequencesequence");
		System.out.println(packageId + "  packageIdpackageIdpackageId");

		// 체인코드 승인 확인 함수 시작
		for (String org : orgs) {
			for (FabricMemberDto peerDto : peerDtoArr) {
				if (org.equals(peerDto.getOrgName())) {
					client    = createClient(peerDto);
					peerProps = createFabricProperties(peerDto);
					peer      = client.newPeer(peerDto.getConName(),peerDto.getConUrl(),peerProps);
				}

			}
			
			System.out.println("Org : "+org+", peer : "+peer.getName());
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

		channel.shutdown(false);
	}

	/**
	 * 체인코드 패키지 함수
	 * 
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */

	public String packageChaincodeWithLifecycle(String ccName, String ccVersion) throws Exception {

		logger.info("[체인코드 라이프 사이클 패키징] 시작");

		Path                      metadataSourcePath        = null;

		// lifecycleChaincode 패키징
		LifecycleChaincodePackage lifecycleChaincodePackage = LifecycleChaincodePackage.fromSource(ccName + "_" + ccVersion, Paths.get(System.getProperty("user.dir") + "/chaincode/"), TransactionRequest.Type.GO_LANG, "test-cc/go/", metadataSourcePath);
		String                    ccPath                    = System.getProperty("user.dir") + "/chaincode/package/" + ccName + "_v" + ccVersion + ".tar";

		lifecycleChaincodePackage.toFile(Paths.get(ccPath), StandardOpenOption.CREATE);

		logger.info("[체인코드 라이프 사이클 패키징] getLabel() : " + lifecycleChaincodePackage.getLabel());
		logger.info("[체인코드 라이프 사이클 패키징] getPath() : " + lifecycleChaincodePackage.getPath());
		logger.info("[체인코드 라이프 사이클 패키징] getType() : " + lifecycleChaincodePackage.getType());

		logger.info("[체인코드 라이프 사이클 패키징] 종료");

		return ccPath;

	}

	/**
	 * 체인코드 설치 함수
	 * 
	 * @param client    HFClient 클라이언트
	 * @param peers     설치할 피어
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * 
	 * @throws Exception
	 */

	private void installChaincodeWithLifecycle(HFClient client, List<Peer> peers, String ccName, String ccVersion) throws Exception {

		logger.info("[체인코드 라이프 사이클 설치] 시작 : " + peers.get(0)
			.getName());

		LifecycleChaincodePackage        lifecycleChaincodePackage = LifecycleChaincodePackage.fromFile(new File(System.getProperty("user.dir") + "/chaincode/package/" + ccName + "_v" + ccVersion + ".tar"));

		// lifecycleChaincode 설치 리퀘스트 생성
		LifecycleInstallChaincodeRequest installProposalRequest    = client.newLifecycleInstallChaincodeRequest();
		installProposalRequest.setLifecycleChaincodePackage(lifecycleChaincodePackage);

		// lifecycleChaincode 설치 리퀘스트 피어로 전송
		Collection<LifecycleInstallChaincodeProposalResponse> responses = client.sendLifecycleInstallChaincodeRequest(installProposalRequest, peers);

		// 설치 완료후 체인코드 패키지 ID 파싱
		for (LifecycleInstallChaincodeProposalResponse response : responses) {
			if (response.getStatus() == ProposalResponse.Status.SUCCESS) {

				logger.info("[체인코드 라이프 사이클 설치] Successful install proposal response Txid : " + response.getTransactionID() + " from peer " + response.getPeer()
					.getName());

				logger.info("[체인코드 라이프 사이클 설치] package ID : " + response.getPackageId());

			}

		}

		logger.info("[체인코드 라이프 사이클 설치] 종료");

	}

	/**
	 * 체인코드 설치 확인 함수
	 * 
	 * @param client  HFClient 클라이언트
	 * @param channel 패브릭 채널
	 * 
	 * @throws Exception
	 */

	private String verifyChaincodeInstalled(HFClient client, Collection<Peer> collection, String ccName, String ccVersion) throws Exception {

		System.out.println("[verifyChaincodeInstalled()] Start Verify Chaincode Installed");

		String                                                        packageId = "";
		// 체인코드 설치확인 리퀘스트 피어로 전송
		Collection<LifecycleQueryInstalledChaincodesProposalResponse> results   = client.sendLifecycleQueryInstalledChaincodes(client.newLifecycleQueryInstalledChaincodesRequest(), collection);

		// 설치된 체인코드 확인
		for (LifecycleQueryInstalledChaincodesProposalResponse peerResults : results) {

			String peerName = peerResults.getPeer()
				.getName();

			for (LifecycleQueryInstalledChaincodesResult lifecycleQueryInstalledChaincodesResult : peerResults.getLifecycleQueryInstalledChaincodesResult()) {

				if (lifecycleQueryInstalledChaincodesResult.getLabel()
					.equals(ccName + "_" + ccVersion)) {
					System.out.println("[verifyChaincodeInstalled()] Peer : " + peerName);
					System.out.println("[verifyChaincodeInstalled()] getLabel : " + lifecycleQueryInstalledChaincodesResult.getLabel());
					System.out.println("[verifyChaincodeInstalled()] getPackageId() : " + lifecycleQueryInstalledChaincodesResult.getPackageId());
					packageId = lifecycleQueryInstalledChaincodesResult.getPackageId();
				}

			}
		}

		System.out.println("[verifyChaincodeInstalled()] Finish Verify Chaincode Installed");
		System.out.println("");
		System.out.println("");

		return packageId;
	}

	/**
	 * 체인코드 현재 시퀀스 조회 함수
	 * 
	 * @param client        HFClient 클라이언트
	 * @param channel       패브릭 채널
	 * @param chaincodeName 체인코드 이름
	 * 
	 * @return 조회한 체인코드희 현재 시퀀스 번호
	 * @throws Exception
	 */

	private long getChaincodeSequence(HFClient client, Channel channel, String chaincodeName) throws Exception {

		System.out.println("[getChaincodeSequence()] Start Get Chaincode Sequence");

		long sequence = 1L;

		// TODO 체인코드가 없을떄는 시퀀스가 1로 시작되야함
		// 현재 체인코드가 없으면 (namespace test-cc is not defined) 에러

		try {

			// 체인코드 시퀀스 확인 리퀘스트 생성
			QueryLifecycleQueryChaincodeDefinitionRequest queryLifecycleQueryChaincodeDefinitionRequest = client.newQueryLifecycleQueryChaincodeDefinitionRequest();
			queryLifecycleQueryChaincodeDefinitionRequest.setChaincodeName(chaincodeName);

			// 생성한 리퀘스트 전송
			Collection<LifecycleQueryChaincodeDefinitionProposalResponse> firstQueryDefininitions = channel.lifecycleQueryChaincodeDefinition(queryLifecycleQueryChaincodeDefinitionRequest, channel.getPeers());

			for (LifecycleQueryChaincodeDefinitionProposalResponse firstDefinition : firstQueryDefininitions) {

				System.out.println("[getChaincodeSequence()] chaincode name : " + chaincodeName);
				System.out.println("[getChaincodeSequence()] sequence : " + firstDefinition.getSequence());

				// 조회된 시퀀스 증가
				sequence = firstDefinition.getSequence() + 1L;

				System.out.println("[getChaincodeSequence()] next sequence : " + sequence);
			}

			System.out.println("[getChaincodeSequence()] Finish Get Chaincode Sequence");
			System.out.println("");
			System.out.println("");

		} catch (ProposalException e) {

			if (e.getMessage()
				.contains("namespace " + chaincodeName + " is not defined")) {

				System.out.println("[getChaincodeSequence()] sequence : x");
				System.out.println("[getChaincodeSequence()] next sequence : " + sequence);
				System.out.println("[getChaincodeSequence()] Finish Get Chaincode Sequence");
				System.out.println("");
				System.out.println("");

				return sequence;
			}
			throw new Exception(e.getMessage());
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
	 * @throws Exception
	 */

	private void approveChaincodeWithLifecycle(HFClient client, Channel channel, String chaincodeName, String packageID, String chaincodeVersion, long sequence) throws Exception {

		System.out.println("[approveChaincodeWithLifecycle()] Start Approve Chaincode With LifeCycle In " + client.getUserContext()
			.getName());

		// 체인코드 PDC 설정파일 설정
//		ChaincodeCollectionConfiguration                   collectionConfig                                   = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));

		// 체인코드 승인 리퀘스트 생성
		LifecycleApproveChaincodeDefinitionForMyOrgRequest lifecycleApproveChaincodeDefinitionForMyOrgRequest = client.newLifecycleApproveChaincodeDefinitionForMyOrgRequest();

		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setSequence(sequence);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeName(chaincodeName);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeVersion(chaincodeVersion);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setInitRequired(false);
		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setPackageId(packageID);
//		lifecycleApproveChaincodeDefinitionForMyOrgRequest.setChaincodeCollectionConfiguration(collectionConfig);

		// 생성한 리퀘스트 전송
		Collection<LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse> lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse = channel.sendLifecycleApproveChaincodeDefinitionForMyOrgProposal(lifecycleApproveChaincodeDefinitionForMyOrgRequest, channel.getPeers());

		for (LifecycleApproveChaincodeDefinitionForMyOrgProposalResponse response : lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse) {

			System.out.println("[approveChaincodeWithLifecycle()] message : " + response.getMessage());
			System.out.println("[approveChaincodeWithLifecycle()] status : " + response.getStatus());
		}

		// 체인코드 승인 결과 오더러로 전송
		channel.sendTransaction(lifecycleApproveChaincodeDefinitionForMyOrgProposalResponse);

		System.out.println("[approveChaincodeWithLifecycle()] Finish Approve Chaincode With LifeCycle");
		System.out.println("");
		System.out.println("");

	}

	/**
	 * 체인코드 승인 확인 함수
	 * 
	 * @param client           HFClient 클라이언트
	 * @param channel          패브릭 채널
	 * @param chaincodeName    체인코드 이름
	 * @param packageID        체인코드 패키지 아이디
	 * @param chaincodeVersion 체인코드 버전
	 * @param sequence         체인코드 시퀀스
	 * 
	 * @throws Exception
	 */

	public void verifyChaincodeApproved(HFClient client, Channel channel, String chaincodeName, String packageID, String chaincodeVersion, long sequence) throws Exception {

		System.out.println("[verifyChaincodeApproved()] Start Verify Chaincode Approved");

		// 체인코드 PDC 설정파일 설정
//		ChaincodeCollectionConfiguration     collectionConfig                     = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));

		// 체인코드 승인 확인 리퀘스트 생성
		LifecycleCheckCommitReadinessRequest lifecycleCheckCommitReadinessRequest = client.newLifecycleSimulateCommitChaincodeDefinitionRequest();

		lifecycleCheckCommitReadinessRequest.setSequence(sequence);
		lifecycleCheckCommitReadinessRequest.setChaincodeName(chaincodeName);
		lifecycleCheckCommitReadinessRequest.setChaincodeVersion(chaincodeVersion);
		lifecycleCheckCommitReadinessRequest.setInitRequired(false);
//		lifecycleCheckCommitReadinessRequest.setChaincodeCollectionConfiguration(collectionConfig);

		// 생성한 리퀘스트 전송
		Collection<LifecycleCheckCommitReadinessProposalResponse> lifecycleSimulateCommitChaincodeDefinitionProposalResponse = channel.sendLifecycleCheckCommitReadinessRequest(lifecycleCheckCommitReadinessRequest, channel.getPeers());

		for (LifecycleCheckCommitReadinessProposalResponse response : lifecycleSimulateCommitChaincodeDefinitionProposalResponse) {

			// 승인 완료한 조직리스트 확인
			System.out.println("[verifyChaincodeApproved()] 승인한 조직 배열 : " + response.getApprovedOrgs());
		}

		System.out.println("[verifyChaincodeApproved()] Finish Verify Chaincode Approved");
		System.out.println("");
		System.out.println("");
	}

	/**
	 * 체인코드 커밋 함수
	 * 
	 * @param client           HFClient 클라이언트
	 * @param channel          패브릭 채널
	 * @param chaincodeName    체인코드 이름
	 * @param packageID        체인코드 패키지 아이디
	 * @param chaincodeVersion 체인코드 버전
	 * @param sequence         체인코드 시퀀스
	 * @return 
	 * @return 
	 * 
	 * @throws Exception
	 */

	private Collection<LifecycleCommitChaincodeDefinitionProposalResponse> commitChaincodeWithLifecycle(HFClient client, Channel channel, String chaincodeName, String packageID, String chaincodeVersion, long sequence) throws Exception {

		System.out.println("[commitChaincodeWithLifecycle()] Start Commit Chaincode With LifeCycle");

		// 체인코드 PDC 설정파일 설정
//		ChaincodeCollectionConfiguration          collectionConfig                          = ChaincodeCollectionConfiguration.fromJsonFile(new File(System.getProperty("user.dir") + "/chaincode/collections_config.json"));

		// 체인코드 커밋 리퀘스트 생성
		LifecycleCommitChaincodeDefinitionRequest lifecycleCommitChaincodeDefinitionRequest = client.newLifecycleCommitChaincodeDefinitionRequest();

		lifecycleCommitChaincodeDefinitionRequest.setSequence(sequence);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeName(chaincodeName);
		lifecycleCommitChaincodeDefinitionRequest.setChaincodeVersion(chaincodeVersion);
		lifecycleCommitChaincodeDefinitionRequest.setInitRequired(false);
//		lifecycleCommitChaincodeDefinitionRequest.setChaincodeCollectionConfiguration(collectionConfig);

		// 생성한 리퀘스트 전송
//		Collection<LifecycleCommitChaincodeDefinitionProposalResponse> lifecycleCommitChaincodeDefinitionProposalResponses = new ArrayList<LifecycleCommitChaincodeDefinitionProposalResponse>();
		
		return  channel.sendLifecycleCommitChaincodeDefinitionProposal(lifecycleCommitChaincodeDefinitionRequest, channel.getPeers());



	}

//  api통신용 현재는 사용안함
//	
//	
//	public JSONObject getChannelConfig(FabricMemberDto memberDto, String channelName) throws CryptoException,
//			InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException,
//			NoSuchMethodException, InvocationTargetException, IOException, TransactionException, ParseException {
//
//		Util util = new Util();
//		JSONObject configJson = new JSONObject();
//		JSONParser jsonParser = new JSONParser();
//
//		// test
//		HFClient client = createClient(memberDto);
//
//		// 채널 생성요청할 오더러 점보 생성
//		Properties props = new Properties();
//		props.put("pemFile", "crypto-config/ca-certs/ca.org" + memberDto.getOrgName() + ".com-cert.pem");
//		props.put("hostnameOverride", memberDto.getConName());
//
//		Orderer orderer = client.newOrderer(memberDto.getConName(), memberDto.getConUrl(), props);
//		Peer peer = client.newPeer(memberDto.getConName(), memberDto.getConUrl(), props);
//
//		Channel channel = client.newChannel(channelName);
//
//		channel.addOrderer(orderer);
//		byte[] configBlock;
//
//		if (memberDto.getOrgType().equals("peer")) {
//
//			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), peer);
//
//		} else {
//
//			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), orderer);
//
//		}
//
//		logger.info(configBlock);
//
//		logger.info(configBlock.toString());
//		
//		//가져온 정보를 디코
//		configJson = (JSONObject) jsonParser.parse(util.configtxRequest(
//				"http://192.168.65.169:7059/protolator/decode/common.Config", channelName, configBlock, configBlock).toString());
//
//		return configJson;
//
//	}
//
//	/**
//	 * 채널 설정 변경 함수(테스트중)
//	 * 
//	 * @param memberDto      채널에 접근할 정보
//	 * @param channelName    채널명
//	 * @param config         기존 설정
//	 * @param modifiedConfig 변경된 설정
//	 * 
//	 * @throws ParseException
//	 * 
//	 */
//
//	public void setChannelConfig(FabricMemberDto memberDto, String channelName, byte[] config, byte[] modifiedConfig)
//			throws ParseException {
//
//		Util util = new Util();
//		JSONObject updateJson = new JSONObject();
//		JSONParser jsonParser = new JSONParser();
//
//		byte[] configPb = null;
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			ObjectOutputStream oos = new ObjectOutputStream(bos);
//			oos.writeObject(util.computeRequest("http://192.168.65.169:7059/protolator/encode/common.Config",
//					channelName, config, modifiedConfig));
//			oos.flush();
//			oos.close();
//			bos.close();
//			configPb = bos.toByteArray();
//		} catch (IOException ex) {
//			// TODO: Handle the exception
//		}
//
////		logger.info(util.configtxRequest("http://192.168.65.169:7059/protolator/encode/common.Config",channelName, config,modifiedConfig).);
////		byte[] configPb=util.configtxRequest("http://192.168.65.169:7059/protolator/encode/common.Config",channelName, config,modifiedConfig).toString().getBytes();
//
//		byte[] modifiedConfigPb = util.configtxRequest("http://192.168.65.169:7059/protolator/encode/common.Config",
//				channelName, modifiedConfig, config).toString().getBytes();
//
//		try {
//
//			File lOutFile = new File(System.getProperty("user.dir") + "/config.pb");
//			FileOutputStream lFileOutputStream = new FileOutputStream(lOutFile);
//			lFileOutputStream.write(configPb);
//			lFileOutputStream.close();
//
//		} catch (Throwable e) {
//
//			e.printStackTrace(System.out);
//
//		}
//
//		try {
//
//			File lOutFile = new File(System.getProperty("user.dir") + "/modifiedConfig.pb");
//
//			FileOutputStream lFileOutputStream = new FileOutputStream(lOutFile);
//
//			lFileOutputStream.write(modifiedConfigPb);
//
//			lFileOutputStream.close();
//
//		} catch (Throwable e) {
//
//			e.printStackTrace(System.out);
//
//		}
//
//		String configUpdate = util
//				.computeRequest("http://192.168.65.169:7059/configtxlator/compute/update-from-configs", channelName,
//						configPb, modifiedConfigPb)
//				.toString();
//		updateJson = (JSONObject) jsonParser
//				.parse(util.configtxRequest("http://192.168.65.169:7059/protolator/decode/common.Config", channelName,
//						configUpdate.getBytes(), config).toString());
//
//		logger.info("zzzzzz");
//		logger.info("zzzzzz");
//		logger.info("zzzzzz");
//		logger.info(updateJson);
//		logger.info("zzzzzz");
//		logger.info("zzzzzz");
//
//	}

};
