package com.brchain.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.brchain.common.exception.BrchainException;
import com.brchain.core.fabric.dto.FabricNodeDto;
import com.brchain.core.fabric.dto.PolicyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.PortBinding;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonUtil {
	
	private Logger               logger     = LoggerFactory.getLogger(this.getClass());
	private final Util util;

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
	public JSONObject createFabrcSetting(String channelName, List<FabricNodeDto> ordererDtoArr, List<FabricNodeDto> peerDtoArr, List<String> orgs) {

		JSONObject        fabricJson        = new JSONObject();

		JSONObject        clientJson        = new JSONObject();

		JSONObject        orgJson1          = new JSONObject();

		JSONObject        channelJson1      = new JSONObject();
		JSONObject        channelJson2      = new JSONObject();

		JSONObject        peerJson          = new JSONObject();
		JSONObject        peerMemberJson    = new JSONObject();
		JSONObject        ordererMemberJson = new JSONObject();

		JSONObject        caJson1           = new JSONObject();

		ArrayList<String> ordererArr        = new ArrayList<String>();

		clientJson.put("organization", peerDtoArr.get(0).getOrgName());

		// 오더러 관련 변수 생성
		for (FabricNodeDto dto : ordererDtoArr) {
			ordererArr.add(dto.getConName());
			ordererMemberJson.put(dto.getConName(), createMemberJson(dto.getOrgName(), dto.getConName(), dto.getConUrl()));

		}

		// 피어 관련 변수 생성
		for (String org : orgs) {

			JSONObject   orgJson2 = new JSONObject();
			List<String> peerArr  = new ArrayList<String>();

			for (FabricNodeDto dto : peerDtoArr) {

				if (org.equals(dto.getOrgName())) {

					JSONObject   caJson2 = new JSONObject();
					List<String> caArr   = new ArrayList<String>();

					caArr.add("ca.org" + dto.getOrgName() + ".com");

					peerJson.put(dto.getConName(), new JSONObject());
					peerArr.add(dto.getConName());
					peerMemberJson.put(dto.getConName(), createMemberJson(dto.getOrgName(), dto.getConName(), dto.getConUrl()));

					orgJson2.put("mspid", dto.getOrgMspId());
					orgJson2.put("certificateAuthorities", caArr);

					orgJson2.put("peers", peerArr);
					orgJson1.put(dto.getOrgName(), orgJson2);

					caJson2.put("caName", "ca.org" + dto.getOrgName() + ".com");
					caJson2.put("url", dto.getCaUrl());
					caJson1.put("ca.org" + dto.getOrgName() + ".com", caJson2);

				}

			}

		}

		channelJson2.put("peers", peerJson);
		channelJson2.put("orderers", ordererArr);
		channelJson1.put(channelName, channelJson2);

		fabricJson.put("name", channelName);
		fabricJson.put("version", "1.0.0");
		fabricJson.put("client", clientJson);
		fabricJson.put("channels", channelJson1);
		fabricJson.put("organizations", orgJson1);
		fabricJson.put("orderers", ordererMemberJson);
		fabricJson.put("peers", peerMemberJson);
		fabricJson.put("certificateAuthorities", caJson1);

		logger.debug("[fabric 설정 json 생성]" + fabricJson.toString().replace("\\", ""));

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
	private JSONObject createMemberJson(String orgName, String hostName, String url) {

		JSONObject memberJson      = new JSONObject();

		JSONObject grpcOptionsJson = new JSONObject();
		JSONObject tlsCACertsJson  = new JSONObject();

		String     certPath        = "crypto-config/ca-certs/ca.org" + orgName + ".com-cert.pem";

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

		String     key        = "";
		JSONObject resultJson = new JSONObject();

		Iterator   iter       = json.keySet().iterator();
		while (iter.hasNext()) {
			key = (String) iter.next();

			if (key.equals("groups") && parentsKey.equals("SampleConsortium")) {
				logger.debug("현재키 :" + key + " 부모 키 :" + parentsKey);

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
	 * 설정파일(조직) 삭제 함수 (테스트중)
	 * 
	 * @param json       수정할 Json
	 * @param parentsKey 부모키??
	 * 
	 * @return 삭제된 Json
	 */

	public JSONObject modifyOrgConfig(JSONObject json, String parentsKey, String orgName) {

		String     key        = "";
		JSONObject resultJson = new JSONObject();

		Iterator   iter       = json.keySet().iterator();
		
		//재귀함수로 json 순회
		while (iter.hasNext()) {
			key = (String) iter.next();

			if (key.equals("groups") && parentsKey.equals("Application")) {
				logger.debug("현재키 :" + key + " 부모 키 :" + parentsKey);

				resultJson = (JSONObject) json.get(key);
				resultJson.remove(orgName);
				
				return resultJson;

			} else if (json.get(key) instanceof JSONObject) {

				resultJson = modifyOrgConfig((JSONObject) json.get(key), key, orgName);
			}

		}

		return json;
	}
	

	/**
	 * 조직 정보 json 생성 함수
	 * 
	 * @param fabricNodeDto 조직정보 Json
	 * 
	 * @return 조직 정보 Json
	 */

	@SuppressWarnings("unchecked")
	public JSONObject createOrgJson(FabricNodeDto fabricNodeDto) {

		JSONObject        orgJson          = new JSONObject();

		JSONObject        policiesJson     = new JSONObject();
		JSONObject        polJson          = new JSONObject();

		JSONObject        cryptoConfigJson = new JSONObject();
		JSONObject        configJson       = new JSONObject();
		JSONObject        valueJson        = new JSONObject();
		JSONObject        mspJson          = new JSONObject();
		JSONObject        valuesJson       = new JSONObject();

		PolicyDto         policyDto        = new PolicyDto();

		ArrayList<String> orgMspArr        = new ArrayList<String>();
		ArrayList<String> adminsArr        = new ArrayList<String>();
		ArrayList<String> rootCertsArr     = new ArrayList<String>();
		ArrayList<String> tlsRootCertsArr  = new ArrayList<String>();

		adminsArr.add(util.fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/" + fabricNodeDto.getOrgType() + "Organizations/org" + fabricNodeDto.getOrgName() + ".com/users/Admin@org" + fabricNodeDto.getOrgName() + ".com/msp/signcerts/cert.pem"));
		rootCertsArr.add(util.fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/ca-certs/ca.org" + fabricNodeDto.getOrgName() + ".com-cert.pem"));
		tlsRootCertsArr.add(util.fileEncodeBases64(System.getProperty("user.dir") + "/crypto-config/ca-certs/ca.org" + fabricNodeDto.getOrgName() + ".com-cert.pem"));

		orgMspArr.add(fabricNodeDto.getOrgMspId());

		policyDto.setPolicyType(1);
		policyDto.setSubPolicy("Writers");
		policyDto.setRule("or");
		policyDto.setIdentityMsps(orgMspArr);

		policiesJson.put("Writers", createPolicyJson(policyDto));

		policyDto.setSubPolicy("Readers");
		policiesJson.put("Readers", createPolicyJson(policyDto));

		policyDto.setSubPolicy("Endorsement");
		policiesJson.put("Endorsement", createPolicyJson(policyDto));

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
		configJson.put("name", fabricNodeDto.getOrgMspId());
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

		logger.debug("[org json 생성]" + orgJson.toString().replace("\\", ""));

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
	private JSONObject createPolicyJson(PolicyDto policyDto) {

		JSONObject returnJson        = new JSONObject();

		JSONObject policyJson        = new JSONObject();

		JSONObject valueJson         = new JSONObject();

		JSONArray  identitiesJsonArr = new JSONArray();

		JSONObject nOutOfJson        = new JSONObject();
		JSONObject roluJson          = new JSONObject();

		JSONArray  rolesJsonArr      = new JSONArray();

		// policyType이 1이면 Signature 정책
		if (policyDto.getPolicyType() == 1) {

			int cnt = 0;
			for (String identityMsp : policyDto.getIdentityMsps()) {

				JSONObject identitiesJson = new JSONObject();
				JSONObject principalJson  = new JSONObject();

				JSONObject rolesJson      = new JSONObject();

				principalJson.put("role", (policyDto.getSubPolicy()
					.equals("Admins")) ? "ADMIN" : "MEMBER");
				principalJson.put("msp_identifier", identityMsp);

				identitiesJson.put("principal", principalJson);
				identitiesJson.put("principal_classification", "ROLE");

				identitiesJsonArr.add(identitiesJson);

				rolesJson.put("signed_by", cnt);

				rolesJsonArr.add(rolesJson);

				cnt++;

			}

			nOutOfJson.put("n", (policyDto.getRule()
				.equals("and")) ? cnt : 1);
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

	/**
	 * 업데이트 헤더 json 추가 함수
	 * 
	 * @param channelName 채널 이름
	 * @param dataJson 업데이트 json
	 * 
	 * @return 헤더를 추가한 업데이트 json
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	public JSONObject addUpdateHeader(String channelName, JSONObject dataJson) {

		JSONObject configUpdateJson  = new JSONObject();

		JSONObject updateJson        = new JSONObject();

		JSONObject payloadJson       = new JSONObject();

		JSONObject channelHeaderJson = new JSONObject();
		JSONObject headerJson        = new JSONObject();

		channelHeaderJson.put("type", 2);
		channelHeaderJson.put("channel_id", channelName);

		headerJson.put("channel_header", channelHeaderJson);

		payloadJson.put("data", configUpdateJson);
		payloadJson.put("header", headerJson);

		configUpdateJson.put("config_update", dataJson);

		updateJson.put("payload", payloadJson);

		return updateJson;
	}

	/**
	 * 앵커 피어 설정 추가 함수
	 * 
	 * @param json       수정할 json
	 * @param addjson    추가할 json
	 * @param parentsKey 부모키
	 * @param peerDto    피어 정보 DTO
	 * 
	 * @return 수정된 json
	 * 
	 */
	
	public JSONObject addAnchorConfig(JSONObject json, JSONObject addjson, String parentsKey, FabricNodeDto peerDto) {

		String     key        = "";
		JSONObject resultJson = new JSONObject();

		Iterator   iter       = json.keySet().iterator();
		
		while (iter.hasNext()) {
			key = (String) iter.next();

			if (key.equals("values") && parentsKey.equals(peerDto.getOrgName())) {
				
				logger.debug("현재키 :" + key + " 부모 키 :" + parentsKey);
				resultJson = (JSONObject) json.get(key);

				if (resultJson.containsKey("AnchorPeers")) {
					JSONObject json1   = new JSONObject();
					JSONObject json2   = new JSONObject();
					JSONArray  jsonArr = new JSONArray();

					json1   = (JSONObject) resultJson.get("AnchorPeers");
					json2   = (JSONObject) json1.get("value");
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

				resultJson = addAnchorConfig((JSONObject) json.get(key), addjson, key, peerDto);
			}

		}

		return json;
	}

	/**
	 * 앵커 피어 설정 생성 함수
	 * 
	 * @param peerDto 피어 정보 DTO
	 * 
	 * @return 생성한 DTO
	 * 
	 */
	
	public JSONObject createAnchorJson(FabricNodeDto peerDto) {

		JSONObject anchorPeerJson = new JSONObject();
		JSONArray  anchorPeerArr  = new JSONArray();

		JSONObject resultJson     = new JSONObject();
		JSONObject valueJson      = new JSONObject();

		anchorPeerJson.put("host", peerDto.getConName());
		anchorPeerJson.put("port", peerDto.getConPort());

		anchorPeerArr.add(anchorPeerJson);

		valueJson.put("anchor_peers", anchorPeerArr);

		resultJson.put("version", "0");
		resultJson.put("value", valueJson);
		resultJson.put("mod_policy", "Admins");

		return resultJson;

	}

	/**
	 * 컨테이너 정보 json 생성 함수
	 * 
	 * @param info 컨테이너 정보
	 *  
	 * @return 생성된 컨테이너 정보 json
	 * 
	 */
	
	public JSONObject createConJson(ContainerInfo info) {

		JSONObject            logJson   = new JSONObject();

		JSONObject            conJson1  = new JSONObject();
		JSONObject            conJson2  = new JSONObject();

		ImmutableList<String> cmdArr    = info.config()
			.cmd();
		String                resultCmd = "";

		for (String cmd : cmdArr) {
			resultCmd = resultCmd + cmd + " ";

		}

		ImmutableMap<String, List<PortBinding>> portMap  = info.hostConfig().portBindings();

		ArrayList<String>                       portList = new ArrayList<String>();

		for (Entry<String, List<PortBinding>> entry : portMap.entrySet()) {

			String            port1       = entry.getKey();
			List<PortBinding> value       = entry.getValue();
			PortBinding       portBinding = value.get(0);
			String            port2       = portBinding.hostPort();
			
			portList.add(port1 + ":" + port2);
		}
		if (!portMap.isEmpty()) {
			conJson2.put("ports", portList);
		}

		logJson.put("driver", "none");

		conJson2.put("container_name", info.name()
			.replace("/", ""));
		conJson2.put("image", info.image());
		conJson2.put("environment", info.config()
			.env());

		conJson2.put("command", resultCmd);

		conJson2.put("logging", logJson);
		conJson2.put("volumes", info.hostConfig().binds());

		conJson2.put("networks", new String[] { info.hostConfig().networkMode() });

		return conJson2;

	}

	/**
	 * docker-compose 생성용 json 생성 함수
	 * 
	 * @param conJson 컨테이너 정보 json
	 * 
	 * @return 생성한 json
	 * 
	 */
	
	public JSONObject createComposeJson(JSONObject conJson) {

		JSONObject ipamConfigJson    = new JSONObject();

		JSONArray  ipamConfigJsonArr = new JSONArray();

		JSONObject ipamJson          = new JSONObject();

		JSONObject networksJson2     = new JSONObject();
		JSONObject networksJson1     = new JSONObject();
		JSONObject composeJson       = new JSONObject();

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

			objectMapper.writeValue(new File(System.getProperty("user.dir") + "/compose-files/" + fileName + ".yaml"), createComposeJson(conJson));
			
		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_IO_ERROR);
		
		} 
	}

}
