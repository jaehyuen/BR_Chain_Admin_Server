package com.brchain.core.container.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.container.dto.OrgInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoCustomRepository {

	List<ConInfoEntity> findMemberByOrgName(String orgName);

	List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);

	boolean portCheck(String conPort);
	
	List<String> findOrgsByChannelName(String channelName);
	
	List<OrgInfoDto> findOrgInfo(String orgType);
	
	}
