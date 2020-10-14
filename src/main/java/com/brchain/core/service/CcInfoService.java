package com.brchain.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.brchain.core.dto.CcInfoDto;
import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.ResultDto;
import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.CcnfoRepository;
import com.brchain.core.repository.ConInfoRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CcInfoService {

	@NonNull
	private CcnfoRepository ccInfoRepository;

	public CcInfoEntity saveConInfo(CcInfoDto ccInfoDto) {

		return ccInfoRepository.save(ccInfoDto.toEntity());

	}
	
	public ResultDto getCcList() {

		JSONArray resultJsonArr = new JSONArray();
		ResultDto resultDto = new ResultDto();

		List<CcInfoEntity> ccInfoArr = ccInfoRepository.findAll();

		for (CcInfoEntity ccInfo : ccInfoArr) {

			JSONObject resultJson = new JSONObject();

			resultJson.put("ccName", ccInfo.getCcName());
			resultJson.put("ccPath", ccInfo.getCcPath());
			resultJson.put("ccLang", ccInfo.getCcLang());
			resultJson.put("ccDesc", ccInfo.getCcDesc());

			resultJsonArr.add(resultJson);
		}
		
		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success get chaincode info");
		resultDto.setResultData(resultJsonArr);

		return resultDto;
	}

	/**
	 * 체인코드 업로드 서비스
	 * 
	 * @param ccFile 체인코드 파일 이름
	 * @param ccName 체인코드 이름
	 * @param ccDesc 체인코드 설명
	 * @param ccLang 체인코드 언어
	 * 
	 * @return
	 */
	
	public ResultDto ccFileUpload(MultipartFile ccFile, String ccName, String ccDesc, String ccLang) {

		ResultDto resultDto = new ResultDto();

		try {


			InputStream inputStream = ccFile.getInputStream();
			File file = new File(System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/");

			if (!file.exists()) {
				try {
					
					file.mkdirs();
					
				} catch (Exception e) {
					
					resultDto.setResultCode("9999");
					resultDto.setResultFlag(false);
					resultDto.setResultMessage(e.getMessage());
					return resultDto;
					
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

			
			CcInfoDto ccInfoDto=new CcInfoDto();
			
			ccInfoDto.setCcName(ccName);
			ccInfoDto.setCcDesc(ccDesc);
			ccInfoDto.setCcLang(ccLang);
			ccInfoDto.setCcPath(
					System.getProperty("user.dir") + "/chaincode/src/" + ccName + "/" + ccFile.getOriginalFilename()
					);
			
			saveConInfo(ccInfoDto);
			
		} catch (Exception e) {
			
			resultDto.setResultCode("9999");
			resultDto.setResultFlag(false);
			resultDto.setResultMessage(e.getMessage());
			
			return resultDto;
			
		}

		resultDto.setResultCode("0000");
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success chaincode file upload");

		return resultDto;
	}
}
