package com.brchain.core.repository.chaincode;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.chaincode.CcInfoEntity;

public interface CcInfoRepository extends JpaRepository<CcInfoEntity, String> {

	CcInfoEntity findByCcName(String ccName);

}
