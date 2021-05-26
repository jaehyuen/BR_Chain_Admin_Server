package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SetupContainer extends Container {

	@Override
	public void initSetting(String orgName, String type, String port, int num) {

		this.type          = type;
		this.orgName       = orgName;
		this.port          = port;
		this.num           = num;
		this.containerName = type + ".org" + orgName + ".com";
	}

	@Override
	public List<String> getBinds() {

		List<String> binds = new ArrayList<>();

		binds.add(sourceDir + "/crypto-config:/crypto-config:rw");
		binds.add(sourceDir + "/scripts/container:/scripts:rw");
		binds.add(sourceDir + "/channel-artifacts:/root/data:rw");

		return binds;
	}

	@Override
	public List<String> getContainerEnv(String param, Boolean couchdbYn) {

		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");
		containerEnv.add("ADMINCERTS=true");
		containerEnv.add("CA_SERVER_PORT_" + orgName + "=" + port);
		containerEnv.add("ORDERER_HOME=/etc/hyperledger/orderer");
		containerEnv.add("PEER_HOME=/opt/gopath/src/github.com/hyperledger/fabric/peer");
		containerEnv.add("FABRIC_CA_SERVER_HOME=/etc/hyperledger/fabric-ca");
		containerEnv.add("FABRIC_CA_SERVER_CSR_CN=ca.org" + orgName + ".com");
		containerEnv.add("FABRIC_CA_SERVER_CSR_HOSTS=ca.org" + orgName + ".com");
		containerEnv.add("FABRIC_CA_SERVER_DEBUG=true");
		containerEnv.add("FABRIC_CA_SERVER_CA_NAME=ca-" + orgName);
		containerEnv.add("FABRIC_CA_SERVER_TLS_ENABLED=false");
		containerEnv.add("FABRIC_CA_SERVER_PORT=" + port);
		containerEnv.add("FABRIC_CA_SERVER_SIGNING_DEFAULT_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_SERVER_SIGNING_PROFILES_TLS_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_SERVER_CSR_CA_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_HOME=/etc/hyperledger/fabric-ca-server");
		containerEnv.add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

		if (type.equals("setup_peer")) {

			containerEnv.add("PEER_ORGS=" + orgName);
			containerEnv.add("NUM_PEERS=" + num);
			containerEnv.add("SETUP_TYPE=peer");

		} else if (type.equals("setup_orderer")) {

			containerEnv.add("ORDERER_ORGS=" + orgName);
			containerEnv.add("NUM_ORDERERS=" + num);
			containerEnv.add("SETUP_TYPE=orderer");

			for (int i = 0; i < num; i++) {
				String[] ordererPort = param.split(" ");
				containerEnv.add("ORDERER_PORT_" + orgName + i + "=" + ordererPort[i]);

			}

		} else if (type == "setup_channel") {
			containerEnv.add("SETUP_TYPE=channel");
			containerEnv.add("CHANNEL_NAME=" + orgName);
		}

		return containerEnv;
	}

	@Override
	public List<String> getCmd() {

		List<String> cmd = new ArrayList<>();

		cmd.add("/bin/sh");
		cmd.add("-c");
		cmd.add("/scripts/setup-fabric.sh; sleep 9999");

		return cmd;
	}

	@Override
	public String getImages() {

		return "hyperledger/fabric-ca:1.4.8";
	}

}
