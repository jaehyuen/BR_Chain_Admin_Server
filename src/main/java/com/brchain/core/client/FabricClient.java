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
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallet.Identity;
import org.hyperledger.fabric.protos.common.Common.Block;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.UpdateChannelConfiguration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.service.ConInfoService;
import com.brchain.core.util.BrchainUser;
import com.brchain.core.util.Util;

@Component
public class FabricClient {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	Environment environment;

	@Autowired
	SshClient sshClient;

	@Autowired
	ConInfoService conInfoService;

	@Value("${brchain.sourcedir}")
	String sourceDir;

	@Value("${brchain.logdir}")
	String logDir;

	@Value("${brchain.datadir}")
	String dataDir;

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

	public Network connect(String channelName, String orgName, JSONObject connectionJson) throws Exception {

		ArrayList<FabricMemberDto> ordererArrDto = new ArrayList<FabricMemberDto>();
		ArrayList<FabricMemberDto> peerArrDto = new ArrayList<FabricMemberDto>();

		for (int i = 0; i < 3; i++) {
			FabricMemberDto ordererDto = new FabricMemberDto();
			int testip = i + 7;
			ordererDto.setOrgName("orderer");
			ordererDto.setConPort("7050");
			ordererDto.setConName("orderer" + i + ".orgorderer.com");
			ordererDto.setConNum(i);
			ordererDto.setConUrl("grpcs://192.168.65.16" + testip + ":7050");
			ordererDto.setOrgMspId("ordererMSP");
			ordererDto.setOrgType("orderer");
			ordererArrDto.add(ordererDto);

		}
		for (int i = 0; i < 3; i++) {
			FabricMemberDto peerDto = new FabricMemberDto();
			int testip = i + 7;
			peerDto.setOrgName("nonghyupit");
			peerDto.setConPort("7051");
			peerDto.setConName("peer" + i + ".orgnonghyupit.com");
			peerDto.setConNum(i);
			peerDto.setConUrl("grpcs://192.168.65.16" + testip + ":7051");
			peerDto.setOrgMspId("nonghyupitMSP");
			peerDto.setOrgType("peer");
			peerDto.setCaUrl("http://192.168.65.167:7054");
			peerArrDto.add(peerDto);

		}

		logger.info("[ordererArrVo]" + ordererArrDto);
		logger.info("[peerArrVo]" + peerArrDto);
//		InputStream is = new ByteArrayInputStream(
//				createFabrcSetting("cert-channel", ordererArrDto, peerArrDto).toString().replace("\\", "").getBytes());
		InputStream is = new ByteArrayInputStream(connectionJson.toString().replace("\\", "").getBytes());
		// 파라미터 설정
//		String channelName = (String) fabricJson.get("channels");

		Path walletPath = Paths.get("wallet");
//		Path networkConfigPath = HelperUtil.resourcesUrlPath("application.yml");

		Wallet wallet = Wallet.createFileSystemWallet(walletPath);
		Gateway.Builder builder = Gateway.createBuilder();
		builder.identity(wallet, orgName).networkConfig(is).discovery(false);
		Gateway gateway = builder.connect();

		Network network = gateway.getNetwork(channelName);

		logger.info("[FabricHelper] Connection Success!");

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

		String certPemFile = "crypto-config/ca-certs/ca.org" + memberDto.getOrgName() + "brord.com-cert.pem";
		PrivateKey key = null;
		String certificate = null;
		InputStream isKey = null;
		BufferedReader brKey = null;
		String path = null;

		Properties props = new Properties();
		props.put("pemFile", certPemFile);

		// ca client생성
		HFCAClient caClient = HFCAClient.createNewInstance(memberDto.getCaUrl(), props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);

		Wallet wallet = Wallet.createFileSystemWallet(Paths.get("wallet"));

		// wallet경로에 인증서 체크
		boolean walletExists = wallet.exists(memberDto.getOrgName());

		if (walletExists) {
			return;
		}

		if (memberDto.getOrgType().equals("peer")) {

			path = "crypto-config/peerOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org"
					+ memberDto.getOrgName() + ".com/msp/keystore/server.key";
			certificate = new String(
					Files.readAllBytes(Paths.get("crypto-config/peerOrganizations/org" + memberDto.getOrgName()
							+ ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/signcerts/cert.pem")));
		} else {

			path = "crypto-config/ordererOrganizations/org" + memberDto.getOrgName() + ".com/users/Admin@org"
					+ memberDto.getOrgName() + ".com/msp/keystore/server.key";
			certificate = new String(
					Files.readAllBytes(Paths.get("crypto-config/ordererOrganizations/org" + memberDto.getOrgName()
							+ ".com/users/Admin@org" + memberDto.getOrgName() + ".com/msp/signcerts/cert.pem")));
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

			byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("EC");
			key = kf.generatePrivate(keySpec);

		} finally {

			isKey.close();
			brKey.close();

		}

		X509Enrollment enrollment = new X509Enrollment(key, certificate);
		// 인증서를 wallet형식으로 변환
		Identity user = Identity.createIdentity(memberDto.getOrgMspId(), enrollment.getCert(), enrollment.getKey());
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

	public HFClient createChannel(ArrayList<FabricMemberDto> peerDtoArr, FabricMemberDto ordererDto, String channelName)
			throws Exception {
		Util util = new Util();

		for (FabricMemberDto peerDto : peerDtoArr) {

			if (!conInfoService.isMemOfConso(ordererDto.getOrgName(), peerDto.getOrgName())) {

				JSONObject genesisJson = getChannelConfig(ordererDto, "testchainid");
				JSONObject testJson = util.createOrgJson(peerDto);

				logger.info(genesisJson.toString());
				logger.info(testJson.toString());

				JSONObject modifiedJson = util.test(genesisJson, testJson, "", peerDto.getOrgName());

				File updateFile = createUpdateFile(ordererDto, "testchainid", genesisJson, modifiedJson);

				setUpdateTest(ordererDto, "testchainid", updateFile);

				conInfoService.updateConsoOrgs(ordererDto.getOrgName(), peerDto.getOrgName());

			}
		}

		FabricMemberDto peerDto = peerDtoArr.get((int) (Math.random() * peerDtoArr.size()));
		// 가저온 인증서 정보로 user 생성
		BrchainUser userContext = createContext(peerDto);

		// 클라이언트 생성
		HFClient client = createClient(peerDto);

		logger.info("[채널생성] 클라이언트 생성완료");

		// 채널 생성요청할 오더러 점보 생성
		Properties props = new Properties();
		props.put("pemFile", "crypto-config/ca-certs/ca.org" + ordererDto.getOrgName() + ".com-cert.pem");
		props.put("hostnameOverride", ordererDto.getConName());

		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), props);

		logger.info("[채널생성] 오더러설정 완료");

		// 채널 생성 작업
		Channel newChannel = client.newChannel(channelName, orderer,
				new ChannelConfiguration(new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")),
				client.getChannelConfigurationSignature(
						new ChannelConfiguration(
								new File("channel-artifacts/" + channelName + "/" + channelName + ".tx")),
						userContext));

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
	 */

	public void joinChannel(HFClient client, FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName)
			throws InvalidArgumentException, ProposalException {

		// 오더러 정보 생성
		Properties ordererProps = createFabricProperties(ordererDto);
		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties peerProps = createFabricProperties(peerDto);
		Peer peer = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

		Channel channel = client.newChannel(channelName);
		channel.addOrderer(orderer);
		channel.joinPeer(peer);
		
		logger.info("[채널가입] : " + peerDto.getConName() + " 컨테이너 " + channelName + " 채널 가입완료");

	}

	/**
	 * UserContext 생성 함수
	 * 
	 * @param memberDto 생성할 UserContext 정
	 * 
	 * @return BrchainUser
	 * 
	 * @throws IOException
	 */

	public BrchainUser createContext(FabricMemberDto memberDto) throws IOException {
		// wallet에서 피어의 인증서 정보를 가져옴
		Wallet wallet = Wallet.createFileSystemWallet(Paths.get("wallet"));
		X509Enrollment enrollment = new X509Enrollment(wallet.get(memberDto.getOrgName()).getPrivateKey(),
				wallet.get(memberDto.getOrgName()).getCertificate());

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

	public HFClient createClient(FabricMemberDto memberDto)
			throws IOException, CryptoException, InvalidArgumentException, ClassNotFoundException,
			IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		
		// 클라이언트 생성
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(cryptoSuite);
		
		//UserConText 생성
		BrchainUser userContext = createContext(memberDto);
		client.setUserContext(userContext);

		return client;

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

		HFClient client = createClient(memberDto);
		

		// 채널 생성요청할 맴버 정보 생성
		Properties props = createFabricProperties(memberDto);


		Orderer orderer = client.newOrderer(memberDto.getConName(), memberDto.getConUrl(), props);
		Peer peer = client.newPeer(memberDto.getConName(), memberDto.getConUrl(), props);

		Channel channel = client.newChannel(channelName);

		byte[] configBlock;

		//설정 파일 가져오기(Byte Array)
		if (memberDto.getOrgType().equals("peer")) {
			channel.addPeer(peer);
			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), peer);

		} else {
			channel.addOrderer(orderer);
			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), orderer);

		}

		
		//설정 파일 가져오기(Byte Array) to File
		String path = "channel-artifacts/" + memberDto.getOrgName() + "/";
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

		
		//로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.uploadFile(path, fileName + ".pb");
		}

		Thread.sleep(1000);

		// json 변경
		String command = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName
				+ ".pb --type common.Config > " + sourceDir + "/" + path + fileName + ".json";
		sshClient.execCommand(command);

		Thread.sleep(1000);
		// 변경된 파일 다운로드
		sshClient.downloadFile(path, fileName + ".json");

		Thread.sleep(1000);
		return (JSONObject) jsonParser
				.parse(new FileReader(System.getProperty("user.dir") + "/" + path + fileName + ".json"));

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
	public File createUpdateFile(FabricMemberDto memberDto, String channelName, JSONObject config,
			JSONObject modifiedConfig) throws Exception {

		Util util = new Util();
		JSONParser jsonParser = new JSONParser();

		String path = "channel-artifacts/" + memberDto.getOrgName() + "/";
		String fileName = channelName + "_modified_config";

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

		
		//로컬 환경에서는 파일이 서버랑 왔다 갓다해야됨
		if (environment.getActiveProfiles()[0].equals("local")) {
			sshClient.uploadFile(path, fileName + ".json");
		}

		String command = "configtxlator proto_encode --input " + sourceDir + "/" + path + fileName + ".json"
				+ " --type common.Config --output " + sourceDir + "/" + path + fileName + ".pb";
		sshClient.execCommand(command);

		command = "configtxlator compute_update --channel_id " + channelName + " --original " + sourceDir + "/" + path
				+ channelName + "_config.pb --updated " + sourceDir + "/" + path + fileName + ".pb --output "
				+ sourceDir + "/" + path + channelName + "_config_update.pb";
		sshClient.execCommand(command);

		fileName = channelName + "_config_update";
		command = "configtxlator proto_decode --input " + sourceDir + "/" + path + fileName
				+ ".pb --type common.ConfigUpdate > " + sourceDir + "/" + path + fileName + ".json";
		sshClient.execCommand(command);

		Thread.sleep(1000);

		// 변경된 파일 다운로드
		sshClient.downloadFile(path, fileName + ".json");
		sshClient.downloadFile(path, fileName + ".pb");

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

	public void setUpdateTest(FabricMemberDto memberDto, String channelName, File updateFile)
			throws InvalidArgumentException, CryptoException, ClassNotFoundException, IllegalAccessException,
			InstantiationException, NoSuchMethodException, InvocationTargetException, IOException,
			TransactionException {

		HFClient client = createClient(memberDto);

		// 채널 설정 변경 요청할 정보 생성
		Properties props = createFabricProperties(memberDto);


		Orderer orderer = client.newOrderer(memberDto.getConName(), memberDto.getConUrl(), props);
		Peer peer = client.newPeer(memberDto.getConName(), memberDto.getConUrl(), props);

		Channel channel = client.newChannel(channelName).initialize();

		byte[] configBlock;

		if (memberDto.getOrgType().equals("peer")) {
			channel.addPeer(peer);
			configBlock = channel.getChannelConfigurationBytes(createContext(memberDto), peer);

		} else {
			channel.addOrderer(orderer);

			UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateFile);
			BrchainUser user = createContext(memberDto);
			byte[] signers = channel.getUpdateChannelConfigurationSignature(updateChannelConfiguration, user);
			channel.updateChannelConfiguration(user, updateChannelConfiguration, orderer, signers);

		}

	}

	/**
	 * 체인코드 설치 함수
	 * 
	 * @param peerDto   설치할 피어 정보 DTO
	 * @param ccName    체인코드 이름
	 * @param ccVersion 체인코드 버전
	 * @throws Exception
	 */

	public void installChaincodeToPeer(FabricMemberDto peerDto, String ccName, String ccVersion) throws Exception {

		//클라이언트 생성
		HFClient client = createClient(peerDto);

		//체인코드 ID 생성
		ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(ccName).setVersion(ccVersion)
				.setPath(ccName + "/");

		ChaincodeID chaincodeID = chaincodeIDBuilder.build();
		
		//체인코드 설치 request 생성
		InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();

		installProposalRequest.setChaincodeID(chaincodeID);
		installProposalRequest.setUserContext(client.getUserContext());
		installProposalRequest.setChaincodeSourceLocation(new File(System.getProperty("user.dir") + "/chaincode"));
		installProposalRequest.setChaincodeVersion(ccVersion);

		//설치할 피어 정보 생성
		List<Peer> peers = new ArrayList<Peer>();
		Peer peer = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), createFabricProperties(peerDto));
		peers.add(peer);
		
		logger.info("[체인코드 설치] 트렌젝션 생성 및 전송");
		Collection<ProposalResponse> responses = client.sendInstallProposal(installProposalRequest, peers);

		for (ProposalResponse response : responses) {

			if (response.getStatus().name().equals("FAILURE")) {
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
	 * @param peerDto 인스턴스화를 진행할 피어 정보 DTO
	 * @param ordererDto 인스턴스화를 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * @param ccName 체인코드 이름
	 * @param ccVersion 체인코드 버전 
	 * @param ccLang 체인코드 언어
	 * 
	 * @throws Exception
	 */
	
	public void instantiateChaincode(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName,
			String ccName, String ccVersion, String ccLang) throws Exception {

		// 클라이언트 생성
		HFClient client = createClient(peerDto);

		// 오더러 정보 생성
		Properties ordererProps = createFabricProperties(ordererDto);
		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties peerProps = createFabricProperties(peerDto);
		Peer peer = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

		// 채널 정보 생성
		Channel channel = client.newChannel(channelName);
		channel.addPeer(peer);
		channel.addOrderer(orderer);
		channel.initialize();
		ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(ccName).setVersion(ccVersion)
				.setPath(ccName + "/");

		ChaincodeID chaincodeID = chaincodeIDBuilder.build();

		Map<String, byte[]> tm = new HashMap<>();
		tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
		tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
		
		// 체인코드 인스턴스화 리퀘스트 생성
		InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
		instantiateProposalRequest.setChaincodeID(chaincodeID);
		instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
		instantiateProposalRequest.setFcn("");
		instantiateProposalRequest.setArgs("");
		instantiateProposalRequest.setProposalWaitTime(300000);		
		instantiateProposalRequest.setTransientMap(tm);

		// 리퀘스트 결과 오더러로 전송
		Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest);
		for (ProposalResponse response : responses) {
			System.out.println(response.getMessage());
			if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
				channel.sendTransaction(responses, channel.getOrderers()).thenApply(transactionEvent -> {
					System.out.println(transactionEvent.isValid());
					System.out.println(transactionEvent.getTransactionID());

					return null;
				});
			} else {
				throw new Exception(response.getMessage());
			}

		}

	}

	
	/**
	 * 채널 블록 이벤트 등록 함수
	 * @param peerDto 이벤트 등록을 진행할 피어 정보 DTO
	 * @param ordererDto 이벤트 등록을 진행할 오더러 정보 DTO
	 * @param channelName 채널 이름
	 * 
	 * @return 이벤트 핸들러
	 * 
	 * @throws Exception
	 */
	
	public String registerEventListener(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName)
			throws Exception {

		System.out.println("eventTest 1");
		// 클라이언트 생성
		HFClient client = createClient(peerDto);

		// 오더러 정보 생성
		Properties ordererProps = createFabricProperties(ordererDto);
		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties peerProps = createFabricProperties(peerDto);
		Peer peer = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

		// 채널 정보 생성
		Channel channel = client.newChannel(channelName);
		channel.addPeer(peer);
		channel.addOrderer(orderer);
		channel.initialize();
		System.out.println("eventTest 2");
		BlockListener blockListener = new BlockListener() {

			@Override
			public void received(BlockEvent blockEvent) {
				Block block = blockEvent.getBlock();

				System.out.println("BLock All FIelds :" + block.getAllFields());
				System.out.println("BLock Number :" + blockEvent.getBlockNumber());

			}

		};

		return channel.registerBlockListener(blockListener);

	}

	public void eventTest2(FabricMemberDto peerDto, FabricMemberDto ordererDto, String channelName, String ccName,
			String ccVersion, String ccLang, String a) throws Exception {

		System.out.println("eventTest 1");
		// 클라이언트 생성
		HFClient client = createClient(peerDto);

		// 오더러 정보 생성
		Properties ordererProps = createFabricProperties(ordererDto);
		Orderer orderer = client.newOrderer(ordererDto.getConName(), ordererDto.getConUrl(), ordererProps);

		// 피어 정보 생성
		Properties peerProps = createFabricProperties(peerDto);
		Peer peer = client.newPeer(peerDto.getConName(), peerDto.getConUrl(), peerProps);

		// 채널 정보 생성
		Channel channel = client.newChannel(channelName);
		channel.addPeer(peer);
		channel.addOrderer(orderer);
		channel.initialize();
		System.out.println("eventTest 2");
		BlockListener bl = new BlockListener() {

			@Override
			public void received(BlockEvent blockEvent) {
				Block block = blockEvent.getBlock();

				System.out.println("BLock All FIelds :" + block.getAllFields());
				System.out.println("BLock Number :" + blockEvent.getBlockNumber());

				System.out.println("THis is buyer Listener..");

			}

		};
		System.out.println("eventTest 3");
		channel.unregisterBlockListener(a);
//String test	=	channel.registerBlockListener(bl);
//System.out.println("eventTest 4"+test);
//		channel

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

}
