package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CaContainer extends Container {

	@Override
	public void initSetting(String orgName, String port, int num) {

		this.orgName       = orgName;
		this.type          = "ca";
		this.port          = port;
		this.containerName = type + ".org" + orgName + ".com";
	}

	@Override
	public List<String> setBinds() {

		List<String> binds = new ArrayList<>();

		binds.add(logDir + "/container_logs/ca.org" + orgName + ".com:/log:rw");
		binds.add(dataDir + "/ca/ca.org" + orgName + ".com:/etc/hyperledger/fabric-ca:rw");
		binds.add(sourceDir + "/crypto-config:/crypto-config:rw");
		binds.add(sourceDir + "/scripts/container:/scripts:rw");
		binds.add(sourceDir + "/channel-artifacts:/root/data:rw"); // setup

		return binds;
	}

	@Override
	public List<String> setContainerEnv(String param, boolean couchdb) {

		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");
		containerEnv.add("ADMINCERTS=true");
		containerEnv.add("CA_SERVER_PORT_" + orgName + "=" + port);
		containerEnv.add("ORDERER_HOME=/etc/hyperledger/orderer");
		containerEnv.add("PEER_HOME=/opt/gopath/src/github.com/hyperledger/fabric/peer");
		containerEnv.add("FABRIC_CA_SERVER_HOME=/etc/hyperledger/fabric-ca");
		containerEnv.add("FABRIC_CA_SERVER_CSR_CN=" + containerName);
		containerEnv.add("FABRIC_CA_SERVER_CSR_HOSTS=" + containerName);
		containerEnv.add("FABRIC_CA_SERVER_DEBUG=true");
		containerEnv.add("FABRIC_CA_SERVER_CA_NAME=ca-" + orgName);
		containerEnv.add("FABRIC_CA_SERVER_TLS_ENABLED=false");
		containerEnv.add("FABRIC_CA_SERVER_PORT=" + port);
		containerEnv.add("FABRIC_CA_SERVER_SIGNING_DEFAULT_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_SERVER_SIGNING_PROFILES_TLS_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_SERVER_CSR_CA_EXPIRY=876600h");
		containerEnv.add("FABRIC_CA_HOME=/etc/hyperledger/fabric-ca-server");
		containerEnv.add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");

		return containerEnv;
	}

	@Override
	public List<String> setCmd() {

		List<String> cmd = new ArrayList<>();

		cmd.add("/bin/sh");
		cmd.add("-c");
		cmd.add("/scripts/start-root-ca.sh");

		return cmd;
	}

	@Override
	public String setImages() {

		return "hyperledger/fabric-ca:1.4.8";
	}

}
