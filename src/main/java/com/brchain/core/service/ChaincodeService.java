package com.brchain.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.brchain.core.dto.ResultDto;
import com.brchain.core.dto.chaincode.CcInfoChannelDto;
import com.brchain.core.dto.chaincode.CcInfoDto;
import com.brchain.core.dto.chaincode.CcInfoPeerDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.dto.channel.ChannelInfoPeerDto;
import com.brchain.core.entity.chaincode.CcInfoChannelEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;
import com.brchain.core.repository.chaincode.CcInfoChannelRepository;
import com.brchain.core.repository.chaincode.CcInfoPeerRepository;
import com.brchain.core.repository.chaincode.CcInfoRepository;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
public class ChaincodeService {

	// jpa 레파지토리
	private final CcInfoRepository ccInfoRepository;
	private final CcInfoPeerRepository ccInfoPeerRepository;
	private final CcInfoChannelRepository ccInfoChannelRepository;

	// 서비스
	private final ContainerService containerService;
	private final ChannelService channelService;

	private final Util util;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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

	public CcInfoDto findCcInfoByCcName(String ccName) {

		return util.toDto(ccInfoRepository.findById(ccName).orElseThrow(IllegalArgumentException::new));

	}

	/**
	 * 체인코드 리스트 조회 서비스
	 * 
	 * @return 체인코드 조회 결과 DTO
	 */

	public ResultDto getCcList() {

		JSONArray resultJsonArr = new JSONArray();

		List<CcInfoEntity> ccInfoArr = ccInfoRepository.findAll();

		for (CcInfoEntity ccInfo : ccInfoArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("ccName", ccInfo.getCcName());
			resultJson.put("ccPath", ccInfo.getCcPath());
			resultJson.put("ccLang", ccInfo.getCcLang());
			resultJson.put("ccDesc", ccInfo.getCcDesc());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get chaincode info", resultJsonArr);

	}

	/**
	 * 체인코드 업로드 서비스
	 * 
	 * @param ccFile 체인코드 파일 이름
	 * @param ccName 체인코드 이름
	 * @param ccDesc 체인코드 설명
	 * @param ccLang 체인코드 언어
	 * 
	 * @return 체인코드 업로드 결과 DTO
	 */

	public ResultDto ccFileUpload(MultipartFile ccFile, String ccName, String ccDesc, String ccLang) {

		try {

			// 파일로 변경 작업
			InputStream inputStream = ccFile.getInputStream();
			File file = new File(System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/");

			if (!file.exists()) {
				try {

					file.mkdirs();

				} catch (Exception e) {

					return util.setResult("9999", false, e.getMessage(), null);

				}

			} else {

			}
			OutputStream outputStream = new FileOutputStream(new File(
					System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/" + ccFile.getOriginalFilename()));
			int i;

			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}

			outputStream.close();
			inputStream.close();

			// 디비에 저장(CCINFO)
			CcInfoDto ccInfoDto = new CcInfoDto();

			ccInfoDto.setCcName(ccName);
			ccInfoDto.setCcDesc(ccDesc);
			ccInfoDto.setCcLang(ccLang);
			ccInfoDto.setCcPath(
					System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/" + ccFile.getOriginalFilename());

			saveCcInfo(ccInfoDto);

		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success chaincode file upload", null);
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

		JSONArray resultJsonArr = new JSONArray();

		ArrayList<CcInfoPeerEntity> ccInfoPeerArr = ccInfoPeerRepository
				.findByConInfoEntity(util.toEntity(containerService.findConInfoByConName(conName)));

		for (CcInfoPeerEntity ccInfoPeer : ccInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("ccName", ccInfoPeer.getCcInfoEntity().getCcName());
			resultJson.put("ccVersion", ccInfoPeer.getCcVersion());
			resultJson.put("ccLang", ccInfoPeer.getCcInfoEntity().getCcLang());

			resultJsonArr.add(resultJson);
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
		JSONArray jsonArr = new JSONArray();

		ArrayList<ChannelInfoPeerDto> channelInfoPeerDtoArr = channelService
				.findChannelInfoPeerByChannelInfo(channelService.findChannelInfoByChannelName(channelName));

		for (ChannelInfoPeerDto channelInfoPeer : channelInfoPeerDtoArr) {

			ArrayList<CcInfoPeerEntity> ccInfoPeerArr = ccInfoPeerRepository
					.findByConInfoEntity(util.toEntity(channelInfoPeer.getConInfoDto()));
			for (CcInfoPeerEntity ccInfoPeer : ccInfoPeerArr) {

				JSONObject ccInfoChannelJson = new JSONObject();

				ccInfoChannelJson.put("ccName", ccInfoPeer.getCcInfoEntity().getCcName());
				ccInfoChannelJson.put("ccVersion", ccInfoPeer.getCcVersion());
				ccInfoChannelJson.put("ccLang", ccInfoPeer.getCcInfoEntity().getCcLang());

				jsonArr.add(ccInfoChannelJson);

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
		JSONArray jsonArr = new JSONArray();

		ArrayList<CcInfoChannelEntity> ccInfoChannelArr = ccInfoChannelRepository
				.findByChannelInfoEntity(util.toEntity(channelService.findChannelInfoByChannelName(channelName)));

		for (CcInfoChannelEntity ccInfoChannel : ccInfoChannelArr) {

			JSONObject ccInfoChannelJson = new JSONObject();

			ccInfoChannelJson.put("ccName", ccInfoChannel.getCcInfoEntity().getCcName());
			ccInfoChannelJson.put("ccVersion", ccInfoChannel.getCcVersion());
			ccInfoChannelJson.put("ccLang", ccInfoChannel.getCcInfoEntity().getCcLang());

			jsonArr.add(ccInfoChannelJson);

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

	public CcInfoChannelDto findCcInfoChannelByChannelInfoAndCcInfo(ChannelInfoDto channelInfoDto,
			CcInfoDto ccInfoDto) {

		return util
				.toDto(ccInfoChannelRepository.findByChannelInfoEntityAndCcInfoEntity(util.toEntity(channelInfoDto), util.toEntity(ccInfoDto))
						.orElseThrow(IllegalArgumentException::new));

	
	}
	
//	public void test() {
//
//		Date before =new Date(System.currentTimeMillis() -3000L);
//		Date now =new Date();
//			System.out.println(before);
//			System.out.println(now);
//			String a = ccInfoPeerRepository.
//		
//
//	}

}
