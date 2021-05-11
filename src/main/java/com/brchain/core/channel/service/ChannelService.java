package com.brchain.core.channel.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final ChannelInfoRepository     channelInfoRepository;
	private final ChannelInfoPeerRepository channelInfoPeerRepository;
	private final ChannelHandleRepository   channelHandleRepository;

	// 서비스
//	private final ContainerService containerService;

	private final Util                      util;

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

		return util.toDto(channelInfoRepository.findById(channelName)
			.orElseThrow(IllegalArgumentException::new));
	}

	/**
	 * 모든 채널 정보 조회 서비스
	 * 
	 * @return 모든 채널 정보 조회 결과 DTO
	 */
	public List<ChannelInfoDto> findChannelInfoList() {

		List<ChannelInfoEntity> channelInfoList = channelInfoRepository.findAll();

		return channelInfoList.stream()
			.map(channelInfo -> util.toDto(channelInfo))
			.collect(Collectors.toList());

	}

	/**
	 * 채널 리스트 조회 서비스
	 * 
	 * @return 결과 DTO(채널 리스트)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<ChannelInfoDto>> getChannelList() {

		return util.setResult("0000", true, "Success get channel info list", findChannelInfoList());
	}

	/**
	 * 채널 이름으로 채널 정보 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO(채널 정보)
	 */

	@Transactional(readOnly = true)
	public ResultDto<ChannelInfoDto> getChannelByChannelName(String channelName) {

		ChannelInfoEntity channelInfoEntity = channelInfoRepository.findById(channelName)
			.orElseThrow(IllegalArgumentException::new);

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

	public ChannelInfoPeerDto saveChannelInfoPeer(ChannelInfoPeerDto channelInfoPeerDto) {

		ChannelInfoPeerEntity channelInfoPeerEntity = util.toEntity(channelInfoPeerDto);

		return util.toDto(channelInfoPeerRepository.save(channelInfoPeerEntity));

	}

	/**
	 * 컨테이너 이름으로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 결과 DTO (채널 정보 (피어))
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<ChannelInfoPeerDto>> getChannelListPeerByConName(String conName) {

		List<ChannelInfoPeerEntity> channelInfoPeerList = channelInfoPeerRepository.findByChannelNameOrConName(null, conName);

		return util.setResult("0000", true, "Success get channel info", channelInfoPeerList.stream()
			.map(channelInfoPeer -> util.toDto(channelInfoPeer))
			.collect(Collectors.toList()));

	}

	/**
	 * 채널 이름으로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO (채널 정보 (피어))
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<ChannelInfoPeerDto>> getChannelListPeerByChannelName(String channelName) {

		List<ChannelInfoPeerEntity> channelInfoPeerList = channelInfoPeerRepository.findByChannelNameOrConName(channelName, null);

		return util.setResult("0000", true, "Success get channel info by channel name", channelInfoPeerList.stream()
			.map(channelInfoPeer -> util.toDto(channelInfoPeer))
			.collect(Collectors.toList()));
	}

	/**
	 * 채널 정보로 채널 정보 (피어) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * 
	 * @return 조회한 채널 정보 (피어) DTO 리스트
	 */

	public List<ChannelInfoPeerDto> findChannelInfoPeerByChannelInfo(String channelName) {

		List<ChannelInfoPeerEntity> channelInfoPeerList = channelInfoPeerRepository.findByChannelNameOrConName(channelName, null);

		return channelInfoPeerList.stream()
			.map(channelInfoPeer -> util.toDto(channelInfoPeer))
			.collect(Collectors.toList());
	}

	/**
	 * 채널 정보, 컨테이너 정보로 채널 정버 (피어) 조회 서비스
	 * 
	 * @param channelInfoDto 채널 정보 DTO
	 * @param conInfoDto     컨테이너 정보 DTO
	 * 
	 * @return 조회한 채널 정보 (피어) DTO 리스트
	 */

	public List<ChannelInfoPeerDto> findChannelInfoPeerByChannelNameAndConName(String channelName, String conName) {

		List<ChannelInfoPeerEntity> channelInfoPeerList = channelInfoPeerRepository.findByChannelNameOrConName(channelName, conName);

		return channelInfoPeerList.stream()
			.map(channelInfoPeer -> util.toDto(channelInfoPeer))
			.collect(Collectors.toList());

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

		return util.toDto(channelHandleRepository.findById(channelName)
			.orElseThrow(IllegalArgumentException::new));
	}

	/**
	 * 채널 요약 정보 조회 서비스
	 * 
	 * @return 결과 DTO(채널 요약 정보)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<ChannelSummaryDto>> getChannelSummaryList() {

		Calendar cal = Calendar.getInstance();
		cal.add(cal.MONTH, -1);

		// 이번달, 지난달 변수 선언
		SimpleDateFormat        dateFormat         = new SimpleDateFormat("yyyyMM");
		String                  nowMonth           = dateFormat.format(new Date());
		String                  preMonth           = dateFormat.format(cal.getTime());

		// 채널 요약 정보 조회
		List<ChannelSummaryDto> channelSummaryList = channelInfoRepository.findChannelSummary(preMonth, nowMonth);

		// 각각의 채널 별로 증감율 계산
		for (ChannelSummaryDto channelSummary : channelSummaryList) {

			// 이번달, 지난달 트렌잭션 변수 선언
			Double preTxCnt = channelSummary.getPreTxCnt();
			Double nowTxCnt = channelSummary.getNowTxCnt();

			// 증감값 계산 (지난달 tx개수 - 이번달 tx개수)
			Double increase = Math.abs(preTxCnt - nowTxCnt);

			// 나누기 계산을 위해 이전달 tx값이 0이면 1로 변경
			if (preTxCnt == 0) {
				preTxCnt = 1d;
			}

			// 증감율 계산 ( 증감값 / 지난달 tx개수 * 100)
			channelSummary.setPercent(Math.round(increase / preTxCnt * 100));

			// false는 감소 true는 증
			if (channelSummary.getPreTxCnt() > channelSummary.getNowTxCnt()) {
				channelSummary.setFlag(false);

			} else {
				channelSummary.setFlag(true);
			}

		}

		return util.setResult("0000", true, "Success get channel info by channel name", channelSummaryList);
	}

}
