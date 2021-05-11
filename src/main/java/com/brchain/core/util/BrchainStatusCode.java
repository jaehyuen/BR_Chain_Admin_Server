package com.brchain.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BrchainStatusCode {

	GET_WALLET(0, "/api/brcoin/wallet?walletId="), // 지갑 조회
	POST_WALLET(1, "/api/brcoin/wallet"), // 지갑 조회
	POST_TOKEN(2, "/api/brcoin/token"), // 토큰 생성
	POST_TOKEN_TRANSFER(2, "/api/brcoin/tranfer"); // 토큰 생성

	private final int    index;
	private final String value;
}
