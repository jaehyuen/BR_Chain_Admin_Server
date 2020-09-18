package com.brchain.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
//import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.PolicyDto;
import com.spotify.docker.client.exceptions.DockerException;

public class Util {

	/**
	 * connection.json 생성 함수
	 * 
	 * @param channelName 채널명
	 * @param ordererArrDto Json 생성시 필요한 오더러 리스트 관련 DTO
	 * @param peerArrDto Json 생성시 필요한 피 리스트 관련 DTO
	 * 
	 * @return JSONObject fabricJson connection.yaml
	 * 
	 * 
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
	 * @param orgName 조직명
	 * @param hostName 호스트명
	 * @param url Url
	 * 
	 * @return peer, orderer 관련 Json
	 * 
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
	 * @param json 수정할 Json
	 * @param addjson 추가할 Json
	 * @param parentsKey 부모키??
	 * 
	 * @return 추가된 Json
	 * 
	 */
	
	public JSONObject test(JSONObject json, JSONObject addjson, String parentsKey,String peerOrg) {

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

				resultJson = test((JSONObject) json.get(key), addjson, key,peerOrg);
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
	 * 
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
	 * 
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
	public JSONObject addUpdateHeader(String channelName, JSONObject dataJson ) {

		JSONObject configUpdateJson = new JSONObject();
		
		JSONObject updateJson = new JSONObject();
		
		JSONObject payloadJson = new JSONObject();
		
		JSONObject channelHeaderJson = new JSONObject();
		JSONObject headerJson = new JSONObject();
		
	
		
		channelHeaderJson.put("type",2);
		channelHeaderJson.put("channel_id",channelName);
		
		headerJson.put("channel_header",channelHeaderJson);
		
		payloadJson.put("data",configUpdateJson);
		payloadJson.put("header",headerJson);
		
		configUpdateJson.put("config_update", dataJson);
		
		updateJson.put("payload",payloadJson);
		

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
	
/* ########################################################################
 * 
 *                                   TEST
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
