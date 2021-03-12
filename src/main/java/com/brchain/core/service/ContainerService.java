package com.brchain.core.service;

import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.ConInfoRepository;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContainerService {

	// jpa 레파지토리
	private final ConInfoRepository conInfoRepository;

	private final Util util;

	@Value("${brchain.ip}")
	private String ip;

	/**
	 * 컨테이너 정보 저장 서비스
	 * 
	 * @param conInfoDto 컨테이너 정보 DTO
	 * 
	 * @return 저장한 컨테이너 정보 DTO
	 */

	public ConInfoDto saveConInfo(ConInfoDto conInfoDto) {

		return util.toDto(conInfoRepository.save(util.toEntity(conInfoDto)));

	}

	/**
	 * 컨테이너 정보 삭제 서비스
	 * 
	 * @param conId 삭제할 컨테이너 ID
	 * 
	 * @return 삭제한 컨테이너 정보 DTO
	 */

	public ConInfoDto deleteConInfo(String conId) {

		ConInfoEntity conInfoEntity = conInfoRepository.findByConId(conId);

		conInfoRepository.deleteById(conInfoEntity.getConName());

		return util.toDto(conInfoEntity);

	}

	/**
	 * 컨테이너 이름으로 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 컨테이너 정보 DTO
	 */

	public ConInfoDto findConInfoByConName(String conName) {

		return util.toDto(conInfoRepository.findById(conName).orElseThrow(IllegalArgumentException::new));

	}

	/**
	 * 컨테이너 타입으로 조직 조회 서비스
	 * 
	 * @param conType 컨테이너 타입
	 * @param orgType 조직 타입
	 * 
	 * @return 조회한 조직 리스트
	 * 
	 */

	public String findConInfoByConType(String conType, String orgType) {

		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgType(conType, orgType);
		String result = "";

		for (ConInfoEntity entity : conInfoEntity) {

//			ConInfoDto conInfoDto = ConInfoDto.builder().conId(entity.getConId()).conName(entity.getConName())
//					.conType(entity.getConType()).conNum(entity.getConNum()).conCnt(entity.getConCnt())
//					.conPort(conInfoEntity.get(0).getConPort()).orgName(entity.getOrgName())
//					.orgType(entity.getOrgType()).couchdbYn(entity.isCouchdbYn())
//					.gossipBootAddr(entity.getGossipBootAddr()).ordererPorts(entity.getOrdererPorts()).build();

			ConInfoDto conInfoDto = util.toDto(entity);
			result = result + conInfoDto.getOrgName() + " ";
		}

		return result;

	}

	/**
	 * 조직 리스트 조회 서비스
	 * 
	 * @param orgType 조직 타입
	 * 
	 * @return 결과 DTO(조직 리스트)
	 */

	public ResultDto getOrgList(String orgType) {

		ArrayList<ConInfoEntity> conInfoEntityList;

		if (orgType.equals("")) {

			conInfoEntityList = conInfoRepository.findByConType("ca");

		} else {

			conInfoEntityList = conInfoRepository.findByConTypeAndOrgType("ca", orgType);

		}

		JSONArray resultJsonArr = new JSONArray();

		for (ConInfoEntity conInfoEntity : conInfoEntityList) {

			resultJsonArr.add(util.toDto(conInfoEntity));

		}

		return util.setResult("0000", true, "Success get container info", resultJsonArr);
	}

	/**
	 * 조직 이름으로 컨테이너 리스트 조회 서비스
	 * 
	 * @param orgName 조직 이름
	 * 
	 * @return 결과 DTO(조직 리스트)
	 */

	public ResultDto getMemberList(String orgName) {

		ArrayList<ConInfoEntity> conInfoEntityList = conInfoRepository.findByOrgName(orgName);

		JSONArray resultJsonArr = new JSONArray();

		for (ConInfoEntity conInfoEntity : conInfoEntityList) {

			if (conInfoEntity.getConType().contains("ca") || conInfoEntity.getConType().contains("setup")
					|| conInfoEntity.getConType().contains("couchdb")) {

				continue;
			}

			resultJsonArr.add(util.toDto(conInfoEntity));

		}

		return util.setResult("0000", true, "Success get " + orgName + " member info list", resultJsonArr);

	}

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

	/**
	 * 컨소시엄 확인 서비스
	 * 
	 * @param ordererOrgName 오더러 조직 이름
	 * @param peerOrgName    피어 조직 이름
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
	 * @param peerOrgName    피어 조직 이름
	 */

	public void updateConsoOrgs(String ordererOrgName, String peerOrgName) {

		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);

		for (ConInfoEntity conInfo : conInfoArr) {

			conInfo.setConsoOrgs(conInfo.getConsoOrgs() + peerOrgName + " ");
			conInfoRepository.save(conInfo);
		}
	}

	/**
	 * 컨테이너 포트 확인 서비스
	 * 
	 * @param port 포트
	 * 
	 * @return 결과 DTO(포트 사용가능 여부)
	 */

	public ResultDto canUseConPort(String port) {

		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConPort(port);

		ResultDto resultDto = new ResultDto();
		resultDto.setResultCode(conInfoArr.isEmpty() ? "0000" : "9999");
		resultDto.setResultFlag(conInfoArr.isEmpty());
		resultDto.setResultMessage(conInfoArr.isEmpty() ? "사용가능" : "사용불가");

		return resultDto;

	}

}
