package com.brchain.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.chaincode.CcInfoChannelDto;
import com.brchain.core.dto.channel.ChannelHandleDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelHandleEntity;
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

	private final ChannelInfoRepository channelInfoRepository;
	private final ChannelInfoPeerRepository channelInfoPeerRepository;
	private final ChannelHandleRepository channelHandleRepository;

	private final ContainerService containerService;

	private final Util util;

	/**
	 * 채널 정보 저장 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * 
	 * @return 저장 채널 정보 엔티티
	 * 
	 * TODO 파라미터를 dto로 변경해야 할거같음 
	 * TODO 리턴를 dto로 변경해야 할거같음
	 */

	public ChannelInfoEntity saveChannelInfo(ChannelInfoEntity channelInfoEntity) {

		return channelInfoRepository.save(channelInfoEntity);

	}

	/**
	 * 채널 이름으로 채널 정보 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 조회한 채널 정보 엔티티
	 * TODO 리턴를 dto로 변경해야 할거같음
	 */

	public ChannelInfoEntity findChannelInfoByChannelName(String channelName) {

		return channelInfoRepository.findById(channelName).orElseThrow(IllegalArgumentException::new);
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
	 * 채널 정보 (피어) 저장 서비스
	 * 
	 * @param channelInfoPeerDto 채널 정보 (피어) 관련 DTO
	 * 
	 * @return 저장한 채널 정보 (피어) 엔티티
	 * 
	 * TODO 파라미터를 dto로 변경해야 할거같음 
	 * TODO 리턴를 dto로 변경해야 할거같음
	 */

	public ChannelInfoPeerEntity saveChannelInfoPeer(ChannelInfoPeerEntity channelInfoPeerEntity) {

		return channelInfoPeerRepository.save(channelInfoPeerEntity);

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
				.findByConInfoEntity(containerService.findConInfoByConName(conName));

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
				.findByChannelInfoEntity(findChannelInfoByChannelName(channelName));

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
	 * @param channelInfo 채널 정보 엔티티
	 * 
	 * @return 조회한 채널 정보 (피어) 엔티티 리스트
	 * 
	 * TODO 파라미터를 dto로 변경해야 할거같음 
	 * TODO 리턴를 dto array로 변경해야 할거같음
	 */

	public ArrayList<ChannelInfoPeerEntity> findChannelInfoPeerByChannelInfo(ChannelInfoEntity channelInfoEntity) {

		return channelInfoPeerRepository.findByChannelInfoEntity(channelInfoEntity);
	}

	/**
	 * 채널 정보, 컨테이너 정보로 채널 정버 (피어) 조회 서비스
	 * 
	 * @param channelInfo 채널 정보 엔티티
	 * @param conInfo     컨테이너 정보 엔티티
	 * 
	 * @return 조회한 채널 정보 (피어) 엔티티 리스트
	 * TODO 리턴를 dto array로 변경해야 할거같음
	 */

	public ArrayList<ChannelInfoPeerEntity> findChannelInfoPeerByChannelNameAndConName(ChannelInfoEntity channelInfoEntity,
			ConInfoEntity conInfoEntity) {
		return channelInfoPeerRepository.findByChannelInfoEntityAndConInfoEntity(channelInfoEntity, conInfoEntity);

	}

	/**
	 * 채널 핸들 정보 저장 서비스
	 * 
	 * @param channelHandleEntity 채널 핸들 정보 엔티티
	 * 
	 * @return 저장한 채널 핸들 정보 엔티티
	 * 
	 * TODO 리턴를 dto로 변경해야 할거같음
	 */

	public ChannelHandleEntity saveChannelHandle(ChannelHandleDto channelHandleDto) {

		return channelHandleRepository.save(util.toEntity(channelHandleDto));

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
	 * @return 조회한 채널 핸들 정보 엔티티
	 * 
	 * TODO 리턴를 dto로 변경해야 할거같음
	 */
	
	public Optional<ChannelHandleEntity> findChannelHandleByChannel(String channelName) {

		return channelHandleRepository.findById(channelName);
	}
	
	

}
