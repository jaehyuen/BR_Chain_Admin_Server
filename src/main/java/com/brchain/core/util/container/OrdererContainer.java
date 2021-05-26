package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class OrdererContainer extends Container {

	@Override
	public List<String> getBinds() {

		List<String> binds = new ArrayList<>();

		binds.add(sourceDir + "/scripts/container:/scripts:rw");
		binds.add(sourceDir + "/crypto-config/ordererOrganizations/org" + orgName + ".com/users/Admin@org" + orgName
				+ ".com/msp:/etc/hyperledger/orderer/admin/msp:rw");
		binds.add(sourceDir + "/channel-artifacts/" + orgName
				+ "/genesis.block:/etc/hyperledger/orderer/orderer.genesis.block:rw");
		binds.add(logDir + "/container_logs/" + containerName + ":/log:rw");
		binds.add(sourceDir + "/crypto-config/ordererOrganizations/org" + orgName + ".com/orderers/" + containerName
				+ ":/etc/hyperledger/orderer:rw");
		binds.add(dataDir + "/production/" + containerName + ":/var/hyperledger/production:rw");

		return binds;
	}

	@Override
	public List<String> getContainerEnv(String param, Boolean couchdbYn) {

		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("ORDERER_HOST=" + containerName);
		containerEnv.add("ORDERER_GENERAL_LISTENADDRESS=0.0.0.0");
		containerEnv.add("ORDERER_GENERAL_GENESISMETHOD=file");
		containerEnv.add("ORDERER_GENERAL_GENESISFILE=/etc/hyperledger/orderer/orderer.genesis.block");
		containerEnv.add("ORDERER_GENERAL_LOCALMSPID=" + orgName + "MSP");
		containerEnv.add("ORDERER_GENERAL_LOCALMSPDIR=/etc/hyperledger/orderer/msp");
		containerEnv.add("ORDERER_GENERAL_LISTENPORT=" + port);
		containerEnv.add("ORDERER_GENERAL_TLS_ENABLED=true");
		containerEnv.add("ORDERER_GENERAL_TLS_PRIVATEKEY=/etc/hyperledger/orderer/tls/server.key");
		containerEnv.add("ORDERER_GENERAL_TLS_CERTIFICATE=/etc/hyperledger/orderer/tls/server.crt");
		containerEnv.add("ORDERER_GENERAL_TLS_ROOTCAS=[/etc/hyperledger/orderer/msp/cacerts/ca.org" + orgName
				+ ".com-cert.pem]");
		containerEnv.add("ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE=/etc/hyperledger/orderer/tls/server.crt");
		containerEnv.add("ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY=/etc/hyperledger/orderer/tls/server.key");
		containerEnv.add("ORDERER_GENERAL_CLUSTER_ROOTCAS=[/etc/hyperledger/orderer/msp/cacerts/ca.org" + orgName
				+ ".com-cert.pem]");

		return containerEnv;
	}

	@Override
	public List<String> getCmd() {

		List<String> cmd = new ArrayList<>();

		cmd.add("/bin/sh");
		cmd.add("-c");
		cmd.add("/scripts/start-orderer0.sh");

		return cmd;
	}

	@Override
	public String getImages() {

		return "hyperledger/fabric-orderer:2.2.0";
	}

}
