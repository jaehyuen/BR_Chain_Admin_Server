package com.brchain.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BrchainStatusCode {
	
	/**
	 * 코드 정의법
	 * 
	 * 첫번째 자리 
	 * 0 = 성공
	 * 8 = 오류(http 4xx)
	 * 9 = 오류(http 5xx)
	 * 
	 * 두번째, 세번쨰 자리 오류 대분류
	 * 
	 * 01 = 도커 에러
	 * 02 = 월랫 에러
	 * 03 = 채널 에러
	 * 04 = 패브릭 에러
	 * 05 = 채널 설정 에러
	 * 06 = 체인코드 에러
	 * 07 = 파일 관련 에러
	 * 08 = jwt 에러
	 * 09 = 서버 에러
	 * 
	 * 
	 * 네번째 자리 오류 소분류(순서)
	 * 
	 */

	SUCCESS("0","success"),
	
	DOCKER_CONNECTION_ERROR("9010","docker_connection_error"),
	WALLET_CREATE_ERROR("9020","wallet_create_error"),
	
	CHANNEL_CREATE_ERROR("9030","channel_create_error"),
	CHANNEL_JOIN_ERROR("9031","channel_join_error"),
	
	FABRIC_CONTEXT_ERROR("9040","fabric_context_error"),
	FABRIC_CLIENT_ERROR("9041","fabric_client_error"),
	FABRIC_QUERY_ERROR("9042","fabric_query_error"),
	
	GET_CHANNEL_CONFIG_ERROR("9050","get_channel_config_error"),
	UPDATE_CHANNEL_CONFIG_ERROR("9051","update_channel_config_error"),
	
	CHAINCODE_INSTALL_ERROR("9060","chaincode_install_error"),
	CHAINCDOE_PACKAGE_ERROR("9061","chaincode_package_error"),
	CHAINCODE_UPLOAD_ERROR("9062","chaincode_upload_error"),
	CHAINCODE_ACTIVE_ERROR("9063","chaincode_active_error"),
	
	FILE_UPLOAD_ERROR("9070","file_upload_error"),
	FILE_DOWNLOAN_ERROR("9071","file_download_error"),
	DELETE_DIR_ERROR("9072","delete_dir_error"),
	EXEC_COMMAND_ERROR("9073","exec_command_error"),
	FILE_IO_ERROR("9074","file_io_error"),
	
	JWT_ERROR("9080","jwt_error"),
	
	THREAD_ERROR("9090","thread_error"),
	
	
	
	ALREADY_AHCHOR_PEER_ERROR("8010","already_anchor_peer_error"),
	ALREADY_REGISTERED_LISTENER_ERROR("8020","already_registered_listener_error"),
	
	LOGIN_ERROR("8000","login_error"),
	INVALID_REFRESH_TOKEN("8030","invalid_refresh_token"),
	INVALID_JWT("8031","invalid_jwt"),
	INVALID_PASSOWRD("8032","invalid_password"),
	ACCOUNT_NOT_FOUND("8033","account_not_found")
	
	
	;

	private final String code;
	private final String message;
}
