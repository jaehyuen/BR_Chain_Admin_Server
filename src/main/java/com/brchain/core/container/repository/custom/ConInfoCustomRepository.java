package com.brchain.core.container.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoCustomRepository {

	List<ConInfoEntity> findMemberByOrgName(@Param("orgName") String orgName);

	List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);

	boolean portCheck(@Param("conPort") String conPort);
	
	List<String> findOrgsByChannelName(@Param("channelName") String channelName);
	
	}
