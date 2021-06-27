package com.brchain.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.query.criteria.internal.path.SetAttributeJoin.TreatedSetAttributeJoin;
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
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.entity.TransactionEntity;
import com.brchain.core.fabric.repository.BlockRepository;
import com.brchain.core.fabric.repository.TransactionRepository;

@DataJpaTest(showSql = false)
//@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChannalDatabaseTest {

	@Autowired
	private ChannelInfoPeerRepository channelInfoPeerRepository;
	@Autowired
	private ChannelInfoRepository channelInfoRepository;

	@Autowired
	private ConInfoRepository conInfoRepository;

	@Autowired
	private BlockRepository blockRepository;
	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	public void setup() {

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

		// 블록, 트랜잭션 등록
		for (int i = 0; i < 10; i++) {
			createBlockAndTx(channelInfoEntity1, i + 1);
		}

		for (int i = 0; i < 25; i++) {
			createBlockAndTx(channelInfoEntity2, i + 1);
		}
		for (int i = 0; i < 2; i++) {
			createBlockAndTx(channelInfoEntity3, i + 1);
		}

	}

	// 트렌젝션, 블록 데이터 추가 해야됨
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

	@Test
	public void 채널정보_피어_조회_테스트1() throws Exception {

		System.out.println("************************ 채널정보_피어_조회_테스트1 시작 ************************");

		// given

		// when
		List<ChannelInfoPeerEntity> result = channelInfoPeerRepository.findByChannelNameOrConName("test-channel", null);

		// then

		System.out.println(result);
		assertThat(result.size()).isEqualTo(5);

		System.out.println("************************ 채널정보_피어_조회_테스트1 종료 ************************");

	}

	@Test
	public void 채널정보_피어_조회_테스트2() throws Exception {

		System.out.println("************************ 채널정보_피어_조회_테스트2 시작 ************************");

		// given

		// when
		List<ChannelInfoPeerEntity> result = channelInfoPeerRepository.findByChannelNameOrConName(null,
				"peer0.orgtest.com");

		// then

		System.out.println(result);
		assertThat(result.size()).isEqualTo(2);

		System.out.println("************************ 채널정보_피어_조회_테스트2 종료 ************************");

	}

	@Test
	public void 채널정보_피어_조회_테스트3() throws Exception {

		System.out.println("************************ 채널정보_피어_조회_테스트3 시작 ************************");

		// given

		// when
		List<ChannelInfoPeerEntity> result = channelInfoPeerRepository.findByChannelNameOrConName("test-channel",
				"peer0.orgtest.com");

		// then

		System.out.println(result);
		assertThat(result.size()).isEqualTo(1);
		System.out.println("************************ 채널정보_피어_조회_테스트3 종료 ************************");
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

	private void createBlockAndTx(ChannelInfoEntity channelInfoEntity, int blockNum) {
		Random random = new Random();
		int txCount = random.nextInt(10);

		channelInfoEntity.setChannelTx(channelInfoEntity.getChannelTx() + txCount);
		channelInfoEntity.setChannelBlock(channelInfoEntity.getChannelBlock() + 1);

		channelInfoEntity = channelInfoRepository.save(channelInfoEntity);

		BlockEntity blockEntity = new BlockEntity();

		blockEntity.setBlockDataHash(channelInfoEntity.getChannelName() + blockNum);
		blockEntity.setBlockNum(blockNum);
		blockEntity.setTxCount(txCount);
		blockEntity.setTimestamp(new Date());
		blockEntity.setPrevDataHash("test prev data hash");
		blockEntity.setChannelInfoEntity(channelInfoEntity);

		blockEntity = blockRepository.save(blockEntity);

		for (int i = 0; i < txCount; i++) {
			TransactionEntity transactionEntity = new TransactionEntity();

			transactionEntity.setTxId(channelInfoEntity.getChannelName() + i);
			transactionEntity.setCreatorId("testMSP");
			transactionEntity.setTxType("test tx type");
			transactionEntity.setTimestamp(new Date());
			transactionEntity.setCcName("zz test cc");
			transactionEntity.setCcVersion("1");
			transactionEntity.setCcArgs("test cc args");
			transactionEntity.setBlockEntity(blockEntity);
			transactionEntity.setChannelInfoEntity(channelInfoEntity);

			transactionRepository.save(transactionEntity);
		}
		System.out.println();

	}

}
