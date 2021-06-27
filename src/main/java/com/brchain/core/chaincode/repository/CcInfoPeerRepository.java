package com.brchain.core.chaincode.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.repository.custom.CcInfoPeerCustomRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity, Long>, CcInfoPeerCustomRepository {

	/**
	 * TODO 수정 예정
	 */
	List<CcInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

}
