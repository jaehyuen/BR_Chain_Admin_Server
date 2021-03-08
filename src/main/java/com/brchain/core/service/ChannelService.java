package com.brchain.core.service;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.channel.ChannelHandleDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.dto.channel.ChannelInfoPeerDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;
import com.brchain.core.repository.channel.ChannelHandleRepository;
import com.brchain.core.repository.channel.ChannelInfoPeerRepository;
import com.brchain.core.repository.channel.ChannelInfoRepository;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChannelService {

	// jpa 레파지토리
	private final ChannelInfoRepository channelInfoRepository;
	private final ChannelInfoPeerRepository channelInfoPeerRepository;
	private final ChannelHandleRepository channelHandleRepository;

	// 서비스
	private final ContainerService containerService;

	private final Util util;

	/**
	 * 채널 정보 저장 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * 
	 * @return 저장 채널 정보 DTO
	 */

	public ChannelInfoDto saveChannelInfo(ChannelInfoDto channelInfoDto) {

		return util.toDto(channelInfoRepository.save(util.toEntity(channelInfoDto)));

	}

	/**
	 * 채널 이름으로 채널 정보 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 조회한 채널 정보 DTO
	 */

	public ChannelInfoDto findChannelInfoByChannelName(String channelName) {

		return util.toDto(channelInfoRepository.findById(channelName).orElseThrow(IllegalArgumentException::new));
	}

	/**
	 * 채널 리스트 조회 서비스
	 * 
	 * @return 결과 DTO(채널 리스트)
	 */

	public ResultDto getChannelList() {

		JSONArray resultJsonArr = new JSONArray();

		List<ChannelInfoEntity> channelInfoArr = channelInfoRepository.findAll();

		for (ChannelInfoEntity channelInfoEntity : channelInfoArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("channelBlock", channelInfoEntity.getChannelBlock());
			resultJson.put("channelTx", channelInfoEntity.getChannelTx());
			resultJson.put("channelName", channelInfoEntity.getChannelName());
			resultJson.put("orderingOrg", channelInfoEntity.getOrderingOrg());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get channel info list", resultJsonArr);
	}

	/**
	 * 채널 이름으로 채널 정보 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 정보)
	 */

	public ResultDto getChannelByChannelName(String channelName) {

		JSONObject resultJson = new JSONObject();

		ChannelInfoEntity channelInfoEntity = channelInfoRepository.findById(channelName).get();

		resultJson.put("channelBlock", channelInfoEntity.getChannelBlock());
		resultJson.put("channelTx", channelInfoEntity.getChannelTx());
		resultJson.put("channelName", channelInfoEntity.getChannelName());
		resultJson.put("orderingOrg", channelInfoEntity.getOrderingOrg());

		resultJson.put("appAdminPolicyType", channelInfoEntity.getAppAdminPolicyType());
		resultJson.put("appAdminPolicyValue", channelInfoEntity.getAppAdminPolicyValue());

		resultJson.put("channelAdminPolicyType", channelInfoEntity.getChannelAdminPolicyType());
		resultJson.put("channelAdminPolicyValue", channelInfoEntity.getChannelAdminPolicyValue());

		resultJson.put("ordererAdminPolicyType", channelInfoEntity.getOrdererAdminPolicyType());
		resultJson.put("ordererAdminPolicyValue", channelInfoEntity.getOrdererAdminPolicyValue());

		resultJson.put("batchTimeout", channelInfoEntity.getBatchTimeout());
		resultJson.put("batchSizeAbsolMax", channelInfoEntity.getBatchSizeAbsolMax());
		resultJson.put("batchSizeMaxMsg", channelInfoEntity.getBatchSizeMaxMsg());
		resultJson.put("batchSizePreferMax", channelInfoEntity.getBatchSizePreferMax());

		return util.setResult("0000", true, "Success get channel info", resultJson);

	}

	/**
	 * 채널 정보 (파이) 저장 서비스
	 * 
	 * @param channelInfoPeerDto 채널 정보 (피어) 관련 DTO
	 * 
	 * @return 저장한 채널 정보 (피어) DTO
	 */

	public ChannelInfoPeerDto saveChannelInfoPeer(ChannelInfoPeerDto channelInfoPeerDto) {

		return util.doDto(channelInfoPeerRepository.save(util.toEntity(channelInfoPeerDto)));

	}

	/**
	 * 컨테이너 이름으로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 채널 정보 (피어) 조회 결과 DTO
	 */

	public ResultDto getChannelListPeerByConName(String conName) {

		JSONArray resultJsonArr = new JSONArray();

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository
				.findByConInfoEntity(util.toEntity(containerService.findConInfoByConName(conName)));

		for (ChannelInfoPeerEntity channelInfoPeer : channelInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("channelName", channelInfoPeer.getChannelInfoEntity().getChannelName());
			resultJson.put("anchorYn", channelInfoPeer.isAnchorYn());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get channel info", resultJsonArr);

	}

	/**
	 * 채널 이름으로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 채널 정보 (피어) 조회 결과 DTO
	 */

	public ResultDto getChannelListPeerByChannelName(String channelName) {

		JSONArray resultJsonArr = new JSONArray();

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository
				.findByChannelInfoEntity(util.toEntity(findChannelInfoByChannelName(channelName)));

		for (ChannelInfoPeerEntity channelInfoPeer : channelInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("conName", channelInfoPeer.getConInfoEntity().getConName());
			resultJson.put("anchorYn", channelInfoPeer.isAnchorYn());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get channel info by channel name", resultJsonArr);
	}

	/**
	 * 채널 정보로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * 
	 * @return 조회한 채널 정보 (피어) DTO 리스트
	 */

	public ArrayList<ChannelInfoPeerDto> findChannelInfoPeerByChannelInfo(ChannelInfoDto channelInfoDto) {

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerEntityArr = channelInfoPeerRepository
				.findByChannelInfoEntity(util.toEntity(channelInfoDto));

		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = new ArrayList<ChannelInfoPeerDto>();

		for (ChannelInfoPeerEntity channelInfoPeerEntity : channelInfoPeerEntityArr) {
			channelInfoPeerDtoArr.add(util.doDto(channelInfoPeerEntity));
		}

		return channelInfoPeerDtoArr;
	}

	/**
	 * 채널 정보, 컨테이너 정보로 채널 정버 (피어) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * @param conInfoDto     컨테이너 정보 DTO
	 * 
	 * @return 조회한 채널 정보 (피어) DTO 리스트
	 */

	public ArrayList<ChannelInfoPeerDto> findChannelInfoPeerByChannelNameAndConName(ChannelInfoDto channelInfoDto,
			ConInfoDto conInfoDto) {

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerEntityArr = channelInfoPeerRepository
				.findByChannelInfoEntityAndConInfoEntity(util.toEntity(channelInfoDto), util.toEntity(conInfoDto));

		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = new ArrayList<ChannelInfoPeerDto>();

		for (ChannelInfoPeerEntity channelInfoPeerEntity : channelInfoPeerEntityArr) {
			channelInfoPeerDtoArr.add(util.doDto(channelInfoPeerEntity));
		}

		return channelInfoPeerDtoArr;

	}

	/**
	 * 채널 핸들 정보 저장 서비스
	 * 
	 * @param channelHandleDto 채널 핸들 정보 DTO
	 * 
	 * @return 저장한 채널 핸들 정보 DTO
	 */

	public ChannelHandleDto saveChannelHandle(ChannelHandleDto channelHandleDto) {

		return util.toDto(channelHandleRepository.save(util.toEntity(channelHandleDto)));

	}

	/**
	 * 채널 이름으로 채널 핸들 정보 삭제 서비스
	 * 
	 * @param channelName 채널 이름
	 */

	public void deleteChannelHandle(String channelName) {

		channelHandleRepository.deleteById(channelName);

	}

	/**
	 * 채널 이름으로 채널 핸들 정보 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 조회한 채널 핸들 정보 DTO
	 */

	public ChannelHandleDto findChannelHandleByChannel(String channelName) {

		return util.toDto(channelHandleRepository.findById(channelName).orElseThrow(IllegalArgumentException::new));
	}

}
