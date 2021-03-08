package com.brchain.core.dto.chaincode;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UploadCcDto {

	private String ccName;
	private String ccDesc;
	private String ccLang;
	private String ccVersion;
	private MultipartFile ccFile;

}
