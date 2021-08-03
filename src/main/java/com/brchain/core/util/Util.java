package com.brchain.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.BrchainException;
import com.brchain.core.chaincode.dto.CcInfoChannelDto;
import com.brchain.core.chaincode.dto.CcInfoDto;
import com.brchain.core.chaincode.dto.CcInfoPeerDto;
import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.channel.dto.ChannelHandleDto;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.channel.dto.ChannelInfoPeerDto;
import com.brchain.core.channel.entitiy.ChannelHandleEntity;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.container.dto.ConInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.fabric.dto.BlockDto;
import com.brchain.core.fabric.dto.FabricNodeDto;
import com.brchain.core.fabric.dto.PolicyDto;
import com.brchain.core.fabric.dto.TransactionDto;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.entity.TransactionEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.PortBinding;

@Component
public class Util {
	
	private Logger               logger     = LoggerFactory.getLogger(this.getClass());


	/**
	 * 파일 base64 인코딩 함수
	 * 
	 * @param filePath 파일 경로
	 * 
	 * @return base64로 인코딩된 파일 내용
	 * 
	 */
	
	public String fileEncodeBases64(String filePath) {

		File   file = new File(filePath);
		byte[] data = new byte[(int) file.length()];

		try (FileInputStream stream = new FileInputStream(file)) {
			stream.read(data, 0, data.length);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		String base64data = Base64.getEncoder().encodeToString(data);

		return base64data;

	}

	/**
	 * 결과 DTO 생성 함수
	 * 
	 * @param <T>     data 필드 변수형
	 * @param code    결과 코드
	 * @param flag    결과 플래그
	 * @param message 결과 메시지
	 * @param data    결과 데이터
	 * 
	 * @return 생성한 결과 DTO
	 * 
	 */
	
	public <T> ResultDto<T> setResult(String code, boolean flag, String message, T data) {

		ResultDto<T> resultDto = new ResultDto<T>();

		resultDto.setResultCode(code);
		resultDto.setResultFlag(flag);
		resultDto.setResultMessage(message);
		resultDto.setResultData(data);

		return resultDto;

	}

	/**
	 * 결과 DTO 생성 함수
	 * 
	 * @param <T>     data 필드 변수형
	 * @param status  결과 상태
	 * @param data    결과 데이터
	 * 
	 * @return 생성한 결과 DTO
	 * 
	 */
	
	public <T> ResultDto<T> setResult(BrchainStatusCode status, T data) {

		ResultDto<T> resultDto = new ResultDto<T>();

		resultDto.setResultCode(status.getCode());
		resultDto.setResultFlag(status.getCode().equals("0000") ? true : false);
		resultDto.setResultMessage(status.getMessage());
		resultDto.setResultData(data);

		return resultDto;

	}
//	
//	public <T> ResultDto<T> setResult(BrchainStatusCode status, Stream<T> stream) {
//
//		ResultDto<T> resultDto = new ResultDto<T>();
//
//		resultDto.setResultCode(status.getCode());
//		resultDto.setResultFlag(status.getCode().equals("0000") ? true : false);
//		resultDto.setResultMessage(status.getMessage());
//		resultDto.setResultData(stream.map(data -> toDto(data)).collect(Collectors.toList()));
////		stream.map(a-> util.to)
//
//		return resultDto;
//
//	}



	/**
	 * 압축 해제 함수
	 * 
	 * @param zipPath      압축 파일 경로
	 * @param zipFileName  압축 파일 이름
	 * @param zipUnzipPath 압축을 해제할 경로
	 * 
	 * @return
	 * 
	 */
	
	public boolean unZip(String zipPath, String zipFileName, String zipUnzipPath) {
		
		boolean  isChk    = false;
		File     zipFile  = new File(zipPath + zipFileName);

		ZipEntry zipentry = null;
		try (FileInputStream fis = new FileInputStream(zipFile); ZipInputStream zis = new ZipInputStream(fis, Charset.forName("EUC-KR"))) {
			if (createFolder(zipUnzipPath)) {

			}

			while ((zipentry = zis.getNextEntry()) != null) {
				
				String filename = zipentry.getName();
				File   file     = new File(zipUnzipPath, filename);
				
				if (zipentry.isDirectory()) {

					file.mkdirs();
				} else {

					createFile(file, zis);

				}
			}
			
			isChk = true;
		} catch (Exception e) {
			isChk = false;
		}
		return isChk;
	}
	

	/**
	 * 폴더 생성 함수
	 * 
	 * @param folderPath 생성할 폴더 경로
	 * 
	 * @return 폴더 생성 여부
	 * 
	 */
	
	public boolean createFolder(String folderPath) {
		if (folderPath.length() < 0) {
			return false;
		}
		File folder = new File(folderPath);

		if (!folder.exists()) {
			folder.mkdir();
		}

		return true;
	}

	/**
	 * 파일 생성 함수
	 * 
	 * @param file 파일
	 * @param zis  압축 inputstream
	 * 
	 */
	private void createFile(File file, ZipInputStream zis) {
		File parentDir = new File(file.getParent());
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		
		try(FileOutputStream fos = new FileOutputStream(file)) {
			

			byte[] buffer = new byte[256];
			int    size   = 0;

			while ((size = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, size);
			}
		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_IO_ERROR);
		}

	}

	/**
	 * 커맨드 실행 함수
	 * 
	 * @param cmd 실행할 커맨드
	 * 
	 */
	
	public void execute(String cmd) {
		Process      process       = null;
		Runtime      runtime       = Runtime.getRuntime();
		StringBuffer resultOutput  = new StringBuffer();     // 결과 스트링 버퍼
		String       msg           = null;                   // 메시지
		List<String> cmdList       = new ArrayList<String>();

		// 운영체제 구분 (window, window 가 아니면 무조건 linux 로 판단)
		if (System.getProperty("os.name")
			.indexOf("Windows") > -1) {
			cmdList.add("cmd");
			cmdList.add("/c");
		} else {
			cmdList.add("/bin/bash");
			cmdList.add("-c");
		}
		// 명령어 셋팅
		cmdList.add(cmd);
		String[] array = cmdList.toArray(new String[cmdList.size()]);
		logger.debug("[커멘드 실행]" + cmd);

		// 명령어 실행
		try {
			process = runtime.exec(array);
		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_IO_ERROR);
		}

		try (BufferedReader resultBufferReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "EUC-KR")); // 성공 버퍼
//				BufferedReader errorBufferReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "EUC-KR"))
						) {

			// shell 실행이 정상 동작했을 경우

			while ((msg = resultBufferReader.readLine()) != null) {
				resultOutput.append(msg + System.getProperty("line.separator"));
			}

//			// shell 실행시 에러가 발생했을 경우
//			while ((msg = errorBufferReader.readLine()) != null) {
//				errorOutput.append(msg + System.getProperty("line.separator"));
//			}

			// 프로세스의 수행이 끝날때까지 대기
			process.waitFor();
			
			logger.debug("[커멘드 실행 결과]" + resultBufferReader.toString());

//			// shell 실행이 정상 종료되었을 경우
//			if (process.exitValue() == 0) {
//				System.out.println("성공");
//				System.out.println(successOutput.toString());
//			} else {
//				// shell 실행이 비정상 종료되었을 경우
//				System.out.println("비정상 종료");
//				System.out.println(successOutput.toString());
//				System.out.println(errorOutput.toString());
//			}

		} catch (IOException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_IO_ERROR);
		} catch (InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.THREAD_ERROR);
		}
	}

	/*
	 * ########################################################################
	 * 
	 * TEST to Entity
	 * 
	 * ########################################################################
	 */

	public ConInfoEntity toEntity(ConInfoDto conInfoDto) {

		return ConInfoEntity.builder()
			.conId(conInfoDto.getConId())
			.conName(conInfoDto.getConName())
			.conType(conInfoDto.getConType())
			.conNum(conInfoDto.getConNum())
			.conCnt(conInfoDto.getConCnt())
			.conPort(conInfoDto.getConPort())
			.orgName(conInfoDto.getOrgName())
			.orgType(conInfoDto.getOrgType())
			.consoOrgs(conInfoDto.getConsoOrgs())
			.couchdbYn(conInfoDto.isCouchdbYn())
			.gossipBootAddr(conInfoDto.getGossipBootAddr())
			.ordererPorts(conInfoDto.getOrdererPorts())
			.createdAt(conInfoDto.getCreatedAt())
			.build();
	}

	public CcInfoEntity toEntity(CcInfoDto ccInfoDto) {

		return CcInfoEntity.builder()
			.id(ccInfoDto.getId())
			.ccName(ccInfoDto.getCcName())
			.ccPath(ccInfoDto.getCcPath())
			.ccLang(ccInfoDto.getCcLang())
			.ccDesc(ccInfoDto.getCcDesc())
			.ccVersion(ccInfoDto.getCcVersion())
			.createdAt(ccInfoDto.getCreatedAt())
			.build();
	}

	public CcInfoPeerEntity toEntity(CcInfoPeerDto ccInfoPeerDto) {

		return CcInfoPeerEntity.builder()
			.id(ccInfoPeerDto.getId())
			.ccVersion(ccInfoPeerDto.getCcVersion())
			.conInfoEntity(toEntity(ccInfoPeerDto.getConInfoDto()))
			.ccInfoEntity(toEntity(ccInfoPeerDto.getCcInfoDto()))
			.createdAt(ccInfoPeerDto.getCreatedAt())
			.build();
	}

	public CcInfoChannelEntity toEntity(CcInfoChannelDto ccInfoChannelDto) {

		return CcInfoChannelEntity.builder()
			.id(ccInfoChannelDto.getId())
			.ccVersion(ccInfoChannelDto.getCcVersion())
			.channelInfoEntity(toEntity(ccInfoChannelDto.getChannelInfoDto()))
			.ccInfoEntity(toEntity(ccInfoChannelDto.getCcInfoDto()))
			.createdAt(ccInfoChannelDto.getCreatedAt())
			.build();

	}

	public ChannelInfoEntity toEntity(ChannelInfoDto channelInfoDto) {

		return ChannelInfoEntity.builder()
			.channelName(channelInfoDto.getChannelName())
			.channelBlock(channelInfoDto.getChannelBlock())
			.channelTx(channelInfoDto.getChannelTx())
			.orderingOrg(channelInfoDto.getOrderingOrg())
			.appAdminPolicyType(channelInfoDto.getAppAdminPolicyType())
			.appAdminPolicyValue(channelInfoDto.getAppAdminPolicyValue())
			.ordererAdminPolicyType(channelInfoDto.getOrdererAdminPolicyType())
			.ordererAdminPolicyValue(channelInfoDto.getOrdererAdminPolicyValue())
			.channelAdminPolicyType(channelInfoDto.getChannelAdminPolicyType())
			.channelAdminPolicyValue(channelInfoDto.getChannelAdminPolicyValue())
			.batchTimeout(channelInfoDto.getBatchTimeout())
			.batchSizeAbsolMax(channelInfoDto.getBatchSizeAbsolMax())
			.batchSizeMaxMsg(channelInfoDto.getBatchSizeMaxMsg())
			.batchSizePreferMax(channelInfoDto.getBatchSizePreferMax())
			.createdAt(channelInfoDto.getCreatedAt())
			.build();
	}

	public ChannelInfoPeerEntity toEntity(ChannelInfoPeerDto channelInfoPeerDto) {

		return ChannelInfoPeerEntity.builder()
			.id(channelInfoPeerDto.getId())
			.anchorYn(channelInfoPeerDto.isAnchorYn())
			.conInfoEntity(toEntity(channelInfoPeerDto.getConInfoDto()))
			.channelInfoEntity(toEntity(channelInfoPeerDto.getChannelInfoDto()))
			.createdAt(channelInfoPeerDto.getCreatedAt())
			.build();
	}

	public ChannelHandleEntity toEntity(ChannelHandleDto channelHandleDto) {

		return ChannelHandleEntity.builder()
			.handle(channelHandleDto.getHandle())
			.channelName(channelHandleDto.getChannelName())
			.createdAt(channelHandleDto.getCreatedAt())
			.build();
	}

	public TransactionEntity toEntity(TransactionDto transactionDto) {

		return TransactionEntity.builder()
			.id(transactionDto.getId())
			.txId(transactionDto.getTxId())
			.creatorId(transactionDto.getCreatorId())
			.txType(transactionDto.getTxType())
			.timestamp(transactionDto.getTimestamp())
			.ccName(transactionDto.getCcName())
			.ccVersion(transactionDto.getCcVersion())
			.ccArgs(transactionDto.getCcArgs())
			.blockEntity(toEntity(transactionDto.getBlockDto()))
			.channelInfoEntity(toEntity(transactionDto.getChannelInfoDto()))
			.createdAt(transactionDto.getCreatedAt())
			.build();
	}

	public BlockEntity toEntity(BlockDto blockDto) {

		return BlockEntity.builder()
			.blockDataHash(blockDto.getBlockDataHash())
			.blockNum(blockDto.getBlockNum())
			.txCount(blockDto.getTxCount())
			.timestamp(blockDto.getTimestamp())
			.prevDataHash(blockDto.getPrevDataHash())
			.channelInfoEntity(toEntity(blockDto.getChannelInfoDto()))
			.createdAt(blockDto.getCreatedAt())
			.build();

	}

	/*
	 * ########################################################################
	 * 
	 * TEST to Dto
	 * 
	 * ########################################################################
	 */

	public ConInfoDto toDto(ConInfoEntity conInfoEntity) {
		return ConInfoDto.builder()
			.conName(conInfoEntity.getConName())
			.conId(conInfoEntity.getConId())
			.conCnt(conInfoEntity.getConCnt())
			.conType(conInfoEntity.getConType())
			.conNum(conInfoEntity.getConNum())
			.conPort(conInfoEntity.getConPort())
			.orgName(conInfoEntity.getOrgName())
			.orgType(conInfoEntity.getOrgType())
			.consoOrgs(conInfoEntity.getConsoOrgs())
			.couchdbYn(conInfoEntity.isCouchdbYn())
			.gossipBootAddr(conInfoEntity.getGossipBootAddr())
			.ordererPorts(conInfoEntity.getOrdererPorts())
			.createdAt(conInfoEntity.getCreatedAt())
			.build();

	}

	public CcInfoDto toDto(CcInfoEntity ccInfoEntity) {
		return CcInfoDto.builder()
			.id(ccInfoEntity.getId())
			.ccName(ccInfoEntity.getCcName())
			.ccPath(ccInfoEntity.getCcPath())
			.ccLang(ccInfoEntity.getCcLang())
			.ccDesc(ccInfoEntity.getCcDesc())
			.ccVersion(ccInfoEntity.getCcVersion())
			.createdAt(ccInfoEntity.getCreatedAt())
			.build();
	}

	public CcInfoPeerDto toDto(CcInfoPeerEntity ccInfoPeerEntity) {
		return CcInfoPeerDto.builder()
			.id(ccInfoPeerEntity.getId())
			.ccVersion(ccInfoPeerEntity.getCcVersion())
			.conInfoDto(toDto(ccInfoPeerEntity.getConInfoEntity()))
			.ccInfoDto(toDto(ccInfoPeerEntity.getCcInfoEntity()))
			.createdAt(ccInfoPeerEntity.getCreatedAt())
			.build();
	}

	public CcInfoChannelDto toDto(CcInfoChannelEntity ccInfoChannelEntity) {
		return CcInfoChannelDto.builder()
			.id(ccInfoChannelEntity.getId())
			.ccVersion(ccInfoChannelEntity.getCcVersion())
			.channelInfoDto(toDto(ccInfoChannelEntity.getChannelInfoEntity()))
			.ccInfoDto(toDto(ccInfoChannelEntity.getCcInfoEntity()))
			.createdAt(ccInfoChannelEntity.getCreatedAt())
			.build();
	}

	public ChannelInfoDto toDto(ChannelInfoEntity channelInfoEntity) {

		return ChannelInfoDto.builder()
			.channelName(channelInfoEntity.getChannelName())
			.channelBlock(channelInfoEntity.getChannelBlock())
			.channelTx(channelInfoEntity.getChannelTx())
			.orderingOrg(channelInfoEntity.getOrderingOrg())
			.appAdminPolicyType(channelInfoEntity.getAppAdminPolicyType())
			.appAdminPolicyValue(channelInfoEntity.getAppAdminPolicyValue())
			.ordererAdminPolicyType(channelInfoEntity.getOrdererAdminPolicyType())
			.ordererAdminPolicyValue(channelInfoEntity.getOrdererAdminPolicyValue())
			.channelAdminPolicyType(channelInfoEntity.getChannelAdminPolicyType())
			.channelAdminPolicyValue(channelInfoEntity.getChannelAdminPolicyValue())
			.batchTimeout(channelInfoEntity.getBatchTimeout())
			.batchSizeAbsolMax(channelInfoEntity.getBatchSizeAbsolMax())
			.batchSizeMaxMsg(channelInfoEntity.getBatchSizeMaxMsg())
			.batchSizePreferMax(channelInfoEntity.getBatchSizePreferMax())
			.createdAt(channelInfoEntity.getCreatedAt())
			.build();
	}

	public ChannelInfoPeerDto toDto(ChannelInfoPeerEntity channelInfoPeerEntity) {
		return ChannelInfoPeerDto.builder()
			.id(channelInfoPeerEntity.getId())
			.anchorYn(channelInfoPeerEntity.isAnchorYn())
			.conInfoDto(toDto(channelInfoPeerEntity.getConInfoEntity()))
			.channelInfoDto(toDto(channelInfoPeerEntity.getChannelInfoEntity()))
			.createdAt(channelInfoPeerEntity.getCreatedAt())
			.build();
	}

	public ChannelHandleDto toDto(ChannelHandleEntity channelHandleEntity) {
		return ChannelHandleDto.builder()
			.handle(channelHandleEntity.getHandle())
			.channelName(channelHandleEntity.getChannelName())
			.createdAt(channelHandleEntity.getCreatedAt())
			.build();

	}

	public TransactionDto toDto(TransactionEntity transactionEntity) {
		return TransactionDto.builder()
			.id(transactionEntity.getId())
			.txId(transactionEntity.getTxId())
			.creatorId(transactionEntity.getCreatorId())
			.txType(transactionEntity.getTxType())
			.timestamp(transactionEntity.getTimestamp())
			.ccName(transactionEntity.getCcName())
			.ccVersion(transactionEntity.getCcVersion())
			.ccArgs(transactionEntity.getCcArgs())
			.blockDto(toDto(transactionEntity.getBlockEntity()))
			.channelInfoDto(toDto(transactionEntity.getChannelInfoEntity()))
			.createdAt(transactionEntity.getCreatedAt())
			.build();
	}

	public BlockDto toDto(BlockEntity blockEntity) {
		return BlockDto.builder()
			.blockDataHash(blockEntity.getBlockDataHash())
			.blockNum(blockEntity.getBlockNum())
			.txCount(blockEntity.getTxCount())
			.timestamp(blockEntity.getTimestamp())
			.prevDataHash(blockEntity.getPrevDataHash())
			.channelInfoDto(toDto(blockEntity.getChannelInfoEntity()))
			.createdAt(blockEntity.getCreatedAt())
			.build();

	}

	/*
	 * ########################################################################
	 * 
	 * TEST
	 * 
	 * ########################################################################
	 */

//	public Object configtxRequest(String url, String channelName, byte[] param1, byte[] param2) {
//		Object result = "";
//	
//		try {
//
//			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//			factory.setConnectTimeout(5000); // 타임아웃 설정 5초
//			factory.setReadTimeout(5000);// 타임아웃 설정 5초
//			RestTemplate restTemplate = new RestTemplate(factory);
//			HttpHeaders header = new HttpHeaders();
//			ResponseEntity<String> resultMap;
//
//			if (url.contains("compute")) {
//				MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//				header.setContentType(MediaType.MULTIPART_FORM_DATA);
//				map.add("original", param1.toString());
//				map.add("updated", param2.toString());
//				map.add("channel", channelName);
//				System.out.println(param1.toString());
//				System.out.println(param2.toString());
//
//				HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
//						header);
//
//				resultMap = restTemplate.postForEntity(url, request, String.class);
//
//			} else {
//				HttpEntity<?> entity = new HttpEntity<>(header);
//
////				String url = "http://192.168.65.169:7059/protolator/decode/common.Config";
//
//				resultMap = restTemplate.postForEntity(url, param1, String.class);
//			}
//
//			result = resultMap.getBody();
//
//		} catch (HttpClientErrorException | HttpServerErrorException e) {
//
//			System.out.println(e.toString());
//
//		} catch (Exception e) {
//
//			System.out.println(e.toString());
//		}
//		return result;
//	}
//
//	public byte[] computeRequest(String url, String channelName, byte[] param1, byte[] param2) {
//		byte[] result = null;
//		
//		File test1 = new File(System.getProperty("user.dir") + "/config.pb");
//		File test2 = new File(System.getProperty("user.dir") + "/zz.pb");
//		FileSystemResource filetest1 = new FileSystemResource(test1);
//		FileSystemResource filetest2 = new FileSystemResource(test2);
//		ByteArrayResource bytetest1 = new ByteArrayResource(param1);
//		ByteArrayResource bytetest2 = new ByteArrayResource(param2);
////		ByteArrayResource
//
//		try {
//
//			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//			factory.setConnectTimeout(5000); // 타임아웃 설정 5초
//			factory.setReadTimeout(5000);// 타임아웃 설정 5초
//			RestTemplate restTemplate = new RestTemplate(factory);
//			HttpHeaders header = new HttpHeaders();
//			ResponseEntity<byte[]> resultMap = null;
//
//			if (url.contains("compute")) {
//				MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
//				header.setContentType(MediaType.MULTIPART_FORM_DATA);
//				map.add("channel", channelName);
//				map.add("original", filetest1);
//				map.add("updated", filetest2);
//				
//
//				resultMap = restTemplate.postForEntity(url, map, byte[].class);
//
//			} else {
//				HttpEntity<?> entity = new HttpEntity<>(header);
//
////				String url = "http://192.168.65.169:7059/protolator/decode/common.Config";
//
//				resultMap = restTemplate.postForEntity(url, param1, byte[].class);
//			}
//
//			result = resultMap.getBody();
//
//		} catch (HttpClientErrorException | HttpServerErrorException e) {
//
//			System.out.println(e.toString());
//
//		} catch (Exception e) {
//
//			System.out.println(e.toString());
//		}
//		return result;
//	}

}
