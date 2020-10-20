package com.brchain.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoEntity;

public interface CcInfoRepository extends JpaRepository<CcInfoEntity, String> {

	CcInfoEntity findByCcName(String ccName);

}
