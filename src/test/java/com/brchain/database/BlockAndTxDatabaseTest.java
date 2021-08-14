package com.brchain.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.ChannelInfoPeerRepository;
import com.brchain.core.channel.repository.ChannelInfoRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.repository.ConInfoRepository;
import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.entity.TransactionEntity;
import com.brchain.core.fabric.repository.BlockRepository;
import com.brchain.core.fabric.repository.TransactionRepository;

@DataJpaTest(showSql = false)
//@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BlockAndTxDatabaseTest {

	@Autowired
	private ChannelInfoRepository channelInfoRepository;

	@Autowired
	private BlockRepository blockRepository;
	@Autowired
	private TransactionRepository transactionRepository;

	@BeforeEach
	public void setup() throws InterruptedException {


		// 채널 등록
		ChannelInfoEntity channelInfoEntity1 = createChannelInfoEntity("test-channel");
		channelInfoEntity1 = channelInfoRepository.save(channelInfoEntity1);

		ChannelInfoEntity channelInfoEntity2 = createChannelInfoEntity("lalala-channel");
		channelInfoEntity2 = channelInfoRepository.save(channelInfoEntity2);

		ChannelInfoEntity channelInfoEntity3 = createChannelInfoEntity("haha-channel");
		channelInfoEntity3 = channelInfoRepository.save(channelInfoEntity3);

		
		// 블록, 트랜잭션 등록
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			createBlockAndTx(channelInfoEntity1, i + 1);
		}

		for (int i = 0; i < 25; i++) {
			createBlockAndTx(channelInfoEntity2, i + 1);
		}
		for (int i = 0; i < 2; i++) {
			createBlockAndTx(channelInfoEntity3, i + 1);
		}

	}


	@Test
	public void 블록_조회_테스트() throws Exception {

		System.out.println("************************ 블록_조회_테스트 시작 ************************");

		// given

		// when
		List<BlockAndTxDto> result = blockRepository.findByChannelName("test-channel");

		// then

		System.out.println(result);
		assertThat(result.size()).isEqualTo(10);

		System.out.println("************************ 블록_조회_테스트 종료 ************************");

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



	private void createBlockAndTx(ChannelInfoEntity channelInfoEntity, int blockNum) throws InterruptedException {
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
		Thread.sleep(10);

	}

}