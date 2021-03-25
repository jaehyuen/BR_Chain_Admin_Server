package com.brchain.core.chaincode.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity, Long> {

	ArrayList<CcInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);
		
	@Query("select a,b,c from CcInfoPeerEntity a left join fetch a.conInfoEntity b left join fetch a.ccInfoEntity c where c.id=:id ")
	List<CcInfoPeerEntity> findByCcId(Long id);

}
