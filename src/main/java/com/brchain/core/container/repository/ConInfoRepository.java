package com.brchain.core.container.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.custom.ConInfoCustomRepository;

public interface ConInfoRepository extends JpaRepository<ConInfoEntity, String>, ConInfoCustomRepository {

	Optional<ConInfoEntity> findByConId(String conId);

	List<ConInfoEntity> findByConTypeAndOrgType(String conType, String orgType);

	List<ConInfoEntity> findByConTypeAndOrgName(String conType, String orgName);

	List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);

	List<ConInfoEntity> findByConType(String conType);

	Optional<ConInfoEntity> findByConPort(String conPort);

	List<ConInfoEntity> findByOrgName(String orgName);



}
