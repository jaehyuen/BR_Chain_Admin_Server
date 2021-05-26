package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PeerContainer extends Container {

	@Override
	public List<String> getBinds() {

		List<String> binds = new ArrayList<>();

		binds.add(sourceDir + "/crypto-config/peerOrganizations/org" + orgName + ".com/peers/" + containerName
				+ ":/opt/gopath/src/github.com/hyperledger/fabric/peer:rw");
		binds.add(logDir + "/container_logs/" + containerName + ":/log:rw");
		binds.add(sourceDir + "/scripts/container:/scripts:rw");
		binds.add(dataDir + "/production/" + containerName + ":/var/hyperledger/production:rw");
		binds.add("/var/run:/host/var/run:rw");

		return binds;
	}

	@Override
	public List<String> getContainerEnv(String param, Boolean couchdbYn) {

		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");
		containerEnv.add("CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock");
		containerEnv.add("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE=" + networkMode);
		containerEnv.add("CORE_PEER_TLS_CERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/tls/server.crt");
		containerEnv.add("CORE_PEER_TLS_KEY_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/tls/server.key");
		containerEnv.add("CORE_PEER_ID=" + containerName);
		containerEnv.add("CORE_PEER_ADDRESS=" + containerName + ":" + port);
		containerEnv.add("CORE_PEER_LISTENADDRESS=0.0.0.0:" + port);
		containerEnv.add("CORE_PEER_GOSSIP_EXTERNALENDPOINT=" + containerName + ":" + port);
		containerEnv.add("CORE_PEER_GOSSIP_BOOTSTRAP=" + param);
		containerEnv.add("CORE_PEER_LOCALMSPID=" + orgName + "MSP");
		containerEnv
			.add("CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/msp/cacerts/ca.org"
					+ orgName + ".com-cert.pem");
		containerEnv.add("CORE_PEER_TLS_ENABLED=true");
		containerEnv.add("CORE_PEER_GOSSIP_USELEADERELECTION=true");
		containerEnv.add("CORE_PEER_GOSSIP_ORGLEADER=false");
		containerEnv.add("CORE_PEER_PROFILE_ENABLED=true");
		containerEnv.add("CORE_CHAINCODE_LOGGING_LEVEL=DEBUG");
		containerEnv.add("CORE_PEER_CHAINCODELISTENADDRESS=0.0.0.0:7052");
		containerEnv.add("CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/msp");

		if (couchdbYn) {
			containerEnv.add("CORE_LEDGER_STATE_STATEDATABASE=CouchDB");
			containerEnv
				.add("CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS=couchdb" + num + ".org" + orgName + ".com:5984");
			containerEnv.add("CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME=admin");
			containerEnv.add("CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD=password");
		}

		return containerEnv;
	}

	@Override
	public List<String> getCmd() {

		List<String> cmd = new ArrayList<>();

		cmd.add("/bin/sh");
		cmd.add("-c");
		cmd.add("/scripts/start-peer.sh");

		return cmd;
	}

	@Override
	public String getImages() {

		return "hyperledger/fabric-peer:2.2.0";
	}

}
