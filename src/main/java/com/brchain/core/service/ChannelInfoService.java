package com.brchain.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.ChannelInfoDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.ChannelInfoRepository;
import com.brchain.core.repository.ConInfoRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChannelInfoService {

	@NonNull
	private ChannelInfoRepository channelInfoRepository;

	public ChannelInfoEntity saveChannelInfo(ChannelInfoDto channelInfoDto) {

		return channelInfoRepository.save(channelInfoDto.toEntity());

	}

	public ResultDto getChannelList() {

		JSONArray resultJsonArr = new JSONArray();
		ResultDto resultDto = new ResultDto();

		List<ChannelInfoEntity> channelInfoArr = channelInfoRepository.findAll();

		for (ChannelInfoEntity channelInfo : channelInfoArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("channelBlock", channelInfo.getChannelBlock());
			resultJson.put("channelTx", channelInfo.getChannelTx());
			resultJson.put("channelName", channelInfo.getChannelName());
			resultJson.put("orderingOrg", channelInfo.getOrderingOrg());

			resultJsonArr.add(resultJson);
		}
		
		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success get channel info");
		resultDto.setResultData(resultJsonArr);

		return resultDto;
	}

}
