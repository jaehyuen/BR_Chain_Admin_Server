package com.brchain.core.chaincode.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.chaincode.entitiy.CcInfoEntity;

public interface CcInfoRepository extends JpaRepository<CcInfoEntity, Long> {

//	CcInfoEntity findByCcName(String ccName);

}
