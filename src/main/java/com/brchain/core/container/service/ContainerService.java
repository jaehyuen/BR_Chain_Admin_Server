package com.brchain.core.container.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.dto.OrgInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;
import com.brchain.core.fabric.dto.FabricNodeDto;
import com.brchain.core.util.BrchainStatusCode;
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
	 * @param conInfoEntity 컨테이너 정보 Entity
	 * 
	 * @return 저장한 컨테이너 정보 Entity
	 */

	public ConInfoEntity saveConInfo(ConInfoEntity conInfoEntity) {

		return conInfoRepository.save(conInfoEntity);

	}

	/**
	 * 컨테이너 정보 삭제 서비스
	 * 
	 * @param conId 삭제할 컨테이너 ID
	 * 
	 * @return 삭제한 컨테이너 정보 Entity
	 */

	public ConInfoEntity deleteConInfo(String conId) {

		ConInfoEntity conInfoEntity = conInfoRepository.findByConId(conId).orElseThrow(IllegalArgumentException::new);

		conInfoRepository.deleteById(conInfoEntity.getConName());

		return conInfoEntity;

	}


	/**
	 * 컨테이너 이름으로 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 컨테이너 정보 Entity
	 */

	public ConInfoEntity findConInfoByConName(String conName) {

		return conInfoRepository.findById(conName).orElseThrow(IllegalArgumentException::new);

	}
	
	public ConInfoEntity findConInfoByConId(String conId) {
		return conInfoRepository.findByConId(conId).orElseThrow(IllegalArgumentException::new);
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

//		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgType(conType, orgType);
		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgTypeAndOrgName(conType, orgType,null);
		String result = "";

		for (ConInfoEntity conInfo : conInfoList) {
			result = result + conInfo.getOrgName() + " ";
		}

		return result;

	}
	
	public String findAllOrgs(String orgType) {


		return conInfoRepository.findAllOrgs();

	}
	

	/**
	 * 조직 리스트 조회 서비스
	 * 
	 * @param orgType 조직 타입
	 * 
	 * @return 결과 DTO(조직 리스트)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<OrgInfoDto>> getOrgList(String orgType) {

			
		//Success get org info list
		return util.setResult(BrchainStatusCode.SUCCESS,conInfoRepository.findOrgInfo(orgType));
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

		//Success get " + orgName + " member info list
		return util.setResult(BrchainStatusCode.SUCCESS, conInfoList.stream()
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
	public ArrayList<FabricNodeDto> createFabricNodeDtoArr(String orgType, String orgName) {

		ArrayList<FabricNodeDto> resultList    = new ArrayList<FabricNodeDto>();
		ConInfoEntity        caInfo = conInfoRepository.findCaInfoByOrgName(orgName).orElseThrow(IllegalArgumentException::new);

		String                     caUrl       = "http://" + ip + ":" + caInfo.getConPort();

//		conInfoList = conInfoRepository.findByConTypeAndOrgName(orgType, orgName);
		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgTypeAndOrgName(orgType,null, orgName);

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

//		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);
		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgTypeAndOrgName("orderer",null, ordererOrgName);

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

//		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgName("orderer", ordererOrgName);
		List<ConInfoEntity> conInfoList = conInfoRepository.findByConTypeAndOrgTypeAndOrgName("orderer",null, ordererOrgName);

		for (ConInfoEntity conInfo : conInfoList) {

			conInfo.setConsoOrgs(conInfo.getConsoOrgs() + peerOrgName + " ");
			conInfoRepository.save(conInfo);
		}
	}

	/**
	 * TODO 리턴 결과 수정해야됨 
	 * 
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

	/**
	 * 채널에 가입되어있는 조직 조회
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 조회한 조직 리스트
	 */
	public List<String> findOrgsInChannel(String channelName) {

		return conInfoRepository.findOrgsByChannelName(channelName);
	}

}
