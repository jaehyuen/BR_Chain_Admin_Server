package com.brchain.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.dto.BlockDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.PolicyDto;
import com.brchain.core.dto.TransactionDto;
import com.brchain.core.dto.chaincode.CcInfoChannelDto;
import com.brchain.core.dto.chaincode.CcInfoDto;
import com.brchain.core.dto.chaincode.CcInfoPeerDto;
import com.brchain.core.dto.channel.ChannelHandleDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.dto.channel.ChannelInfoPeerDto;
import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.TransactionEntity;
import com.brchain.core.entity.chaincode.CcInfoChannelEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity;
import com.brchain.core.entity.channel.ChannelHandleEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.PortBinding;

@Component
public class Util {

	/**
	 * connection.json 생성 함수
	 * 
	 * @param channelName   채널명
	 * @param ordererArrDto Json 생성시 필요한 오더러 리스트 관련 DTO
	 * @param peerArrDto    Json 생성시 필요한 피 리스트 관련 DTO
	 * 
	 * @return JSONObject fabricJson connection.yaml
	 */

	@SuppressWarnings({ "unchecked" })
	public JSONObject createFabrcSetting(String channelName, ArrayList<FabricMemberDto> ordererArrDto,
			ArrayList<FabricMemberDto> peerArrDto) {

		JSONObject fabricJson = new JSONObject();

		JSONObject clientJson = new JSONObject();

		JSONObject orgJson1 = new JSONObject();
		JSONObject orgJson2 = new JSONObject();

		JSONObject channelJson1 = new JSONObject();
		JSONObject channelJson2 = new JSONObject();

		JSONObject peerJson = new JSONObject();
		JSONObject peerMemberJson = new JSONObject();
		JSONObject ordererMemberJson = new JSONObject();

		JSONObject caJson1 = new JSONObject();
		JSONObject caJson2 = new JSONObject();

		ArrayList<String> ordererArr = new ArrayList<String>();
		ArrayList<String> peerArr = new ArrayList<String>();
		ArrayList<String> caArr = new ArrayList<String>();

		caArr.add("ca.org" + peerArrDto.get(0).getOrgName() + ".com");

		clientJson.put("organization", peerArrDto.get(0).getOrgName());

		// 오더러 관련 변수 생성
		for (FabricMemberDto dto : ordererArrDto) {
			ordererArr.add(dto.getConName());
			ordererMemberJson.put(dto.getConName(),
					createMemberJson(dto.getOrgName(), dto.getConName(), dto.getConUrl()));

		}

		// 피어 관련 변수 생성
		for (FabricMemberDto dto : peerArrDto) {
			peerJson.put(dto.getConName(), new JSONObject());
			peerArr.add(dto.getConName());
			peerMemberJson.put(dto.getConName(), createMemberJson(dto.getOrgName(), dto.getConName(), dto.getConUrl()));

		}

		channelJson2.put("peers", peerJson);
		channelJson2.put("orderers", ordererArr);
		channelJson1.put(channelName, channelJson2);

		orgJson2.put("mspid", peerArrDto.get(0).getOrgMspId());
		orgJson2.put("certificateAuthorities", caArr);
		orgJson2.put("peers", peerArr);
		orgJson1.put(peerArrDto.get(0).getOrgName(), orgJson2);

		caJson2.put("caName", caArr.get(0));
		caJson2.put("url", peerArrDto.get(0).getCaUrl());
		caJson1.put("ca.org" + peerArrDto.get(0).getOrgName() + ".com", caJson2);

		fabricJson.put("name", channelName);
		fabricJson.put("version", "1.0.0");
		fabricJson.put("client", clientJson);
		fabricJson.put("channels", channelJson1);
		fabricJson.put("organizations", orgJson1);
		fabricJson.put("orderers", ordererMemberJson);
		fabricJson.put("peers", peerMemberJson);
		fabricJson.put("certificateAuthorities", caJson1);

		System.out.println("[fabric 설정 json 생성]" + fabricJson.toString().replace("\\", ""));

		return fabricJson;
	}

	/**
	 * peer, orderer 관련 Json 생성 함수
	 * 
	 * @param orgName  조직명
	 * @param hostName 호스트명
	 * @param url      Url
	 * 
	 * @return peer, orderer 관련 Json
	 */

	@SuppressWarnings("unchecked")
	public JSONObject createMemberJson(String orgName, String hostName, String url) {

		JSONObject memberJson = new JSONObject();

		JSONObject grpcOptionsJson = new JSONObject();
		JSONObject tlsCACertsJson = new JSONObject();

		String certPath = "crypto-config/ca-certs/ca.org" + orgName + ".com-cert.pem";

		grpcOptionsJson.put("hostnameOverride", hostName);
		grpcOptionsJson.put("ssl-target-name-override", hostName);

		tlsCACertsJson.put("path", certPath);

		memberJson.put("grpcOptions", grpcOptionsJson);
		memberJson.put("tlsCACerts", tlsCACertsJson);
		memberJson.put("url", url);
		memberJson.put("name", hostName);

		return memberJson;
	}

	/**
	 * 설정파일(컨소시움) 수정 함수 (테스트중)
	 * 
	 * @param json       수정할 Json
	 * @param addjson    추가할 Json
	 * @param parentsKey 부모키??
	 * 
	 * @return 추가된 Json
	 */

	public JSONObject modifyConsoConfig(JSONObject json, JSONObject addjson, String parentsKey, String peerOrg) {

		String key = "";
		JSONObject resultJson = new JSONObject();

		Iterator iter = json.keySet().iterator();
		while (iter.hasNext()) {
			key = (String) iter.next();

			if (key.equals("groups") && parentsKey.equals("SampleConsortium")) {
				System.out.println("현재키 :" + key + " 부모 키 :" + parentsKey);
				System.out.println("찾았다!!!!11");
				System.out.println("찾았다!!!!22");
				System.out.println("찾았다!!!!33");
				resultJson = (JSONObject) json.get(key);

				resultJson.put(peerOrg, addjson);
				return resultJson;

			} else if (json.get(key) instanceof JSONObject) {

				resultJson = modifyConsoConfig((JSONObject) json.get(key), addjson, key, peerOrg);
			}

		}

		return json;
	}

	/**
	 * 조직 정보 json 생성 함수
	 * 
	 * @param memberDto 조직정보 Json
	 * 
	 * @return 조직 정보 Json
	 */

	@SuppressWarnings("unchecked")
	public JSONObject createOrgJson(FabricMemberDto memberDto) {

		JSONObject orgJson = new JSONObject();

		JSONObject policiesJson = new JSONObject();
		JSONObject polJson = new JSONObject();

		JSONObject cryptoConfigJson = new JSONObject();
		JSONObject configJson = new JSONObject();
		JSONObject valueJson = new JSONObject();
		JSONObject mspJson = new JSONObject();
		JSONObject valuesJson = new JSONObject();

		PolicyDto policyDto = new PolicyDto();

		ArrayList<String> orgMspArr = new ArrayList<String>();
		ArrayList<String> adminsArr = new ArrayList<String>();
		ArrayList<String> rootCertsArr = new ArrayList<String>();
		ArrayList<String> tlsRootCertsArr = new ArrayList<String>();

		adminsArr.add(fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/" + memberDto.getOrgType()
				+ "Organizations/org" + memberDto.getOrgName() + ".com/users/Admin@org" + memberDto.getOrgName()
				+ ".com/msp/signcerts/cert.pem"));
		rootCertsArr.add(fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/ca-certs/ca.org"
				+ memberDto.getOrgName() + ".com-cert.pem"));
		tlsRootCertsArr.add(fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/ca-certs/ca.org"
				+ memberDto.getOrgName() + ".com-cert.pem"));

		orgMspArr.add(memberDto.getOrgMspId());

		policyDto.setPolicyType(1);
		policyDto.setSubPolicy("Writers");
		policyDto.setRule("or");
		policyDto.setIdentityMsps(orgMspArr);

		policiesJson.put("Writers", createPolicyJson(policyDto));

		policyDto.setSubPolicy("Readers");
		policiesJson.put("Readers", createPolicyJson(policyDto));

		policyDto.setSubPolicy("Admins");
		policiesJson.put("Admins", createPolicyJson(policyDto));

		cryptoConfigJson.put("signature_hash_family", "SHA2");
		cryptoConfigJson.put("identity_identifier_hash_function", "SHA256");

		configJson.put("tls_root_certs", tlsRootCertsArr);
		configJson.put("tls_intermediate_certs", new ArrayList<String>());
		configJson.put("signing_identity", null);
		configJson.put("root_certs", rootCertsArr);
		configJson.put("revocation_list", new ArrayList<String>());
		configJson.put("organizational_unit_identifiers", new ArrayList<String>());
		configJson.put("name", memberDto.getOrgMspId());
		configJson.put("intermediate_certs", new ArrayList<String>());
		configJson.put("fabric_node_ous", null);
		configJson.put("crypto_config", cryptoConfigJson);
		configJson.put("admins", adminsArr);

		valueJson.put("config", configJson);
		valueJson.put("type", 0);

		mspJson.put("value", valueJson);
		mspJson.put("version", "0");
		mspJson.put("mod_policy", "Admins");

		valuesJson.put("MSP", mspJson);

		orgJson.put("version", "0");
		orgJson.put("mod_policy", "Admins");
		orgJson.put("policies", policiesJson);
		orgJson.put("groups", new JSONObject());
		orgJson.put("values", valuesJson);

		System.out.println("[org json 생성]" + orgJson.toString().replace("\\", ""));

		return orgJson;
	}

	/**
	 * 정책 json 생성 함수
	 * 
	 * @param policyDto 정책 정보 DTO
	 * 
	 * @return 정책 Json
	 */

	@SuppressWarnings("unchecked")
	public JSONObject createPolicyJson(PolicyDto policyDto) {

		JSONObject returnJson = new JSONObject();

		JSONObject policyJson = new JSONObject();

		JSONObject valueJson = new JSONObject();

		JSONArray identitiesJsonArr = new JSONArray();

		JSONObject nOutOfJson = new JSONObject();
		JSONObject roluJson = new JSONObject();

		JSONArray rolesJsonArr = new JSONArray();

		// policyType이 1이면 Signature 정책
		if (policyDto.getPolicyType() == 1) {

			int cnt = 0;
			for (String identityMsp : policyDto.getIdentityMsps()) {

				JSONObject identitiesJson = new JSONObject();
				JSONObject principalJson = new JSONObject();

				JSONObject rolesJson = new JSONObject();

				principalJson.put("role", (policyDto.getSubPolicy().equals("Admins")) ? "ADMIN" : "MEMBER");
				principalJson.put("msp_identifier", identityMsp);

				identitiesJson.put("principal", principalJson);
				identitiesJson.put("principal_classification", "ROLE");

				identitiesJsonArr.add(identitiesJson);

				rolesJson.put("signed_by", cnt);

				rolesJsonArr.add(rolesJson);

				cnt++;

			}

			nOutOfJson.put("n", (policyDto.getRule().equals("and")) ? cnt : 1);
			nOutOfJson.put("rules", rolesJsonArr);

			roluJson.put("n_out_of", nOutOfJson);

			valueJson.put("identities", identitiesJsonArr);
			valueJson.put("rule", roluJson);
			valueJson.put("version", 0);

		}

		// policyType이 3이면 ImplicitMeta 정책
		else {

			valueJson.put("rule", policyDto.getRule());
			valueJson.put("sub_policy", policyDto.getSubPolicy());

		}

		policyJson.put("value", valueJson);
		policyJson.put("type", policyDto.getPolicyType());

		returnJson.put("policy", policyJson);
		returnJson.put("version", "0");
		returnJson.put("mod_policy", "Admins");

		return returnJson;

	}

	@SuppressWarnings("unchecked")
	public JSONObject addUpdateHeader(String channelName, JSONObject dataJson) {

		JSONObject configUpdateJson = new JSONObject();

		JSONObject updateJson = new JSONObject();

		JSONObject payloadJson = new JSONObject();

		JSONObject channelHeaderJson = new JSONObject();
		JSONObject headerJson = new JSONObject();

		channelHeaderJson.put("type", 2);
		channelHeaderJson.put("channel_id", channelName);

		headerJson.put("channel_header", channelHeaderJson);

		payloadJson.put("data", configUpdateJson);
		payloadJson.put("header", headerJson);

		configUpdateJson.put("config_update", dataJson);

		updateJson.put("payload", payloadJson);

		return updateJson;
	}

	public String fileEncodeBases64(String filePath) {

		File file = new File(filePath);
		byte[] data = new byte[(int) file.length()];

		try (FileInputStream stream = new FileInputStream(file)) {
			stream.read(data, 0, data.length);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		String base64data = Base64.getEncoder().encodeToString(data);

		return base64data;

	}

	public ResultDto setResult(String code, boolean flag, String message, Object data) {

		ResultDto resultDto = new ResultDto();

		resultDto.setResultCode(code);
		resultDto.setResultFlag(flag);
		resultDto.setResultMessage(message);
		resultDto.setResultData(data);

		return resultDto;

	}

	public JSONObject modifyAnchorConfig(JSONObject json, JSONObject addjson, String parentsKey,
			FabricMemberDto peerDto) {

		String key = "";
		JSONObject resultJson = new JSONObject();

		Iterator iter = json.keySet().iterator();
		while (iter.hasNext()) {
			key = (String) iter.next();

			if (key.equals("values") && parentsKey.equals(peerDto.getOrgName())) {
				System.out.println("현재키 :" + key + " 부모 키 :" + parentsKey);

				System.out.println("찾았다!!!!11");
				System.out.println("찾았다!!!!22");
				System.out.println("찾았다!!!!33");
				resultJson = (JSONObject) json.get(key);

				if (resultJson.containsKey("AnchorPeers")) {
					JSONObject json1 = new JSONObject();
					JSONObject json2 = new JSONObject();
					JSONArray jsonArr = new JSONArray();

					json1 = (JSONObject) resultJson.get("AnchorPeers");
					json2 = (JSONObject) json1.get("value");
					jsonArr = (JSONArray) json2.get("anchor_peers");

					JSONObject anchorPeerJson = new JSONObject();
					anchorPeerJson.put("host", peerDto.getConName());
					anchorPeerJson.put("port", peerDto.getConPort());
					jsonArr.add(anchorPeerJson);

				} else {
					resultJson.put("AnchorPeers", addjson);
				}

				return resultJson;

			} else if (json.get(key) instanceof JSONObject) {

				resultJson = modifyAnchorConfig((JSONObject) json.get(key), addjson, key, peerDto);
			}

		}

		return json;
	}

	public JSONObject createAnchorJson(FabricMemberDto peerDto) {

		JSONObject anchorPeerJson = new JSONObject();
		JSONArray anchorPeerArr = new JSONArray();

		JSONObject resultJson = new JSONObject();
		JSONObject valueJson = new JSONObject();

		anchorPeerJson.put("host", peerDto.getConName());
		anchorPeerJson.put("port", peerDto.getConPort());

		anchorPeerArr.add(anchorPeerJson);

		valueJson.put("anchor_peers", anchorPeerArr);

		resultJson.put("version", "0");
		resultJson.put("value", valueJson);
		resultJson.put("mod_policy", "Admins");

		return resultJson;

	}

	public JSONObject createConJson(ContainerInfo info) {

		JSONObject logJson = new JSONObject();

		JSONObject conJson1 = new JSONObject();
		JSONObject conJson2 = new JSONObject();

		ImmutableList<String> cmdArr = info.config().cmd();
		String resultCmd = "";

		for (String cmd : cmdArr) {
			resultCmd = resultCmd + cmd + " ";

		}

		ImmutableMap<String, List<PortBinding>> portMap = info.hostConfig().portBindings();

		ArrayList<String> portList = new ArrayList<String>();

		for (Entry<String, List<PortBinding>> entry : portMap.entrySet()) {

			String port1 = entry.getKey();

			List<PortBinding> value = entry.getValue();
			PortBinding portBinding = value.get(0);

			String port2 = portBinding.hostPort();
			portList.add(port1 + ":" + port2);
		}
		if (!portMap.isEmpty()) {
			conJson2.put("ports", portList);
		}

		logJson.put("driver", "none");

		conJson2.put("container_name", info.name().replace("/", ""));
		conJson2.put("image", info.image());
		conJson2.put("environment", info.config().env());

		conJson2.put("command", resultCmd);

		conJson2.put("logging", logJson);
		conJson2.put("volumes", info.hostConfig().binds());

		conJson2.put("networks", new String[] { info.hostConfig().networkMode() });

//		conJson1.put(info.name(), conJson2);

		return conJson2;

	}

	public JSONObject createComposeJson(JSONObject conJson) {

		JSONObject ipamConfigJson = new JSONObject();

		JSONArray ipamConfigJsonArr = new JSONArray();

		JSONObject ipamJson = new JSONObject();

		JSONObject networksJson2 = new JSONObject();
		JSONObject networksJson1 = new JSONObject();
		JSONObject composeJson = new JSONObject();

		ipamConfigJson.put("subnet", "123.123.123.0/24");
		ipamConfigJson.put("gateway", "123.123.123.1");

		ipamConfigJsonArr.add(ipamConfigJson);

		ipamJson.put("config", ipamConfigJsonArr);
		ipamJson.put("driver", "default");

		networksJson2.put("ipam", ipamJson);
		networksJson2.put("name", "brchain-network");

		networksJson1.put("brchain-network", networksJson2);

		composeJson.put("services", conJson);
		composeJson.put("networks", networksJson1);
		composeJson.put("version", "2.1");
		return composeJson;

	}

	/**
	 * docker-compose yaml file 생성함수
	 * 
	 * @param fileName 파일 명
	 * @param conJson  컨테이너 JSON
	 * 
	 */
	public void createYamlFile(String fileName, JSONObject conJson) {
		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
		try {

			File file = new File(System.getProperty("user.dir") + "/compose-files/");

			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}

			objectMapper.writeValue(new File(System.getProperty("user.dir") + "/compose-files/" + fileName + ".yaml"),
					createComposeJson(conJson));
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean unZip(String zipPath, String zipFileName, String zipUnzipPath) {
		boolean isChk = false;
//		zipUnzipPath = zipUnzipPath; 
		File zipFile = new File(zipPath + zipFileName);
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry zipentry = null;
		try {
			if (makeFolder(zipUnzipPath)) {

			}
			fis = new FileInputStream(zipFile);
			zis = new ZipInputStream(fis, Charset.forName("EUC-KR"));
			while ((zipentry = zis.getNextEntry()) != null) {
				String filename = zipentry.getName();

				File file = new File(zipUnzipPath, filename);
				if (zipentry.isDirectory()) {

					file.mkdirs();
				} else {
					try {
						createFile(file, zis);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
			isChk = true;
		} catch (Exception e) {
			isChk = false;
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return isChk;
	}

	public boolean makeFolder(String folder) {
		if (folder.length() < 0) {
			return false;
		}
		String path = folder;
		File Folder = new File(path);
		if (!Folder.exists()) {
			try {
				Folder.mkdir();

			} catch (Exception e) {
				e.getStackTrace();
			}
		} else {

		}
		return true;
	}

	private void createFile(File file, ZipInputStream zis) throws Throwable {
		File parentDir = new File(file.getParent());
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[256];
			int size = 0;
			while ((size = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, size);
			}
		} catch (Throwable e) {
			throw e;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * ########################################################################
	 * 
	 * TEST to Entity
	 * 
	 * ########################################################################
	 */

	public ConInfoEntity toEntity(ConInfoDto conInfoDto) {

//		ConInfoEntityBuilder conInfoEntityBuilder = ConInfoEntity.builder().conId(conInfoDto.getConId())
//				.conName(conInfoDto.getConName()).conType(conInfoDto.getConType()).conNum(conInfoDto.getConNum())
//				.conCnt(conInfoDto.getConCnt()).conPort(conInfoDto.getConPort()).orgName(conInfoDto.getOrgName())
//				.orgType(conInfoDto.getOrgType()).consoOrgs(conInfoDto.getConsoOrgs())
//				.couchdbYn(conInfoDto.isCouchdbYn()).gossipBootAddr(conInfoDto.getGossipBootAddress())
//				.ordererPorts(conInfoDto.getOrdererPorts());
//
//		if (conInfoDto.getCreatedAt() == null) {
//			return conInfoEntityBuilder.build();
//		} else {
//			return conInfoEntityBuilder.createdAt(conInfoDto.getCreatedAt()).build();
//		}

		return ConInfoEntity.builder().conId(conInfoDto.getConId()).conName(conInfoDto.getConName())
				.conType(conInfoDto.getConType()).conNum(conInfoDto.getConNum()).conCnt(conInfoDto.getConCnt())
				.conPort(conInfoDto.getConPort()).orgName(conInfoDto.getOrgName()).orgType(conInfoDto.getOrgType())
				.consoOrgs(conInfoDto.getConsoOrgs()).couchdbYn(conInfoDto.isCouchdbYn())
				.gossipBootAddr(conInfoDto.getGossipBootAddr()).ordererPorts(conInfoDto.getOrdererPorts())
				.createdAt(conInfoDto.getCreatedAt()).build();
	}

	public CcInfoEntity toEntity(CcInfoDto ccInfoDto) {

//		CcInfoEntityBuilder ccInfoEntityBuilder = CcInfoEntity.builder().ccName(ccInfoDto.getCcName())
//				.ccPath(ccInfoDto.getCcPath()).ccLang(ccInfoDto.getCcLang()).ccDesc(ccInfoDto.getCcDesc());
//
//		if (ccInfoDto.getCreatedAt() == null) {
//			return ccInfoEntityBuilder.build();
//		} else {
//			return ccInfoEntityBuilder.createdAt(ccInfoDto.getCreatedAt()).build();
//		}
//
		return CcInfoEntity.builder().id(ccInfoDto.getId()).ccName(ccInfoDto.getCcName()).ccPath(ccInfoDto.getCcPath())
				.ccLang(ccInfoDto.getCcLang()).ccDesc(ccInfoDto.getCcDesc()).ccVersion(ccInfoDto.getCcVersion())
				.createdAt(ccInfoDto.getCreatedAt()).build();
	}

	public CcInfoPeerEntity toEntity(CcInfoPeerDto ccInfoPeerDto) {

//		CcInfoPeerEntityBuilder ccInfoPeerEntityBuilder = CcInfoPeerEntity.builder().id(ccInfoPeerDto.getId())
//				.ccVersion(ccInfoPeerDto.getCcVersion()).conInfoEntity(ccInfoPeerDto.getConInfoEntity())
//				.ccInfoEntity(ccInfoPeerDto.getCcInfoEntity());
//
//		if (ccInfoPeerDto.getCreatedAt() == null) {
//			return ccInfoPeerEntityBuilder.build();
//		} else {
//			return ccInfoPeerEntityBuilder.createdAt(ccInfoPeerDto.getCreatedAt()).build();
//		}

		return CcInfoPeerEntity.builder().id(ccInfoPeerDto.getId()).ccVersion(ccInfoPeerDto.getCcVersion())
				.conInfoEntity(toEntity(ccInfoPeerDto.getConInfoDto()))
				.ccInfoEntity(toEntity(ccInfoPeerDto.getCcInfoDto())).createdAt(ccInfoPeerDto.getCreatedAt()).build();
	}

	public CcInfoChannelEntity toEntity(CcInfoChannelDto ccInfoChannelDto) {

//		CcInfoChannelEntityBuilder ccInfoChannelEntityBuilder = CcInfoChannelEntity.builder()
//				.id(ccInfoChannelDto.getId()).ccVersion(ccInfoChannelDto.getCcVersion())
//				.channelInfoEntity(ccInfoChannelDto.getChannelInfoEntity())
//				.ccInfoEntity(ccInfoChannelDto.getCcInfoEntity());
//
//		if (ccInfoChannelDto.getCreatedAt() == null) {
//			return ccInfoChannelEntityBuilder.build();
//		} else {
//			return ccInfoChannelEntityBuilder.createdAt(ccInfoChannelDto.getCreatedAt()).build();
//		}

		return CcInfoChannelEntity.builder().id(ccInfoChannelDto.getId()).ccVersion(ccInfoChannelDto.getCcVersion())
				.channelInfoEntity(toEntity(ccInfoChannelDto.getChannelInfoDto()))
				.ccInfoEntity(toEntity(ccInfoChannelDto.getCcInfoDto())).createdAt(ccInfoChannelDto.getCreatedAt())
				.build();

	}

	public ChannelInfoEntity toEntity(ChannelInfoDto channelInfoDto) {

//		ChannelInfoEntityBuilder channelInfoEntityBuilder = ChannelInfoEntity.builder()
//				.channelName(channelInfoDto.getChannelName()).channelBlock(channelInfoDto.getChannelBlock())
//				.channelTx(channelInfoDto.getChannelTx()).orderingOrg(channelInfoDto.getOrderingOrg())
//				.appAdminPolicyType(channelInfoDto.getAppAdminPolicyType())
//				.appAdminPolicyValue(channelInfoDto.getAppAdminPolicyValue())
//				.ordererAdminPolicyType(channelInfoDto.getOrdererAdminPolicyType())
//				.ordererAdminPolicyValue(channelInfoDto.getOrdererAdminPolicyValue())
//				.channelAdminPolicyType(channelInfoDto.getChannelAdminPolicyType())
//				.channelAdminPolicyValue(channelInfoDto.getChannelAdminPolicyValue())
//				.batchTimeout(channelInfoDto.getBatchTimeout()).batchSizeAbsolMax(channelInfoDto.getBatchSizeAbsolMax())
//				.batchSizeMaxMsg(channelInfoDto.getBatchSizeMaxMsg())
//				.batchSizePreferMax(channelInfoDto.getBatchSizePreferMax());
//
//		if (channelInfoDto.getCreatedAt() == null) {
//			return channelInfoEntityBuilder.build();
//		} else {
//			return channelInfoEntityBuilder.createdAt(channelInfoDto.getCreatedAt()).build();
//		}

		return ChannelInfoEntity.builder().channelName(channelInfoDto.getChannelName())
				.channelBlock(channelInfoDto.getChannelBlock()).channelTx(channelInfoDto.getChannelTx())
				.orderingOrg(channelInfoDto.getOrderingOrg()).appAdminPolicyType(channelInfoDto.getAppAdminPolicyType())
				.appAdminPolicyValue(channelInfoDto.getAppAdminPolicyValue())
				.ordererAdminPolicyType(channelInfoDto.getOrdererAdminPolicyType())
				.ordererAdminPolicyValue(channelInfoDto.getOrdererAdminPolicyValue())
				.channelAdminPolicyType(channelInfoDto.getChannelAdminPolicyType())
				.channelAdminPolicyValue(channelInfoDto.getChannelAdminPolicyValue())
				.batchTimeout(channelInfoDto.getBatchTimeout()).batchSizeAbsolMax(channelInfoDto.getBatchSizeAbsolMax())
				.batchSizeMaxMsg(channelInfoDto.getBatchSizeMaxMsg())
				.batchSizePreferMax(channelInfoDto.getBatchSizePreferMax()).createdAt(channelInfoDto.getCreatedAt())
				.build();
	}

	public ChannelInfoPeerEntity toEntity(ChannelInfoPeerDto channelInfoPeerDto) {

//		ChannelInfoPeerEntityBuilder channelInfoPeerEntityBuilder = ChannelInfoPeerEntity.builder()
//				.id(channelInfoPeerDto.getId()).anchorYn(channelInfoPeerDto.isAnchorYn())
//				.conInfoEntity(channelInfoPeerDto.getConInfoEntity())
//				.channelInfoEntity(channelInfoPeerDto.getChannelInfoEntity());
//
//		if (channelInfoPeerDto.getCreatedAt() == null) {
//			return channelInfoPeerEntityBuilder.build();
//		} else {
//			return channelInfoPeerEntityBuilder.createdAt(channelInfoPeerDto.getCreatedAt()).build();
//		}

		return ChannelInfoPeerEntity.builder().id(channelInfoPeerDto.getId()).anchorYn(channelInfoPeerDto.isAnchorYn())
				.conInfoEntity(toEntity(channelInfoPeerDto.getConInfoDto()))
				.channelInfoEntity(toEntity(channelInfoPeerDto.getChannelInfoDto()))
				.createdAt(channelInfoPeerDto.getCreatedAt()).build();
	}

	public ChannelHandleEntity toEntity(ChannelHandleDto channelHandleDto) {

//		ChannelHandleEntityBuilder channelHandleEntityBuilder = ChannelHandleEntity.builder()
//				.handle(channelHandleDto.getHandle()).channelName(channelHandleDto.getChannelName());
//		if (channelHandleDto.getCreatedAt() == null) {
//			return channelHandleEntityBuilder.build();
//		} else {
//			return channelHandleEntityBuilder.createdAt(channelHandleDto.getCreatedAt()).build();
//		}

		return ChannelHandleEntity.builder().handle(channelHandleDto.getHandle())
				.channelName(channelHandleDto.getChannelName()).createdAt(channelHandleDto.getCreatedAt()).build();
	}

	public TransactionEntity toEntity(TransactionDto transactionDto) {

//		TransactionEntityBuilder transactionEntityBuilder = TransactionEntity.builder().txID(transactionDto.getTxID())
//				.creatorId(transactionDto.getCreatorId()).txType(transactionDto.getTxType())
//				.timestamp(transactionDto.getTimestamp()).ccName(transactionDto.getCcName())
//				.ccVersion(transactionDto.getCcVersion()).ccArgs(transactionDto.getCcArgs())
//				.blockEntity(transactionDto.getBlockEntity()).channelInfoEntity(transactionDto.getChannelInfoEntity());
//
//		if (transactionDto.getCreatedAt() == null) {
//			return transactionEntityBuilder.build();
//		} else {
//			return transactionEntityBuilder.createdAt(transactionDto.getCreatedAt()).build();
//		}

		return TransactionEntity.builder().txID(transactionDto.getTxID()).creatorId(transactionDto.getCreatorId())
				.txType(transactionDto.getTxType()).timestamp(transactionDto.getTimestamp())
				.ccName(transactionDto.getCcName()).ccVersion(transactionDto.getCcVersion())
				.ccArgs(transactionDto.getCcArgs()).blockEntity(toEntity(transactionDto.getBlockDto()))
				.channelInfoEntity(toEntity(transactionDto.getChannelInfoDto()))
				.createdAt(transactionDto.getCreatedAt()).build();
	}

	public BlockEntity toEntity(BlockDto blockDto) {

//		BlockEntityBuilder blockEntityBuilder = BlockEntity.builder().blockDataHash(blockDto.getBlockDataHash())
//				.blockNum(blockDto.getBlockNum()).txCount(blockDto.getTxCount())
//				.prevDataHash(blockDto.getPrevDataHash()).channelInfoEntity(blockDto.getChannelInfoEntity());
//
//		if (blockDto.getCreatedAt() == null) {
//			return blockEntityBuilder.build();
//		} else {
//			return blockEntityBuilder.createdAt(blockDto.getCreatedAt()).build();
//		}

		return BlockEntity.builder().blockDataHash(blockDto.getBlockDataHash()).blockNum(blockDto.getBlockNum())
				.txCount(blockDto.getTxCount()).prevDataHash(blockDto.getPrevDataHash())
				.channelInfoEntity(toEntity(blockDto.getChannelInfoDto())).createdAt(blockDto.getCreatedAt()).build();

	}

	/*
	 * ########################################################################
	 * 
	 * TEST to Dto
	 * 
	 * ########################################################################
	 */

	public ConInfoDto toDto(ConInfoEntity conInfoEntity) {
		return ConInfoDto.builder().conName(conInfoEntity.getConName()).conId(conInfoEntity.getConId())
				.conCnt(conInfoEntity.getConCnt()).conType(conInfoEntity.getConType()).conNum(conInfoEntity.getConNum())
				.conPort(conInfoEntity.getConPort()).orgName(conInfoEntity.getOrgName())
				.orgType(conInfoEntity.getOrgType()).consoOrgs(conInfoEntity.getConsoOrgs())
				.couchdbYn(conInfoEntity.isCouchdbYn()).gossipBootAddr(conInfoEntity.getGossipBootAddr())
				.ordererPorts(conInfoEntity.getOrdererPorts()).createdAt(conInfoEntity.getCreatedAt()).build();

	}

	public CcInfoDto toDto(CcInfoEntity ccInfoEntity) {
		return CcInfoDto.builder().id(ccInfoEntity.getId()).ccName(ccInfoEntity.getCcName())
				.ccPath(ccInfoEntity.getCcPath()).ccLang(ccInfoEntity.getCcLang()).ccDesc(ccInfoEntity.getCcDesc())
				.ccVersion(ccInfoEntity.getCcVersion()).createdAt(ccInfoEntity.getCreatedAt()).build();
	}

	public CcInfoPeerDto toDto(CcInfoPeerEntity ccInfoPeerEntity) {
		return CcInfoPeerDto.builder().id(ccInfoPeerEntity.getId()).ccVersion(ccInfoPeerEntity.getCcVersion())
				.conInfoDto(toDto(ccInfoPeerEntity.getConInfoEntity()))
				.ccInfoDto(toDto(ccInfoPeerEntity.getCcInfoEntity())).createdAt(ccInfoPeerEntity.getCreatedAt())
				.build();
	}

	public CcInfoChannelDto toDto(CcInfoChannelEntity ccInfoChannelEntity) {
		return CcInfoChannelDto.builder().id(ccInfoChannelEntity.getId()).ccVersion(ccInfoChannelEntity.getCcVersion())
				.channelInfoDto(toDto(ccInfoChannelEntity.getChannelInfoEntity()))
				.ccInfoDto(toDto(ccInfoChannelEntity.getCcInfoEntity())).createdAt(ccInfoChannelEntity.getCreatedAt())
				.build();
	}

	public ChannelInfoDto toDto(ChannelInfoEntity channelInfoEntity) {

		return ChannelInfoDto.builder().channelName(channelInfoEntity.getChannelName())
				.channelBlock(channelInfoEntity.getChannelBlock()).channelTx(channelInfoEntity.getChannelTx())
				.orderingOrg(channelInfoEntity.getOrderingOrg())
				.appAdminPolicyType(channelInfoEntity.getAppAdminPolicyType())
				.appAdminPolicyValue(channelInfoEntity.getAppAdminPolicyValue())
				.ordererAdminPolicyType(channelInfoEntity.getOrdererAdminPolicyType())
				.ordererAdminPolicyValue(channelInfoEntity.getOrdererAdminPolicyValue())
				.channelAdminPolicyType(channelInfoEntity.getChannelAdminPolicyType())
				.channelAdminPolicyValue(channelInfoEntity.getChannelAdminPolicyValue())
				.batchTimeout(channelInfoEntity.getBatchTimeout())
				.batchSizeAbsolMax(channelInfoEntity.getBatchSizeAbsolMax())
				.batchSizeMaxMsg(channelInfoEntity.getBatchSizeMaxMsg())
				.batchSizePreferMax(channelInfoEntity.getBatchSizePreferMax())
				.createdAt(channelInfoEntity.getCreatedAt()).build();
	}

	public ChannelInfoPeerDto toDto(ChannelInfoPeerEntity channelInfoPeerEntity) {
		return ChannelInfoPeerDto.builder().id(channelInfoPeerEntity.getId())
				.anchorYn(channelInfoPeerEntity.isAnchorYn())
				.conInfoDto(toDto(channelInfoPeerEntity.getConInfoEntity()))
				.channelInfoDto(toDto(channelInfoPeerEntity.getChannelInfoEntity()))
				.createdAt(channelInfoPeerEntity.getCreatedAt()).build();
	}

	public ChannelHandleDto toDto(ChannelHandleEntity channelHandleEntity) {
		return ChannelHandleDto.builder().handle(channelHandleEntity.getHandle())
				.channelName(channelHandleEntity.getChannelName()).createdAt(channelHandleEntity.getCreatedAt())
				.build();

	}

	public TransactionDto toDto(TransactionEntity transactionEntity) {
		return TransactionDto.builder().txID(transactionEntity.getTxID()).creatorId(transactionEntity.getCreatorId())
				.txID(transactionEntity.getTxType()).timestamp(transactionEntity.getTimestamp())
				.ccName(transactionEntity.getCcName()).ccVersion(transactionEntity.getCcVersion())
				.ccArgs(transactionEntity.getCcArgs()).blockDto(toDto(transactionEntity.getBlockEntity()))
				.channelInfoDto(toDto(transactionEntity.getChannelInfoEntity()))
				.createdAt(transactionEntity.getCreatedAt()).build();
	}

	public BlockDto toDto(BlockEntity blockEntity) {
		return BlockDto.builder().blockDataHash(blockEntity.getBlockDataHash()).blockNum(blockEntity.getBlockNum())
				.txCount(blockEntity.getTxCount()).prevDataHash(blockEntity.getPrevDataHash())
				.channelInfoDto(toDto(blockEntity.getChannelInfoEntity())).createdAt(blockEntity.getCreatedAt())
				.build();

	}

	/*
	 * ########################################################################
	 * 
	 * TEST
	 * 
	 * ########################################################################
	 */

//	public Object configtxRequest(String url, String channelName, byte[] param1, byte[] param2) {
//		Object result = "";
//	
//		try {
//
//			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//			factory.setConnectTimeout(5000); // 타임아웃 설정 5초
//			factory.setReadTimeout(5000);// 타임아웃 설정 5초
//			RestTemplate restTemplate = new RestTemplate(factory);
//			HttpHeaders header = new HttpHeaders();
//			ResponseEntity<String> resultMap;
//
//			if (url.contains("compute")) {
//				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//				header.setContentType(MediaType.MULTIPART_FORM_DATA);
//				map.add("original", param1.toString());
//				map.add("updated", param2.toString());
//				map.add("channel", channelName);
//				System.out.println(param1.toString());
//				System.out.println(param2.toString());
//
//				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
//						header);
//
//				resultMap = restTemplate.postForEntity(url, request, String.class);
//
//			} else {
//				HttpEntity<?> entity = new HttpEntity<>(header);
//
////				String url = "http://192.168.65.169:7059/protolator/decode/common.Config";
//
//				resultMap = restTemplate.postForEntity(url, param1, String.class);
//			}
//
//			result = resultMap.getBody();
//
//		} catch (HttpClientErrorException | HttpServerErrorException e) {
//
//			System.out.println(e.toString());
//
//		} catch (Exception e) {
//
//			System.out.println(e.toString());
//		}
//		return result;
//	}
//
//	public byte[] computeRequest(String url, String channelName, byte[] param1, byte[] param2) {
//		byte[] result = null;
//		
//		File test1 = new File(System.getProperty("user.dir") + "/config.pb");
//		File test2 = new File(System.getProperty("user.dir") + "/zz.pb");
//		FileSystemResource filetest1 = new FileSystemResource(test1);
//		FileSystemResource filetest2 = new FileSystemResource(test2);
//		ByteArrayResource bytetest1 = new ByteArrayResource(param1);
//		ByteArrayResource bytetest2 = new ByteArrayResource(param2);
////		ByteArrayResource
//
//		try {
//
//			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//			factory.setConnectTimeout(5000); // 타임아웃 설정 5초
//			factory.setReadTimeout(5000);// 타임아웃 설정 5초
//			RestTemplate restTemplate = new RestTemplate(factory);
//			HttpHeaders header = new HttpHeaders();
//			ResponseEntity<byte[]> resultMap = null;
//
//			if (url.contains("compute")) {
//				MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
//				header.setContentType(MediaType.MULTIPART_FORM_DATA);
//				map.add("channel", channelName);
//				map.add("original", filetest1);
//				map.add("updated", filetest2);
//				
//
//				resultMap = restTemplate.postForEntity(url, map, byte[].class);
//
//			} else {
//				HttpEntity<?> entity = new HttpEntity<>(header);
//
////				String url = "http://192.168.65.169:7059/protolator/decode/common.Config";
//
//				resultMap = restTemplate.postForEntity(url, param1, byte[].class);
//			}
//
//			result = resultMap.getBody();
//
//		} catch (HttpClientErrorException | HttpServerErrorException e) {
//
//			System.out.println(e.toString());
//
//		} catch (Exception e) {
//
//			System.out.println(e.toString());
//		}
//		return result;
//	}

}
