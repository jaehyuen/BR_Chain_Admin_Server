package com.brchain.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.repository.CcInfoChannelRepository;
import com.brchain.core.chaincode.repository.CcInfoPeerRepository;
import com.brchain.core.chaincode.repository.CcInfoRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChaincodeDatabaseTest {

	@Autowired
	private CcInfoRepository        ccInfoRepository;
	@Autowired
	private CcInfoPeerRepository    ccInfoPeerRepository;
	@Autowired
	private CcInfoChannelRepository ccInfoChannelRepository;
	@Autowired
	private ConInfoRepository       conInfoRepository;

	@Test
	public void 체인코드_정보_저장_조회_테스트() throws Exception {

		System.out.println("************************ 체인코드_정보_저장_조회_테스트 시작************************");

		// given
		CcInfoEntity ccInfoEntity = createCcInfoEntity("test-chaincode");

		// when
		ccInfoEntity = ccInfoRepository.save(ccInfoEntity);
		ccInfoEntity = ccInfoRepository.findByCcName("test-chaincode");
		System.out.println("ccInfoEntity : " + ccInfoEntity);

		// then
		assertThat(ccInfoEntity.getCcName()).isEqualTo("test-chaincode");
		assertThat(ccInfoEntity.getCcPath()).isEqualTo("/src/test/chaincode/test.go");
		assertThat(ccInfoEntity.getCcLang()).isEqualTo("golang");
		assertThat(ccInfoEntity.getCcDesc()).isEqualTo("this is test chaincode");
		assertThat(ccInfoEntity.getCcVersion()).isEqualTo("1");

		System.out.println("************************ 체인코드_정보_저장_조회_테스트 종료 ************************");

	}

	@Test
	public void 체인코드_피어_정보_저장_조회_테스트() throws Exception {

		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 시작 ************************");

		// given
		CcInfoEntity ccInfoEntity = createCcInfoEntity("test-chaincode");
		ccInfoEntity = ccInfoRepository.save(ccInfoEntity);

		ConInfoEntity conInfoEntity1 = createConInfoEntity("peer0.orgtest.com", "1111");
		conInfoEntity1 = conInfoRepository.save(conInfoEntity1);

		ConInfoEntity conInfoEntity2 = createConInfoEntity("peer1.orgtest.com", "1112");
		conInfoEntity2 = conInfoRepository.save(conInfoEntity2);

		CcInfoPeerEntity ccInfoPeerEntity1 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity1);
		CcInfoPeerEntity ccInfoPeerEntity2 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity2);

		// when
		ccInfoPeerEntity1 = ccInfoPeerRepository.save(ccInfoPeerEntity1);
		ccInfoPeerEntity2 = ccInfoPeerRepository.save(ccInfoPeerEntity2);

		System.out.println("ccInfoPeerEntity1 : " + ccInfoPeerEntity1);
		System.out.println("ccInfoPeerEntity2 : " + ccInfoPeerEntity2);

		List<CcInfoPeerEntity> resultList = ccInfoPeerRepository.findByCcId(ccInfoEntity.getId());

//		// then
//		assertThat(ccInfoEntity.getCcName()).isEqualTo("test-chaincode");
//		assertThat(ccInfoEntity.getCcPath()).isEqualTo("/src/test/chaincode/test.go");
//		assertThat(ccInfoEntity.getCcLang()).isEqualTo("golang");
//		assertThat(ccInfoEntity.getCcDesc()).isEqualTo("this is test chaincode");
//		assertThat(ccInfoEntity.getCcVersion()).isEqualTo("1");

		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 종료 ************************");

	}

	private CcInfoEntity createCcInfoEntity(String param) {

		CcInfoEntity ccInfoEntity = new CcInfoEntity();

		// ccInfoEntity.setId(1L);
		ccInfoEntity.setCcName(param);
		ccInfoEntity.setCcPath("/src/test/chaincode/test.go");
		ccInfoEntity.setCcLang("golang");
		ccInfoEntity.setCcDesc("this is test chaincode");
		ccInfoEntity.setCcVersion("1");
		// ccInfoEntity.setCreatedAt(LocalDateTime.now());

		return ccInfoEntity;

	}

	private ConInfoEntity createConInfoEntity(String param1, String param2) {

		ConInfoEntity conInfoEntity = new ConInfoEntity();

		conInfoEntity.setConName(param1);
		conInfoEntity.setConId("testconid");
		conInfoEntity.setConType("peer");
		conInfoEntity.setConNum(0);
		conInfoEntity.setConCnt(1);
		conInfoEntity.setConPort(param2);
		conInfoEntity.setOrgName("test");
		conInfoEntity.setOrgType("peer");
		conInfoEntity.setCouchdbYn(true);
		conInfoEntity.setGossipBootAddr("gossip addr");

		return conInfoEntity;

	}

	private CcInfoPeerEntity createCcInfoPeerEntity(CcInfoEntity ccInfoEntity, ConInfoEntity conInfoEntity) {

		CcInfoPeerEntity ccInfoPeerEntity = new CcInfoPeerEntity();

		ccInfoPeerEntity.setCcVersion("1");
		ccInfoPeerEntity.setCcInfoEntity(ccInfoEntity);
		ccInfoPeerEntity.setConInfoEntity(conInfoEntity);

		return ccInfoPeerEntity;

	}

}
