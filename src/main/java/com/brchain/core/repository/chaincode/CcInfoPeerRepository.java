package com.brchain.core.repository.chaincode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity, Long> {

	ArrayList<CcInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);
	
//	@Query(value = "SELECT * FROM CCINFO_PEER WHERE CCINFO_ID=:id", nativeQuery = true)
	
	@Query("select a,b,c from CcInfoPeerEntity a left join fetch a.conInfoEntity b left join fetch a.ccInfoEntity c where c.id=:id ")
	List<CcInfoPeerEntity> findByCcId(Long id);

}
