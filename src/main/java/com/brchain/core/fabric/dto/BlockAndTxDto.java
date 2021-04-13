package com.brchain.core.fabric.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockAndTxDto {

	private String blockDataHash; // 블록 데이터 해쉬
	private int    blockNum;      // 블록 번호
	private int    txCount;       // 트랜잭션 개수
	private Date   timestamp;     // 타임스탬프
	private String prevDataHash;  // 이전블록 데이터 해쉬
	private String txList;        // 트랜잭션 개수

}
