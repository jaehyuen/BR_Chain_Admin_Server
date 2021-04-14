package com.brchain.core.container.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoCustomRepository {

	List<ConInfoEntity> findMemberByOrgName(@Param("orgName") String orgName);


}
