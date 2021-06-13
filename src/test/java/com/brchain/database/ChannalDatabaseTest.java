package com.brchain.database;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.repository.CcInfoChannelRepository;
import com.brchain.core.chaincode.repository.CcInfoPeerRepository;
import com.brchain.core.chaincode.repository.CcInfoRepository;
import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.ChannelInfoPeerRepository;
import com.brchain.core.channel.repository.ChannelInfoRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;

@DataJpaTest(showSql = false)
//@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChannalDatabaseTest {

	@Autowired
	private ChannelInfoPeerRepository channelInfoPeerRepository;
	@Autowired
	private ChannelInfoRepository channelInfoRepository;

	@Autowired
	private CcInfoRepository ccInfoRepository;
	@Autowired
	private CcInfoPeerRepository ccInfoPeerRepository;
	@Autowired
	private CcInfoChannelRepository ccInfoChannelRepository;
	@Autowired
	private ConInfoRepository conInfoRepository;

	@BeforeEach
	public void setup() {
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

		ConInfoEntity conInfoEntity2 = createConInfoEntity("test", "1112", "peer", 1);
		conInfoEntity2 = conInfoRepository.save(conInfoEntity2);

		ConInfoEntity conInfoEntity3 = createConInfoEntity("test", "1113", "peer", 2);
		conInfoEntity3 = conInfoRepository.save(conInfoEntity3);

		ConInfoEntity conInfoEntity4 = createConInfoEntity("lalala", "1114", "peer", 1);
		conInfoEntity4 = conInfoRepository.save(conInfoEntity4);

		ConInfoEntity conInfoEntity5 = createConInfoEntity("lalala", "1115", "peer", 2);
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

		// 채널 등록
		ChannelInfoEntity channelInfoEntity1 = createChannelInfoEntity("test-channel");
		channelInfoEntity1 = channelInfoRepository.save(channelInfoEntity1);

		ChannelInfoEntity channelInfoEntity2 = createChannelInfoEntity("lalala-channel");
		channelInfoEntity2 = channelInfoRepository.save(channelInfoEntity2);

		ChannelInfoEntity channelInfoEntity3 = createChannelInfoEntity("haha-channel");
		channelInfoEntity3 = channelInfoRepository.save(channelInfoEntity3);

		// 채널 피어 등록
		ChannelInfoPeerEntity channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity1, conInfoEntity1);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity1, conInfoEntity2);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity1, conInfoEntity3);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity1, conInfoEntity4);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity1, conInfoEntity5);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);

		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity2, conInfoEntity1);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity2, conInfoEntity2);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity2, conInfoEntity3);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);

		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity3, conInfoEntity4);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);
		channelInfoPeerEntity = createChannelInfoPeerEntity(channelInfoEntity3, conInfoEntity5);
		channelInfoPeerEntity = channelInfoPeerRepository.save(channelInfoPeerEntity);

		// 체인코드 채널 등록
		CcInfoChannelEntity ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity1, ccInfoEntity1);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);
		ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity1, ccInfoEntity2);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);
		ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity1, ccInfoEntity3);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);

		ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity2, ccInfoEntity1);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);
		ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity2, ccInfoEntity2);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);

		ccInfoChannelEntity = createCcInfoChannelEntity(channelInfoEntity2, ccInfoEntity1);
		ccInfoChannelEntity = ccInfoChannelRepository.save(ccInfoChannelEntity);

	}

	//트렌젝션, 블록 데이터 추가 해야됨
	@Test
	public void 채널_요약_조회_테스트() throws Exception {

		System.out.println("************************ 채널_요약_조회_테스트 시작 ************************");

		// given

		// when
		List<ChannelSummaryDto> result = channelInfoRepository.findChannelSummary("202105", "202106");

		// then

		System.out.println(result);
//		assertThat(result.size()).isEqualTo(5);

		System.out.println("************************ 채널_요약_조회_테스트 종료 ************************");

	}

	private CcInfoEntity createCcInfoEntity(String param) {

		CcInfoEntity ccInfoEntity = new CcInfoEntity();

		ccInfoEntity.setCcName(param);
		ccInfoEntity.setCcPath("/src/test/chaincode/test.go");
		ccInfoEntity.setCcLang("golang");
		ccInfoEntity.setCcDesc("this is test chaincode");
		ccInfoEntity.setCcVersion("1");

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

		Random random = new Random();

		int txCnt = random.nextInt(1000);

		channelInfoEntity.setChannelName(channelName);
		channelInfoEntity.setOrderingOrg("testorderer");
		channelInfoEntity.setChannelTx(txCnt);
		channelInfoEntity.setChannelBlock(txCnt / 2);
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

	private CcInfoChannelEntity createCcInfoChannelEntity(ChannelInfoEntity channelInfoEntity,
			CcInfoEntity ccInfoEntity) {

		CcInfoChannelEntity ccInfoChannelEntity = new CcInfoChannelEntity();

		ccInfoChannelEntity.setCcInfoEntity(ccInfoEntity);
		ccInfoChannelEntity.setChannelInfoEntity(channelInfoEntity);
		ccInfoChannelEntity.setCcVersion("1");

		return ccInfoChannelEntity;

	}

}
