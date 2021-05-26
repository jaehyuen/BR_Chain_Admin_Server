package com.brchain.service;

import static org.assertj.core.api.Assertions.assertThat;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brchain.core.util.ContainerSetting;
import com.brchain.core.util.container.CaContainer;
import com.brchain.core.util.container.CouchContainer;
import com.brchain.core.util.container.OrdererContainer;
import com.brchain.core.util.container.PeerContainer;
import com.brchain.core.util.container.SetupContainer;

@ExtendWith(MockitoExtension.class)
@Transactional
class ContainerSettingTest {

	@InjectMocks
	private CaContainer      caContainer;

	@InjectMocks
	private PeerContainer    peerContainer;
	@InjectMocks
	private CouchContainer   couchContainer;
	@InjectMocks
	private OrdererContainer ordererContainer;
	@InjectMocks
	private SetupContainer   setupContainer;

	@InjectMocks
	private ContainerSetting containerSetting;

	@Test
	public void CA_컨테이너_세팅_테스트() throws Exception {

		System.out.println("************************ CA_컨테이너_세팅_테스트 시작 ************************");

		caContainer.initSetting("test", "ca", "1111", 0);
		containerSetting.initSetting("test", "ca", "1111", 2);

		assertThat(caContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(caContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(caContainer.getExposedPort(new String[] { caContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(caContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(caContainer.getContainerEnv("7050", false))
			.isEqualTo(containerSetting.setContainerEnv("7050", false));
		assertThat(caContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		System.out.println("************************ CA_컨테이너_세팅_테스트 종료 ************************");

	}

	@Test
	public void PEER_컨테이너_세팅_테스트() throws Exception {

		System.out.println("************************ PEER_컨테이너_세팅_테스트 시작 ************************");

		peerContainer.initSetting("test", "peer", "1112", 1);
		containerSetting.initSetting("test", "peer", "1112", 1);

		assertThat(peerContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(peerContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(peerContainer.getExposedPort(new String[] { peerContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(peerContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(peerContainer.getContainerEnv("gossip test", false))
			.isEqualTo(containerSetting.setContainerEnv("gossip test", false));
		assertThat(peerContainer.getContainerEnv("gossip test", true))
			.isEqualTo(containerSetting.setContainerEnv("gossip test", true));
		assertThat(peerContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		System.out.println("************************ PEER_컨테이너_세팅_테스트 종료 ************************");

	}

	@Test
	public void ORDERER_컨테이너_세팅_테스트() throws Exception {

		System.out.println("************************ ORDERER_컨테이너_세팅_테스트 시작 ************************");

		ordererContainer.initSetting("test", "orderer", "1113", 1);
		containerSetting.initSetting("test", "orderer", "1113", 1);

		assertThat(ordererContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(ordererContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(ordererContainer.getExposedPort(new String[] { ordererContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(ordererContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(ordererContainer.getContainerEnv(null, null))
			.isEqualTo(containerSetting.setContainerEnv("gossip test", false));
		assertThat(ordererContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		System.out.println("************************ ORDERER_컨테이너_세팅_테스트 종료 ************************");

	}

	@Test
	public void 카우치_컨테이너_세팅_테스트() throws Exception {

		System.out.println("************************ 카우치_컨테이너_세팅_테스트 시작 ************************");

		couchContainer.initSetting("test", "couchdb", null, 1);
		containerSetting.initSetting("test", "couchdb", null, 1);

		assertThat(couchContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(couchContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(couchContainer.getExposedPort(new String[] { couchContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(couchContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(couchContainer.getContainerEnv(null, null)).isEqualTo(containerSetting.setContainerEnv(null, false));
		assertThat(couchContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		System.out.println("************************ 카우치_컨테이너_세팅_테스트 종료 ************************");

	}

	@Test
	public void SETUP_컨테이너_세팅_테스트() throws Exception {

		System.out.println("************************ SETUP_컨테이너_세팅_테스트 시작 ************************");

		setupContainer.initSetting("test", "setup_peer", null, 2);
		containerSetting.initSetting("test", "setup_peer", null, 2);

		assertThat(setupContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(setupContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(setupContainer.getExposedPort(new String[] { setupContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(setupContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(setupContainer.getContainerEnv(null, null)).isEqualTo(containerSetting.setContainerEnv(null, false));
		assertThat(setupContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		setupContainer.initSetting("test", "setup_orderer", "7050 8050", 2);
		containerSetting.initSetting("test", "setup_orderer", "7050 8050", 2);

		assertThat(setupContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(setupContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(setupContainer.getExposedPort(new String[] { setupContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(setupContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(setupContainer.getContainerEnv("7050 8050", null)).isEqualTo(containerSetting.setContainerEnv("7050 8050", false));
		assertThat(setupContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		setupContainer.initSetting("test", "setup_channel", null, 0);
		containerSetting.initSetting("test", "setup_channel", null, 0);

		assertThat(setupContainer.getContainerName()).isEqualTo(containerSetting.getContainerName());
		assertThat(setupContainer.getBinds()).isEqualTo(containerSetting.setBinds());
		assertThat(setupContainer.getExposedPort(new String[] { setupContainer.getPort() }))
			.isEqualTo(containerSetting.setExposedPort(new String[] { containerSetting.getPort() }));
		assertThat(setupContainer.getPort()).isEqualTo(containerSetting.getPort());
		assertThat(setupContainer.getContainerEnv(null, null)).isEqualTo(containerSetting.setContainerEnv(null, false));
		assertThat(setupContainer.getCmd()).isEqualTo(containerSetting.setCmd());

		System.out.println("************************ SETUP_컨테이너_세팅_테스트 종료 ************************");

	}

}
