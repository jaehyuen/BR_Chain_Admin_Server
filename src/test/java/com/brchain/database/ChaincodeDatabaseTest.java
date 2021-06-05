package com.brchain.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
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
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.ChannelInfoPeerRepository;
import com.brchain.core.channel.repository.ChannelInfoRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChaincodeDatabaseTest {

	@Autowired
	private CcInfoRepository ccInfoRepository;
	@Autowired
	private CcInfoPeerRepository ccInfoPeerRepository;
	@Autowired
	private CcInfoChannelRepository ccInfoChannelRepository;
	@Autowired
	private ConInfoRepository conInfoRepository;

	@Autowired
	private ChannelInfoPeerRepository channelInfoPeerRepository;
	@Autowired
	private ChannelInfoRepository channelInfoRepository;

	@BeforeEach
	public void init() {
		// 체인코드 등록
		CcInfoEntity ccInfoEntity1 = createCcInfoEntity("test-chaincode");
		ccInfoEntity1 = ccInfoRepository.save(ccInfoEntity1);
		
		CcInfoEntity ccInfoEntity2 = createCcInfoEntity("abc-chaincode");
		ccInfoEntity2 = ccInfoRepository.save(ccInfoEntity2);
		
		CcInfoEntity ccInfoEntity3 = createCcInfoEntity("zzzcc");
		ccInfoEntity3 = ccInfoRepository.save(ccInfoEntity3);

		// 피어 등록, 체인코드
		ConInfoEntity conInfoEntity1 = createConInfoEntity("test", "1111", "peer", 0);
		conInfoEntity1 = conInfoRepository.save(conInfoEntity1);

		ConInfoEntity conInfoEntity2 = createConInfoEntity("test", "1111", "peer", 1);
		conInfoEntity2 = conInfoRepository.save(conInfoEntity2);

		ConInfoEntity conInfoEntity3 = createConInfoEntity("test", "1111", "peer", 2);
		conInfoEntity3 = conInfoRepository.save(conInfoEntity3);

		ConInfoEntity conInfoEntity4 = createConInfoEntity("lalala", "1111", "peer", 1);
		conInfoEntity4 = conInfoRepository.save(conInfoEntity4);

		ConInfoEntity conInfoEntity5 = createConInfoEntity("lalala", "1111", "peer", 2);
		conInfoEntity5 = conInfoRepository.save(conInfoEntity5);

		// 체인코드 피어 등록
		CcInfoPeerEntity ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity1, conInfoEntity1);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity1, conInfoEntity2);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity1, conInfoEntity3);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity1, conInfoEntity4);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity1, conInfoEntity5);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity2, conInfoEntity1);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity2, conInfoEntity2);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity2, conInfoEntity3);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity3, conInfoEntity1);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		ccInfoPeerEntity = createCcInfoPeerEntity(ccInfoEntity3, conInfoEntity2);
		ccInfoPeerEntity = ccInfoPeerRepository.save(ccInfoPeerEntity);
		
		

	}

	@Test
	public void 저장_톄스트() throws Exception {
		List<CcInfoEntity> test = ccInfoRepository.findAll();
		System.out.println(test);

		List<ConInfoEntity> test2 = conInfoRepository.findAll();
		System.out.println(test2);
		
		List<CcInfoPeerEntity> test3 = ccInfoPeerRepository.findAll();
		System.out.println(test3);
	}

	@Test
	public void 체인코드_정보_저장_조회_테스트() throws Exception {

		System.out.println("************************ 체인코드_정보_저장_조회_테스트 시작************************");

		// given
		// CcInfoEntity ccInfoEntity = createCcInfoEntity("test-chaincode");

		// when
		// ccInfoEntity = ccInfoRepository.save(ccInfoEntity);
		CcInfoEntity ccInfoEntity = ccInfoRepository.findByCcName("test-chaincode");
		System.out.println("ccInfoEntity : " + ccInfoEntity);

		// then
		assertThat(ccInfoEntity.getCcName()).isEqualTo("test-chaincode");
		assertThat(ccInfoEntity.getCcPath()).isEqualTo("/src/test/chaincode/test.go");
		assertThat(ccInfoEntity.getCcLang()).isEqualTo("golang");
		assertThat(ccInfoEntity.getCcDesc()).isEqualTo("this is test chaincode");
		assertThat(ccInfoEntity.getCcVersion()).isEqualTo("1");

		System.out.println("************************ 체인코드_정보_저장_조회_테스트 종료 ************************");

	}

//	@Test
//	public void 체인코드_피어_정보_저장_조회_테스트() throws Exception {
//
//		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 시작 ************************");
//
//		// given
////		CcInfoEntity ccInfoEntity = createCcInfoEntity("test-chaincode");
////		ccInfoEntity = ccInfoRepository.save(ccInfoEntity);
////
////		ConInfoEntity conInfoEntity1 = createConInfoEntity("peer0.orgtest.com", "1111");
////		conInfoEntity1 = conInfoRepository.save(conInfoEntity1);
////
////		ConInfoEntity conInfoEntity2 = createConInfoEntity("peer1.orgtest.com", "1112");
////		conInfoEntity2 = conInfoRepository.save(conInfoEntity2);
////
////		
////		
////		CcInfoPeerEntity ccInfoPeerEntity1 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity1);
////		CcInfoPeerEntity ccInfoPeerEntity2 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity2);
////		
////		
////
////		// when
////		ccInfoPeerEntity1 = ccInfoPeerRepository.save(ccInfoPeerEntity1);
////		ccInfoPeerEntity2 = ccInfoPeerRepository.save(ccInfoPeerEntity2);
////
////		System.out.println("ccInfoPeerEntity1 : " + ccInfoPeerEntity1);
////		System.out.println("ccInfoPeerEntity2 : " + ccInfoPeerEntity2);
//
//		List<CcInfoPeerEntity> resultList = ccInfoPeerRepository.findByCcId(1l);
//
////		// then
////		assertThat(ccInfoEntity.getCcName()).isEqualTo("test-chaincode");
////		assertThat(ccInfoEntity.getCcPath()).isEqualTo("/src/test/chaincode/test.go");
////		assertThat(ccInfoEntity.getCcLang()).isEqualTo("golang");
////		assertThat(ccInfoEntity.getCcDesc()).isEqualTo("this is test chaincode");
////		assertThat(ccInfoEntity.getCcVersion()).isEqualTo("1");
//
//		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 종료 ************************");
//
//	}
//
//	@Test
//	public void 체인코드_피어_조회_테스트() throws Exception {
//
//		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 시작 ************************");
//
//		// given
//		CcInfoEntity ccInfoEntity = createCcInfoEntity("test-chaincode");
//		ccInfoEntity = ccInfoRepository.save(ccInfoEntity);
//
//		ConInfoEntity conInfoEntity1 = createConInfoEntity("peer0.orgtest.com", "1111");
//		conInfoEntity1 = conInfoRepository.save(conInfoEntity1);
//
//		ConInfoEntity conInfoEntity2 = createConInfoEntity("peer1.orgtest.com", "1112");
//		conInfoEntity2 = conInfoRepository.save(conInfoEntity2);
//
//		ChannelInfoEntity channelInfoEntity = createChannelInfoEntity("test-channel");
//		channelInfoEntity = channelInfoRepository.save(channelInfoEntity);
//
//		CcInfoPeerEntity ccInfoPeerEntity1 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity1);
//		CcInfoPeerEntity ccInfoPeerEntity2 = createCcInfoPeerEntity(ccInfoEntity, conInfoEntity2);
//
//		// when
//		ccInfoPeerEntity1 = ccInfoPeerRepository.save(ccInfoPeerEntity1);
//		ccInfoPeerEntity2 = ccInfoPeerRepository.save(ccInfoPeerEntity2);
//
//		System.out.println("ccInfoPeerEntity1 : " + ccInfoPeerEntity1);
//		System.out.println("ccInfoPeerEntity2 : " + ccInfoPeerEntity2);
//
//		List<CcInfoPeerEntity> resultList = ccInfoPeerRepository.findByCcId(ccInfoEntity.getId());
//
////		// then
//		assertThat(ccInfoEntity.getCcName()).isEqualTo("test-chaincode");
//		assertThat(ccInfoEntity.getCcPath()).isEqualTo("/src/test/chaincode/test.go");
//		assertThat(ccInfoEntity.getCcLang()).isEqualTo("golang");
//		assertThat(ccInfoEntity.getCcDesc()).isEqualTo("this is test chaincode");
//		assertThat(ccInfoEntity.getCcVersion()).isEqualTo("1");
//
//		System.out.println("************************ 체인코드_피어_정보_저장_조회_테스트 종료 ************************");
//
//	}

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

	private ConInfoEntity createConInfoEntity(String orgName, String port, String orgType, int conNum) {

		ConInfoEntity conInfoEntity = new ConInfoEntity();

		conInfoEntity.setConName(orgType + conNum + ".org" + orgName + ".com");
		conInfoEntity.setConId("testconid" + Math.random() + Math.random());
		conInfoEntity.setConType(orgType);
		conInfoEntity.setConNum(conNum);
		conInfoEntity.setConCnt(1);
		conInfoEntity.setConPort(port);
		conInfoEntity.setOrgName(orgName);
		conInfoEntity.setOrgType(orgType);
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

	private ChannelInfoEntity createChannelInfoEntity(String channelName) {

		ChannelInfoEntity channelInfoEntity = new ChannelInfoEntity();

		channelInfoEntity.setChannelName(channelName);
		channelInfoEntity.setOrderingOrg("testorderer");
		channelInfoEntity.setChannelTx(0);
		channelInfoEntity.setChannelBlock(0);
		channelInfoEntity.setAppAdminPolicyType("ImplicitMeta");
		channelInfoEntity.setAppAdminPolicyValue("ANY Admins");
		channelInfoEntity.setChannelAdminPolicyType("ImplicitMeta");
		channelInfoEntity.setChannelAdminPolicyValue("ANY Admins");
		channelInfoEntity.setOrdererAdminPolicyType("ImplicitMeta");
		channelInfoEntity.setOrdererAdminPolicyValue("ANY Admins");
		channelInfoEntity.setBatchTimeout("1s");
		channelInfoEntity.setBatchSizeAbsolMax(81920);
		channelInfoEntity.setBatchSizeMaxMsg(20);
		channelInfoEntity.setBatchSizePreferMax(20480);

		return channelInfoEntity;

	}

	private ChannelInfoPeerEntity createChannelInfoPeerEntity(ChannelInfoEntity channelInfoEntity,
			ConInfoEntity conInfoEntity) {

		ChannelInfoPeerEntity channelInfoPeerEntity = new ChannelInfoPeerEntity();

		channelInfoPeerEntity.setChannelInfoEntity(channelInfoEntity);
		channelInfoPeerEntity.setConInfoEntity(conInfoEntity);
		channelInfoPeerEntity.setAnchorYn(false);
		// channelInfoPeerEntity.set

		return channelInfoPeerEntity;

	}

}
