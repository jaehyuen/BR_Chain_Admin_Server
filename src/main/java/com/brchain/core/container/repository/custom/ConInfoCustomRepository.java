package com.brchain.core.container.repository.custom;

import java.util.List;
import java.util.Optional;

import com.brchain.core.container.dto.OrgInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoCustomRepository {

	List<ConInfoEntity> findMemberByOrgName(String orgName);

	List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);
	
	Optional<ConInfoEntity> findCaInfoByOrgName(String orgName);

	boolean portCheck(String conPort);

	List<String> findOrgsByChannelName(String channelName);

	List<OrgInfoDto> findOrgInfo(String orgType);
	
	void testDelete(String conId);

//	String findAllOrgs();

}
