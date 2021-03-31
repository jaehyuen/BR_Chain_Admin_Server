package com.brchain.core.channel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.channel.dto.ChannelHandleDto;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.channel.dto.ChannelInfoPeerDto;
import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.ChannelHandleRepository;
import com.brchain.core.channel.repository.ChannelInfoPeerRepository;
import com.brchain.core.channel.repository.ChannelInfoRepository;
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
//	private final ContainerService containerService;

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

	
	public List<ChannelInfoDto> findChannelInfoList() {
		List<ChannelInfoDto> channelInfoDtoList = new ArrayList<ChannelInfoDto>();

		List<ChannelInfoEntity> channelInfoArr = channelInfoRepository.findAll();

		for (ChannelInfoEntity channelInfoEntity : channelInfoArr) {

			channelInfoDtoList.add(util.toDto(channelInfoEntity));

		}
		return channelInfoDtoList;

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

			resultJsonArr.add(util.toDto(channelInfoEntity));
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

		return util.setResult("0000", true, "Success get channel info", util.toDto(channelInfoEntity));

	}

	/**
	 * 채널 정보 (파이) 저장 서비스
	 * 
	 * @param channelInfoPeerDto 채널 정보 (피어) 관련 DTO
	 * 
	 * @return 저장한 채널 정보 (피어) DTO
	 * @throws InterruptedException 
	 */

	public ChannelInfoPeerDto saveChannelInfoPeer(ChannelInfoPeerDto channelInfoPeerDto) throws InterruptedException {
		ChannelInfoPeerEntity channelInfoPeerEntity= util.toEntity(channelInfoPeerDto);
		Thread.sleep(1000);
		return util.toDto(channelInfoPeerRepository.save(channelInfoPeerEntity));

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

	List<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository.findByChannelNameOrConName(null,conName);


		for (ChannelInfoPeerEntity channelInfoPeer : channelInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

//			resultJson.put("channelName", channelInfoPeer.getChannelInfoEntity().getChannelName());
//			resultJson.put("anchorYn", channelInfoPeer.isAnchorYn());

			resultJsonArr.add(util.toDto(channelInfoPeer));
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

		List<ChannelInfoPeerEntity> channelInfoPeerArr = channelInfoPeerRepository.findByChannelNameOrConName(channelName,null);

		for (ChannelInfoPeerEntity channelInfoPeer : channelInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJsonArr.add(util.toDto(channelInfoPeer));
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

	public ArrayList<ChannelInfoPeerDto> findChannelInfoPeerByChannelInfo(String channelName) {

		List<ChannelInfoPeerEntity> channelInfoPeerEntityArr = channelInfoPeerRepository.findByChannelNameOrConName(channelName,null);

		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = new ArrayList<ChannelInfoPeerDto>();

		for (ChannelInfoPeerEntity channelInfoPeerEntity : channelInfoPeerEntityArr) {
			channelInfoPeerDtoArr.add(util.toDto(channelInfoPeerEntity));
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

	public ArrayList<ChannelInfoPeerDto> findChannelInfoPeerByChannelNameAndConName(String channelName, String conName) {

		List<ChannelInfoPeerEntity> channelInfoPeerEntityArr = channelInfoPeerRepository.findByChannelNameOrConName(channelName, conName);

		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = new ArrayList<ChannelInfoPeerDto>();

		for (ChannelInfoPeerEntity channelInfoPeerEntity : channelInfoPeerEntityArr) {
			channelInfoPeerDtoArr.add(util.toDto(channelInfoPeerEntity));
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

	public ResultDto getChannelSummaryList() {

		Calendar cal = Calendar.getInstance();
		cal.add(cal.MONTH, -1);
		
		SimpleDateFormat        dateFormat            = new SimpleDateFormat("yyyyMM");
		String                  nowMonth              = dateFormat.format(new Date());
		String                  preMonth              = dateFormat.format(cal.getTime());
		
		
		System.out.println("nowMonth : "+nowMonth+", preMonth : "+preMonth);
		List<ChannelSummaryDto> channelSummaryDtoList = channelInfoRepository.findChannelSummary("20210317","20210322");
		for (ChannelSummaryDto channelSummaryDto : channelSummaryDtoList) {

			Double preTxCnt = channelSummaryDto.getPreTxCnt();
			Double nowTxCnt = channelSummaryDto.getNowTxCnt();
			
			Double increase = Math.abs(preTxCnt - nowTxCnt);

			if (preTxCnt == 0) {
				preTxCnt = 1d;
			}

			channelSummaryDto.setPercent(increase / preTxCnt * 100);

			if (channelSummaryDto.getPreTxCnt() > channelSummaryDto.getNowTxCnt()) {
				channelSummaryDto.setFlag(false);

			} else {
				channelSummaryDto.setFlag(true);
			}

		}

		return util.setResult("0000", true, "Success get channel info by channel name", channelSummaryDtoList);
	}

}
