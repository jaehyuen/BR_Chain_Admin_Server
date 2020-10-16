package com.brchain.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.brchain.core.dto.CcInfoDto;
import com.brchain.core.dto.CcInfoPeerDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.CcInfoChannelEntity;
import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.CcInfoChannelRepository;
import com.brchain.core.repository.CcInfoPeerRepository;
import com.brchain.core.repository.CcInfoRepository;
import com.brchain.core.repository.ConInfoRepository;
import com.brchain.core.util.Util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CcInfoService {

	@NonNull
	private CcInfoRepository ccInfoRepository;

	@NonNull
	private CcInfoPeerRepository ccInfoPeerRepository;
	@NonNull
	private CcInfoChannelRepository ccInfoChannelRepository;

	@Autowired
	private Util util;

	/**
	 * 체인코드 정보 저장 서비스
	 * 
	 * @param ccInfoDto 체인코드 정보 관련 DTO
	 * 
	 * @return
	 */

	public CcInfoEntity saveCcnInfo(CcInfoDto ccInfoDto) {

		return ccInfoRepository.save(ccInfoDto.toEntity());

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

			CcInfoDto ccInfoDto = new CcInfoDto();

			ccInfoDto.setCcName(ccName);
			ccInfoDto.setCcDesc(ccDesc);
			ccInfoDto.setCcLang(ccLang);
			ccInfoDto.setCcPath(
					System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/" + ccFile.getOriginalFilename());

			saveCcnInfo(ccInfoDto);

		} catch (Exception e) {

			return util.setResult("9999", false, e.getMessage(), null);

		}

		return util.setResult("0000", true, "Success chaincode file upload", null);
	}

	/**
	 * 체인코드 언어 조회 서비스
	 * 
	 * @param ccName 체인코드 이름
	 * 
	 * @return 체인코드 언어 정보
	 */

	public String getCcLang(String ccName) {
		return ccInfoRepository.findByCcName(ccName).getCcLang();
	}

	/**
	 * 체인코드 정보 (피어) 저장 서비스
	 * 
	 * @param ccInfoPeerDto 체인코드 정보 (피어) 관련 DTO
	 * 
	 * @return
	 */

	public CcInfoPeerEntity saveCcnInfoPeer(CcInfoPeerDto ccInfoPeerDto) {

		return ccInfoPeerRepository.save(ccInfoPeerDto.toEntity());

	}

	/**
	 * 컨테이너 이름으로 체인코드 정보 (피어) 조회 서비스
	 * 
	 * @param conName 컨테이너 이름
	 * 
	 * @return 체인코드 정보 (피어) 조회 결과 DTO
	 * 
	 */

	public ResultDto getCcListPeer(String conName) {

		JSONArray resultJsonArr = new JSONArray();

		ArrayList<CcInfoPeerEntity> ccInfoPeerArr = ccInfoPeerRepository.findByConName(conName);

		for (CcInfoPeerEntity ccInfoPeer : ccInfoPeerArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("ccName", ccInfoPeer.getCcName());
			resultJson.put("ccVersion", ccInfoPeer.getCcVersion());
			resultJson.put("ccLang", ccInfoPeer.getCcLang());

			resultJsonArr.add(resultJson);
		}

		return util.setResult("0000", true, "Success get chaincode info", resultJsonArr);
	}

	/**
	 * 채널 이름으로 체인코드 정보 (채널) 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 체인코드 정보 (채널) 조회 결과 DTO
	 * 
	 */

	public ResultDto getCcListChannel(String channelName) {

		JSONArray jsonArr = new JSONArray();

		ArrayList<CcInfoChannelEntity> ccInfoChannelArr = ccInfoChannelRepository.findByChannelName(channelName);

		for (CcInfoChannelEntity ccInfoChannel : ccInfoChannelArr) {

			JSONObject ccInfoChannelJson = new JSONObject();

			ccInfoChannelJson.put("ccName", ccInfoChannel.getCcName());
			ccInfoChannelJson.put("ccVersion", ccInfoChannel.getCcVersion());
			ccInfoChannelJson.put("ccLang", ccInfoChannel.getCcLang());

			jsonArr.add(ccInfoChannelJson);

		}

		return util.setResult("0000", true, "Success get chaincode list channel", jsonArr);

	}
}
