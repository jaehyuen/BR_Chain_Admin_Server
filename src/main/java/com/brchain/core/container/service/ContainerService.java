package com.brchain.core.container.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;
import com.brchain.core.fabric.dto.FabricNodeDto;
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

		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgType(conType, orgType);
		String result = "";

		for (ConInfoEntity conInfo : conInfoList) {
			result = result + conInfo.getOrgName() + " ";
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

	@Transactional(readOnly = true)
	public ResultDto<List<ConInfoDto>> getOrgList(String orgType) {

		List<ConInfoEntity> conInfoList;

		if (orgType.equals("")) {

			conInfoList = conInfoRepository.findByConType("ca");

		} else {

			conInfoList = conInfoRepository.findByConTypeAndOrgType("ca", orgType);

		}

		return util.setResult("0000", true, "Success get container info", conInfoList.stream()
			.map(conInfo -> util.toDto(conInfo))
			.collect(Collectors.toList()));
	}

	/**
	 * 조직 이름으로 컨테이너 리스트 조회 서비스
	 * 
	 * @param orgName 조직 이름
	 * 
	 * @return 결과 DTO(조직 리스트)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<ConInfoDto>> getMemberList(String orgName) {

		List<ConInfoEntity> conInfoList = conInfoRepository.findMemberByOrgName(orgName);

		return util.setResult("0000", true, "Success get " + orgName + " member info list", conInfoList.stream()
			.map(conInfo -> util.toDto(conInfo))
			.collect(Collectors.toList()));

	}

	/**
	 * FabricNodeDto 생성 서비스
	 * 
	 * @param orgType 조직 타입
	 * @param orgName 조직명
	 * 
	 * @return FabricNodeDto 배열
	 */

	@Transactional(readOnly = true)
	public ArrayList<FabricNodeDto> createfabricNodeDtoArr(String orgType, String orgName) {

		ArrayList<FabricNodeDto> resultList  = new ArrayList<FabricNodeDto>();
		List<ConInfoEntity>        conInfoList = conInfoRepository.findByConTypeAndOrgTypeAndOrgName("ca", orgType, orgName);

		String                     caUrl       = "http://" + ip + ":" + conInfoList.get(0).getConPort();

		conInfoList = conInfoRepository.findByConTypeAndOrgName(orgType, orgName);

		for (ConInfoEntity conInfo : conInfoList) {
			FabricNodeDto fabricNodeDto = new FabricNodeDto();

			fabricNodeDto.setConName(conInfo.getConName());
			fabricNodeDto.setConNum(conInfo.getConNum());
			fabricNodeDto.setConPort(conInfo.getConPort());
			fabricNodeDto.setConUrl("grpcs://" + ip + ":" + conInfo.getConPort());
			fabricNodeDto.setOrgMspId(conInfo.getOrgName() + "MSP");
			fabricNodeDto.setOrgName(conInfo.getOrgName());
			fabricNodeDto.setOrgType(conInfo.getOrgType());
			fabricNodeDto.setCaUrl(caUrl);

			resultList.add(fabricNodeDto);
		}

		return resultList;

	}

	/**
	 * 컨소시엄 확인 서비스
	 * 
	 * @param ordererOrgName 오더러 조직 이름
	 * @param peerOrgName    피어 조직 이름
	 * 
	 * @return 컨소시엄에 있는지 여부
	 * TODO 쿼리 변경 예정
	 */

	@Transactional(readOnly = true)
	public boolean isMemOfConso(String ordererOrgName, String peerOrgName) {

		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);

		String[] consoList = conInfoList.get(0).getConsoOrgs().split(" ");

		for (int i = 0; i < consoList.length; i++) {

			if (consoList[i].equals(peerOrgName)) {

				return true;

			};
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

		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);

		for (ConInfoEntity conInfo : conInfoList) {

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

	@Transactional(readOnly = true)
	public ResultDto<String> canUseConPort(String port) {

		boolean portYn = conInfoRepository.portCheck(port);

		return util.setResult(!portYn ? "0000" : "9999", !portYn, !portYn ? "사용가능" : "사용불가", null);

	}

	public List<String> findOrgsInChannel(String channelName) {

		return conInfoRepository.findOrgsByChannelName(channelName);
	}

}
