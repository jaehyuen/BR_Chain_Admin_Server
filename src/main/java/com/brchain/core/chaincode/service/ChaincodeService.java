package com.brchain.core.chaincode.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.chaincode.dto.CcInfoChannelDto;
import com.brchain.core.chaincode.dto.CcInfoDto;
import com.brchain.core.chaincode.dto.CcInfoPeerDto;
import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.repository.CcInfoChannelRepository;
import com.brchain.core.chaincode.repository.CcInfoPeerRepository;
import com.brchain.core.chaincode.repository.CcInfoRepository;
import com.brchain.core.channel.service.ChannelService;
import com.brchain.core.container.service.ContainerService;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class ChaincodeService {

	// jpa 레파지토리
	private final CcInfoRepository        ccInfoRepository;
	private final CcInfoPeerRepository    ccInfoPeerRepository;
	private final CcInfoChannelRepository ccInfoChannelRepository;

	// 서비스
	private final ContainerService        containerService;
	private final ChannelService          channelService;

	private final Util                    util;

	private Logger                        logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 체인코드 정보 저장 서비스
	 * 
	 * @param ccInfoDto 체인코드 정보 관련 DTO
	 * 
	 * @return 저장한 체인코드 정보 DTO
	 */

	public CcInfoDto saveCcInfo(CcInfoDto ccInfoDto) {

		return util.toDto(ccInfoRepository.save(util.toEntity(ccInfoDto)));

	}

	/**
	 * 체인코드 이름으로 체인코드 정보 조회 서비스
	 * 
	 * @param ccName 체인코드 이름
	 * 
	 * @return 조죄한 체인코드 정보 DTO
	 */

//	@Transactional(readOnly = true)
	public CcInfoDto findCcInfoById(Long id) {

		return util.toDto(ccInfoRepository.findById(id)
			.orElseThrow(IllegalArgumentException::new));

	}

	/**
	 * 모든 체인코드 리스트 조회 서비스
	 * 
	 * @return 결과 DTO (체인코드 정보)
	 */
	
	@Transactional(readOnly = true)
	public ResultDto<List<CcInfoDto>> getCcList() {

		List<CcInfoEntity> ccInfoList = ccInfoRepository.findAll();

		return util.setResult("0000", true, "Success get chaincode info", ccInfoList.stream()
			.map(ccInfo -> util.toDto(ccInfo))
			.collect(Collectors.toList()));

	}

	/**
	 * 체인코드 정보 (피어) 저장 서비스
	 * 
	 * @param ccInfoPeerDto 체인코드 정보 (피어) 관련 DTO
	 * 
	 * @return 저장한 체인코드 정보 DTO
	 */

	public CcInfoPeerDto saveCcnInfoPeer(CcInfoPeerDto ccInfoPeerDto) {

		return util.toDto(ccInfoPeerRepository.save(util.toEntity(ccInfoPeerDto)));

	}

	/**
	 * 컨테이너 이름으로 체인코드 정보 (피어) 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 결과 DTO (체인코드 정보 (피어))
	 */
	@Transactional(readOnly = true)
	public ResultDto<List<CcInfoPeerDto>> getCcListPeer(String conName) {

		List<CcInfoPeerEntity> ccInfoPeerList = ccInfoPeerRepository.findByConInfoEntity(util.toEntity(containerService.findConInfoByConName(conName)));

		return util.setResult("0000", true, "Success get chaincode info", ccInfoPeerList.stream()
			.map(ccInfoPeer -> util.toDto(ccInfoPeer))
			.collect(Collectors.toList()));
	}

	/**
	 * 채널에 엑티브 가능한 상태인 체인코드 리스트 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO (체인코드 리스트)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<CcInfoPeerDto>> getCcListToActiveInChannel(String channelName) {
		
		List<CcInfoPeerEntity> ccInfoPeerList = ccInfoPeerRepository.findCcInfoPeerToActive(channelName);

		return util.setResult("0000", true, "Success get chaincode list channel", ccInfoPeerList.stream()
			.map(ccInfoPeer -> util.toDto(ccInfoPeer))
			.collect(Collectors.toList()));

	}

	/**
	 * 체인코드 정보 (채널) 저장 서비스
	 * 
	 * @param CcInfoChannelDto 체인코드 정보 (채널) DTO
	 * 
	 * @return 저장한 체인코드 정보 (채널) DTO
	 */

	public CcInfoChannelDto saveCcInfoChannel(CcInfoChannelDto ccInfoChannelDto) {

		return util.toDto(ccInfoChannelRepository.save(util.toEntity(ccInfoChannelDto)));

	}

	/**
	 * 채널에 엑티브된 체인코드 리스트 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(체인코드 리스트)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<CcInfoChannelDto>> getCcListActive(String channelName) {

		List<CcInfoChannelEntity> ccInfoChannelList = ccInfoChannelRepository.findByChannelName(channelName);

		return util.setResult("0000", true, "Success get actived chaincode list channel ", ccInfoChannelList.stream()
			.map(ccInfoChannel -> util.toDto(ccInfoChannel))
			.collect(Collectors.toList()));

	}

	/**
	 * 채널 정보, 체인코드 정보로 체인코드 정보 (채널) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * @param ccInfoDto      체인코드 정보 DTO
	 * 
	 * @return 조회한 체인코드 정보 (채널) DTO
	 */

	
	public CcInfoChannelDto findByChannelNameAndCcName(String channelName, String ccName) {

		return util.toDto(ccInfoChannelRepository.findByChannelNameAndCcName(channelName, ccName));

	}

	/**
	 * 체인코드 id 값으로 체인코드 정보 (피어) 조회 서비스
	 * 
	 * @param id 체인코드 id
	 * 
	 * @return 조회한 체인코드 정보 (피어) DTO
	 */
	
	@Transactional(readOnly = true)
	public List<CcInfoPeerDto> findByCcInfoId(Long id) {

		List<CcInfoPeerEntity> ccInfoPeerList = ccInfoPeerRepository.findByCcId(id);

		return ccInfoPeerList.stream()
			.map(ccInfoPeer -> util.toDto(ccInfoPeer))
			.collect(Collectors.toList());
	}

	/**
	 * 체인코드 요약정보 (피어) 조회 서비스
	 * 
	 * @return 결과 DTO(체인코드 요약정보 (피어))
	 */
	@Transactional(readOnly = true)
	public ResultDto<List<CcSummaryDto>> getCcSummaryList() {

		List<CcSummaryDto> CcSummaryList = ccInfoPeerRepository.findChaincodeSummary();

		return util.setResult("0000", true, "Success get cc summary", CcSummaryList);
	}
	
//	public void test() {
//		System.out.println(ccInfoChannelRepository.testQuery("querytestchannel", "test-cc"));
//	}
	
}
