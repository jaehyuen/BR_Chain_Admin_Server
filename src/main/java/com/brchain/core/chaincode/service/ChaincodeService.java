package com.brchain.core.chaincode.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.chaincode.dto.CcInfoChannelDto;
import com.brchain.core.chaincode.dto.CcInfoDto;
import com.brchain.core.chaincode.dto.CcInfoPeerDto;
import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.repository.CcInfoChannelRepository;
import com.brchain.core.chaincode.repository.CcInfoPeerRepository;
import com.brchain.core.chaincode.repository.CcInfoRepository;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.channel.dto.ChannelInfoPeerDto;
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

	public CcInfoDto findCcInfoById(Long id) {

		return util.toDto(ccInfoRepository.findById(id)
			.orElseThrow(IllegalArgumentException::new));

	}

	/**
	 * 체인코드 리스트 조회 서비스
	 * 
	 * @return 체인코드 조회 결과 DTO
	 */

	public ResultDto getCcList() {

		JSONArray          resultJsonArr = new JSONArray();

		List<CcInfoEntity> ccInfoArr     = ccInfoRepository.findAll();


		return util.setResult("0000", true, "Success get chaincode info", ccInfoArr);

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
	 * @return 체인코드 정보 (피어) 조회 결과 DTO
	 */
	@Transactional
	public ResultDto getCcListPeer(String conName) {

		JSONArray                   resultJsonArr       = new JSONArray();

		ArrayList<CcInfoPeerEntity> ccInfoPeerEntityArr = ccInfoPeerRepository.findByConInfoEntity(util.toEntity(containerService.findConInfoByConName(conName)));

		for (CcInfoPeerEntity ccInfoPeerEntity : ccInfoPeerEntityArr) {

			JSONObject resultJson = new JSONObject();

			resultJsonArr.add(util.toDto(ccInfoPeerEntity));
		}

		return util.setResult("0000", true, "Success get chaincode info", resultJsonArr);
	}

	/**
	 * 채널에 엑티브 가능한 상태인 체인코드 리스트 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 체인코드 리스트 조회 결과 DTO
	 */

	public ResultDto getCcListToActiveInChannel(String channelName) {
		JSONArray                     jsonArr               = new JSONArray();

//		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelService.findChannelInfoByChannelName(channelName));
		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService.findChannelInfoPeerByChannelInfo(channelName);

		for (ChannelInfoPeerDto channelInfoPeerDto : channelInfoPeerDtoArr) {

			ArrayList<CcInfoPeerEntity> ccInfoPeerEntityArr = ccInfoPeerRepository.findByConInfoEntity(util.toEntity(channelInfoPeerDto.getConInfoDto()));
			for (CcInfoPeerEntity ccInfoPeerEntity : ccInfoPeerEntityArr) {

				JSONObject ccInfoChannelJson = new JSONObject();

				jsonArr.add(util.toDto(ccInfoPeerEntity));

			}

		}

		return util.setResult("0000", true, "Success get chaincode list channel", jsonArr);

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
	 * @return 체인코드 리스트 조회 결과 DTO
	 */

	public ResultDto getCcListActive(String channelName) {
		JSONArray                      jsonArr                = new JSONArray();

//		ArrayList<CcInfoChannelEntity> ccInfoChannelEntityArr = ccInfoChannelRepository.findByChannelInfoEntity(util.toEntity(channelService.findChannelInfoByChannelName(channelName)));
		ArrayList<CcInfoChannelEntity> ccInfoChannelEntityArr = ccInfoChannelRepository.findByChannelName(channelName);

		for (CcInfoChannelEntity ccInfoChannelEntity : ccInfoChannelEntityArr) {

			JSONObject ccInfoChannelJson = new JSONObject();

			jsonArr.add(util.toDto(ccInfoChannelEntity));

		}

		return util.setResult("0000", true, "Success get actived chaincode list channel ", jsonArr);

	}

	/**
	 * 채널 정보, 체인코드 정보로 체인코드 정보 (채널) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * @param ccInfoDto      체인코드 정보 DTO
	 * 
	 * @return 조회한 체인코드 정보 (채널) DTO
	 */

//	public CcInfoChannelDto findCcInfoChannelByChannelInfoAndCcInfo(ChannelInfoDto channelInfoDto, CcInfoDto ccInfoDto) {
	public CcInfoChannelDto findByChannelNameAndCcName(String channelName, Long id) {

//		return util.toDto(ccInfoChannelRepository.findByChannelInfoEntityAndCcInfoEntity(util.toEntity(channelInfoDto), util.toEntity(ccInfoDto)).orElseThrow(IllegalArgumentException::new));
		return util.toDto(ccInfoChannelRepository.findByChannelNameAndCcName(channelName, id).orElseThrow(IllegalArgumentException::new));

	}

	public List<CcInfoPeerDto> findByccInfoId(Long id) {
//		List<CcInfoPeerEntity> ccInfoPeerEntityArr = ccInfoPeerRepository.findByccInfoId(id);
		List<CcInfoPeerEntity> ccInfoPeerEntityArr = ccInfoPeerRepository.findByCcId(id);
		List<CcInfoPeerDto>    ccInfoPeerDtoList   = new ArrayList<CcInfoPeerDto>();
		for (CcInfoPeerEntity ccInfoPeerEntity : ccInfoPeerEntityArr) {
			ccInfoPeerDtoList.add(util.toDto(ccInfoPeerEntity));
		}

		return ccInfoPeerDtoList;
	}



}
