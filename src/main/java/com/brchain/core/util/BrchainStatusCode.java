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
	 * 9 = 오류
	 * 
	 * 두번째, 세번쨰 자리 오류가 생긴?
	 * 
	 * 네번째 자리 오류 번호
	 */

	GET_WALLET(0), // 지갑 조회
	POST_WALLET(1), // 지갑 조회
	POST_TOKEN(2), // 토큰 생성
	POST_TOKEN_TRANSFER(3); // 토큰 생성

	private final int    index;
}
