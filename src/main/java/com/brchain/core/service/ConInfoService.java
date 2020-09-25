package com.brchain.core.service;

import java.util.ArrayList;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.ConInfoRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ConInfoService {

	@NonNull
	private ConInfoRepository conInfoRepository;

	@Value("${brchain.ip}")
	String ip;

	/**
	 * 컨테이너 정보 저장 서비스
	 * 
	 * @param conInfoDto 컨테이너 정보 DTO
	 * 
	 * @return
	 */

	public ConInfoEntity saveConInfo(ConInfoDto conInfoDto) {

		return conInfoRepository.save(conInfoDto.toEntity());

	}

	/**
	 * 컨테이너 정보 삭제 서비스
	 * 
	 * @param conId 삭제할 컨테이너 ID
	 * 
	 * @return 삭제한 조직명
	 */

	public String removeConInfo(String conId) {

		Optional<ConInfoEntity> conInfoEntityWrapper = conInfoRepository.findById(conId);
		ConInfoEntity conInfoEntity = conInfoEntityWrapper.get();

		conInfoRepository.deleteById(conInfoEntity.getConId());

		return conInfoEntity.getOrgName();
	}

	/**
	 * 컨테이너 이름으로 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 컨테이너 정보
	 */

	public ConInfoDto selectByConName(String conName) {

		ConInfoEntity conInfoEntity = conInfoRepository.findByConName(conName);

		return ConInfoDto.builder().conId(conInfoEntity.getConId()).conName(conInfoEntity.getConName())
				.conType(conInfoEntity.getConType()).conNum(conInfoEntity.getConNum()).conCnt(conInfoEntity.getConCnt())
				.conPort(conInfoEntity.getConPort()).orgName(conInfoEntity.getOrgName())
				.orgType(conInfoEntity.getOrgType()).couchdbYn(conInfoEntity.isCouchdbYn())
				.gossipBootAddress(conInfoEntity.getGossipBootAddr()).ordererPorts(conInfoEntity.getOrdererPorts())
				.build();

	}

	/**
	 * 컨테이너 타입으로 조회 서비스
	 * 
	 * @param conType 컨테이너 타입
	 * @param orgType 조직 타입
	 * 
	 * @return
	 */

	public String selectByConType(String conType, String orgType) {

		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgType(conType, orgType);
		String result = "";
		for (ConInfoEntity entity : conInfoEntity) {

			ConInfoDto conInfoDto = ConInfoDto.builder().conId(entity.getConId()).conName(entity.getConName())
					.conType(entity.getConType()).conNum(entity.getConNum()).conCnt(entity.getConCnt())
					.conPort(conInfoEntity.get(0).getConPort()).orgName(entity.getOrgName())
					.orgType(entity.getOrgType()).couchdbYn(entity.isCouchdbYn())
					.gossipBootAddress(entity.getGossipBootAddr()).ordererPorts(entity.getOrdererPorts()).build();

			result = result + conInfoDto.getOrgName() + " ";
		}

		return result;

	}


	public ResultDto getOrgList() {

		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConType("ca");
		JSONArray resultJsonArr = new JSONArray();
		ResultDto resultDto = new ResultDto();
		
		String result = "";
		for (ConInfoEntity entity : conInfoEntity) {


			
			JSONObject resultJson = new JSONObject();
			
			resultJson.put("orgName",entity.getOrgName());
			resultJson.put("orgType",entity.getOrgType());
			resultJson.put("conCnt",entity.getConCnt());
			
			resultJsonArr.add(resultJson);

		}

		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success get container info");
		resultDto.setResultData(resultJsonArr);

		return resultDto;
		
		

	}
	
//	
//	/**
//	 * PeerDto 생성 서비스
//	 * 
//	 * @param orgName 조직 이름
//	 * 
//	 * @return
//	 */
//	
//	public FabricMemberDto createPeerDto(String orgName) {
//
//
//		FabricMemberDto result = new FabricMemberDto();
//		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgName("ca", orgName);
//
//		result.setCaUrl("http://"+ip+":" + conInfoEntity.get(0).getConPort());
//
//		conInfoEntity = conInfoRepository.findByConTypeAndOrgName("peer", orgName);
//
//		ConInfoDto peerDto = new ConInfoDto();
//		peerDto = ConInfoDto.builder().conId(conInfoEntity.get(0).getConId()).conName(conInfoEntity.get(0).getConName())
//				.conType(conInfoEntity.get(0).getConType()).conNum(conInfoEntity.get(0).getConNum())
//				.conCnt(conInfoEntity.get(0).getConCnt()).conPort(conInfoEntity.get(0).getConPort())
//				.orgName(conInfoEntity.get(0).getOrgName()).orgType(conInfoEntity.get(0).getOrgType())
//				.couchdbYn(conInfoEntity.get(0).isCouchdbYn())
//				.gossipBootAddress(conInfoEntity.get(0).getGossipBootAddr())
//				.ordererPorts(conInfoEntity.get(0).getOrdererPorts()).build();
//
//		result.setConName(peerDto.getConName());
//		result.setConNum(peerDto.getConNum());
//		result.setConPort(peerDto.getConPort());
//		result.setConUrl("grpcs://"+ip+":" + peerDto.getConPort());
//		result.setOrgMspId(peerDto.getOrgName() + "MSP");
//		result.setOrgName(peerDto.getOrgName());
//		result.setOrgType(peerDto.getOrgType());
//
//		return result;
//
//	}

	/**
	 * FabricMemberDTO 생성 서비스
	 * 
	 * @param orgType 조직 타입
	 * @param orgName 조직명
	 * 
	 * @return FabricMemberDTO 배열
	 */

	public ArrayList<FabricMemberDto> createMemberDtoArr(String orgType, String orgName) {

		ArrayList<FabricMemberDto> resultArr = new ArrayList<FabricMemberDto>();
		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConTypeAndOrgTypeAndOrgName("ca", orgType,
				orgName);

		String caUrl = "http://" + ip + ":" + conInfoArr.get(0).getConPort();

		conInfoArr = conInfoRepository.findByConTypeAndOrgName(orgType, orgName);

		for (ConInfoEntity conInfo : conInfoArr) {
			FabricMemberDto memberDto = new FabricMemberDto();

			memberDto.setConName(conInfo.getConName());
			memberDto.setConNum(conInfo.getConNum());
			memberDto.setConPort(conInfo.getConPort());
			memberDto.setConUrl("grpcs://" + ip + ":" + conInfo.getConPort());
			memberDto.setOrgMspId(conInfo.getOrgName() + "MSP");
			memberDto.setOrgName(conInfo.getOrgName());
			memberDto.setOrgType(conInfo.getOrgType());
			memberDto.setCaUrl(caUrl);

			resultArr.add(memberDto);

		}

		return resultArr;

	}

//	public FabricMemberDto createOrdererVo(String orgName) {
//
//		
//		FabricMemberDto result = new FabricMemberDto();
//		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgName("ca", orgName);
//
//		result.setCaUrl("http://"+ip+":" + conInfoEntity.get(0).getConPort());
//
//		conInfoEntity = conInfoRepository.findByConTypeAndOrgName("orderer", orgName);
//
//		ConInfoDto peerDto = new ConInfoDto();
//		peerDto = ConInfoDto.builder().conId(conInfoEntity.get(0).getConId()).conName(conInfoEntity.get(0).getConName())
//				.conType(conInfoEntity.get(0).getConType()).conNum(conInfoEntity.get(0).getConNum())
//				.conCnt(conInfoEntity.get(0).getConCnt()).conPort(conInfoEntity.get(0).getConPort())
//				.orgName(conInfoEntity.get(0).getOrgName()).orgType(conInfoEntity.get(0).getOrgType())
//				.couchdbYn(conInfoEntity.get(0).isCouchdbYn())
//				.gossipBootAddress(conInfoEntity.get(0).getGossipBootAddr())
//				.ordererPorts(conInfoEntity.get(0).getOrdererPorts()).build();
//
//		result.setConName(peerDto.getConName());
//		result.setConNum(peerDto.getConNum());
//		result.setConPort(peerDto.getConPort());
//		result.setConUrl("grpcs://"+ip+":" + peerDto.getConPort());
//		result.setOrgMspId(peerDto.getOrgName() + "MSP");
//		result.setOrgName(peerDto.getOrgName());
//		result.setOrgType(peerDto.getOrgType());
//
//		return result;
//
//	}

	/**
	 * 컨소시엄 확인 서비스
	 * 
	 * @param ordererOrgName 오더러 조직 이름
	 * @param peerOrgName 피어 조직 이름
	 * 
	 * @return 컨소시엄에 있는지 여부
	 */

	public boolean isMemOfConso(String ordererOrgName, String peerOrgName) {

		ArrayList<ConInfoEntity> conInfoEntityArr = conInfoRepository.findByConTypeAndOrgName("orderer",
				ordererOrgName);

		String[] consoList = conInfoEntityArr.get(0).getConsoOrgs().split(" ");

		for (int i = 0; i < consoList.length; i++) {

			if (consoList[i].equals(peerOrgName)) {

				return true;

			}
			;
		}

		return false;

	}

	
	/**
	 * 컨소시움 수정 서비스
	 * 
	 * @param ordererOrgName 오더러 조직 이름
	 * @param peerOrgName 피어 조직 이름
	 */
	
	public void updateConsoOrgs(String ordererOrgName, String peerOrgName) {
		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);

		for (ConInfoEntity conInfo : conInfoArr) {
			conInfo.setConsoOrgs(conInfo.getConsoOrgs() + peerOrgName + " ");

			conInfoRepository.save(conInfo);
		}

	}
	
	public ResultDto checkConPort(String port) {
		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConPort(port);

		
		ResultDto resultDto = new ResultDto();
		resultDto.setResultCode(conInfoArr.isEmpty()?"0000":"9999");
		resultDto.setResultFlag(conInfoArr.isEmpty());
		resultDto.setResultMessage(conInfoArr.isEmpty()?"사용가능":"사용불가");
		
		return resultDto;

	

	}

}
