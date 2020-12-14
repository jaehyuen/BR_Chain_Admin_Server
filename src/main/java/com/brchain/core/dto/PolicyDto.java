package com.brchain.core.dto;

import java.util.ArrayList;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PolicyDto {
	
	private int policyType;  						                      //정책 타입 1 == Signature 3 == ImplicitMeta
	private ArrayList<String> identityMsps;  						      //Signature 정책일때 identities(조직) 리스트
	private String rule;     						                      //ImplicitMeta 정책일때 rule (MAJORITY, ANY, ALL), Signature 정책일때는 숫자 (and면 숫자 or면 1)
	private String subPolicy;  						                      //ImplicitMeta 정책일때 sub_policy(Writers, Readers, Admins) 	Signature 정책일때는 role 설정용 (Writers, Readers, Admins)
	

}
