package com.brchain.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.ChannelInfoDto;
import com.brchain.core.dto.ChannelInfoPeerDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.ChannelInfoPeerRepository;
import com.brchain.core.repository.ChannelInfoRepository;
import com.brchain.core.repository.ConInfoRepository;
import com.brchain.core.util.Util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChannelInfoService {

	@NonNull
	private ChannelInfoRepository channelInfoRepository;

	@NonNull
	private ChannelInfoPeerRepository channelInfoPeerRepository;

	
	@Autowired
	private ConInfoService conInfoService;
	
	@Autowired
	private ChannelInfoService channelInfoService;

	@Autowired
	private Util util;

	/**
	 * 채널 정보 저장 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * 
	 * @return
	 */

	public ChannelInfoEntity saveChannelInfo(ChannelInfoDto channelInfoDto) {

		return channelInfoRepository.save(channelInfoDto.toEntity());

	}

	public ChannelInfoEntity findByChannelName(String channelName) {
		
		return channelInfoRepository.findById(channelName).get();
	}
	
	/**
	 * 채널 리스트 조회 서비스
	 * 
	 * @return 결과 DTO(채널 리스트)
	 */

	public ResultDto getChannelList() {

		JSONArray resultJsonArr = new JSONArray();

		List<ChannelInfoEntity> channelInfoArr = channelInfoRepository.findAll();

		for (ChannelInfoEntity channelInfo : channelInfoArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("channelBlock", channelInfo.getChannelBlock());
			resultJson.put("channelTx", channelInfo.getChannelTx());
			resultJson.put("channelName", channelInfo.getChannelName());
			resultJson.put("orderingOrg", channelInfo.getOrderingOrg());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get channel info", resultJsonArr);
	}

	/**
	 * 채널 정보 (피어) 저장 서비스
	 * 
	 * @param channelInfoPeerDto 채널 정보 (피어) 관련 DTO
	 * 
	 * @return
	 */

	public ChannelInfoPeerEntity saveChannelInfoPeer(ChannelInfoPeerDto channelInfoPeerDto) {

		return channelInfoPeerRepository.save(channelInfoPeerDto.toEntity());

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

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository.findByConInfoEntity(conInfoService.selectByConName(conName).toEntity());

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

		ArrayList<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository.findByChannelInfoEntity(channelInfoService.findByChannelName(channelName));

		for (ChannelInfoPeerEntity channelInfoPeer : channelInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("conName", channelInfoPeer.getConInfoEntity().getConName());
			resultJson.put("anchorYn", channelInfoPeer.isAnchorYn());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get channel info", resultJsonArr);
	}

}
