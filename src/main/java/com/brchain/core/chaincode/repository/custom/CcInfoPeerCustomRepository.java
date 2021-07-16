package com.brchain.core.chaincode.repository.custom;

import java.util.List;

import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;

public interface CcInfoPeerCustomRepository {

	List<CcInfoPeerEntity> findCcInfoPeerToActive(String channelName);
	
	List<CcSummaryDto> findChaincodeSummary();
	
	List<CcInfoPeerEntity> findByCcId(Long id);
	
	List<CcInfoPeerEntity> findByConName(String conName);


}
