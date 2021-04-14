package com.brchain.core.container.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoCustomRepository {

	List<ConInfoEntity> findMemberByOrgName(@Param("orgName") String orgName);

	List<ConInfoEntity> findByTest(@Param("conType") String conType, @Param("orgType") String orgType, @Param("orgName") String orgName);

}
