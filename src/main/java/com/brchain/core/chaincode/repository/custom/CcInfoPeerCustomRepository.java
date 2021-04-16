package com.brchain.core.chaincode.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;

public interface CcInfoPeerCustomRepository {

	List<CcInfoPeerEntity> findCcInfoPeerToActive(@Param("channelName") String channelName);
	
	List<CcSummaryDto> findChaincodeSummary();


}
