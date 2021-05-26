package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class ContainerSetting_back {

	@Value("${brchain.sourcedir}")
	private String sourceDir;

	@Value("${brchain.logdir}")
	private String logDir;

	@Value("${brchain.datadir}")
	private String dataDir;

	@Value("${brchain.networkmode}")
	private String networkMode;

	private String orgName;
	private String type;
	private String containerName;
	private String port;
	private int num;

	public void initSetting(String orgName, String type, String port, int num) {
		this.orgName = orgName;
		this.type = type;
		this.port = port;
		this.num = num;

		switch (type) {
		case "peer":
		case "couchdb":
		case "orderer":
			this.containerName = type + num + ".org" + orgName + ".com";
			break;
		case "setup_peer":
		case "setup_orderer":
		case "setup_channel":
		case "ca":
			this.containerName = type + ".org" + orgName + ".com";
			break;

		}
	}

	public List<String> setBinds() {
		List<String> binds = new ArrayList<>();

		switch (type) {
		case "peer":
			binds.add(sourceDir + "/crypto-config/peerOrganizations/org" + orgName + ".com/peers/" + containerName
					+ ":/opt/gopath/src/github.com/hyperledger/fabric/peer:rw");
			binds.add(logDir + "/container_logs/" + containerName + ":/log:rw");
			binds.add(sourceDir + "/scripts/container:/scripts:rw");
			binds.add(dataDir + "/production/" + containerName + ":/var/hyperledger/production:rw");
			binds.add("/var/run:/host/var/run:rw");
			break;
		case "ca":
			binds.add(logDir + "/container_logs/ca.org" + orgName + ".com:/log:rw");
			binds.add(dataDir + "/ca/ca.org" + orgName + ".com:/etc/hyperledger/fabric-ca:rw");
		case "setup_orderer":
		case "setup_channel":
		case "setup_peer":
			binds.add(sourceDir + "/crypto-config:/crypto-config:rw");
			binds.add(sourceDir + "/scripts/container:/scripts:rw");
			binds.add(sourceDir + "/channel-artifacts:/root/data:rw"); // setup
			break;
		case "couchdb":
			binds.add(dataDir + "/couchdb/couchdb" + num + ".org" + orgName + ".com:/opt/couchdb/data:rw");
//			binds.add(sourceDir+"/config/couchdb/local0.ini:/opt/couchdb/etc/local.ini:rw");
			binds.add(logDir + "/container_logs/" + type + num + ".org" + orgName + ".com:/opt/couchdb/log:rw");
			break;
		case "orderer":
			binds.add(sourceDir + "/scripts/container:/scripts:rw");
			binds.add(sourceDir + "/crypto-config/ordererOrganizations/org" + orgName + ".com/users/Admin@org" + orgName
					+ ".com/msp:/etc/hyperledger/orderer/admin/msp:rw");
			binds.add(sourceDir + "/channel-artifacts/" + orgName
					+ "/genesis.block:/etc/hyperledger/orderer/orderer.genesis.block:rw");
			binds.add(logDir + "/container_logs/" + containerName + ":/log:rw");
			binds.add(sourceDir + "/crypto-config/ordererOrganizations/org" + orgName + ".com/orderers/" + containerName
					+ ":/etc/hyperledger/orderer:rw");
			binds.add(dataDir + "/production/" + containerName + ":/var/hyperledger/production:rw");
			break;
		}

		return binds;
	}

//	public List<String> setExtraHosts() {
//		List<String> extraHosts = new ArrayList<>();
//		extraHosts.add("orderer0.orgorderer.com:192.168.65.167");
//		extraHosts.add("orderer1.orgorderer.com:192.168.65.167");
//		extraHosts.add("peer0.orgdoro.com:192.168.65.167");
//		extraHosts.add("peer0.orgminj.com:192.168.65.168");
//
//		return extraHosts;
//	}

	public List<String> setContainerEnv(String param, boolean couchdb) {
		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");

		if (type.equals("peer")) {

			containerEnv.add("CORE_VM_ENDPOINT=unix:///host/var/run/docker.sock");
			containerEnv.add("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE=" + networkMode);
			containerEnv
					.add("CORE_PEER_TLS_CERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/tls/server.crt");
			containerEnv
					.add("CORE_PEER_TLS_KEY_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/tls/server.key");
			containerEnv.add("CORE_PEER_ID=" + containerName);
			containerEnv.add("CORE_PEER_ADDRESS=" + containerName + ":" + port);
			containerEnv.add("CORE_PEER_LISTENADDRESS=0.0.0.0:" + port);
			containerEnv.add("CORE_PEER_GOSSIP_EXTERNALENDPOINT=" + containerName + ":" + port);
			containerEnv.add("CORE_PEER_GOSSIP_BOOTSTRAP=" + param);
			containerEnv.add("CORE_PEER_LOCALMSPID=" + orgName + "MSP");
			containerEnv.add(
					"CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/msp/cacerts/ca.org"
							+ orgName + ".com-cert.pem");
			containerEnv.add("CORE_PEER_TLS_ENABLED=true");
			containerEnv.add("CORE_PEER_GOSSIP_USELEADERELECTION=true");
			containerEnv.add("CORE_PEER_GOSSIP_ORGLEADER=false");
			containerEnv.add("CORE_PEER_PROFILE_ENABLED=true");
			containerEnv.add("CORE_CHAINCODE_LOGGING_LEVEL=DEBUG");
			containerEnv.add("CORE_PEER_CHAINCODELISTENADDRESS=0.0.0.0:7052");
			containerEnv.add("CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/msp");

			if (type.equals("peer") && couchdb) {
				containerEnv.add("CORE_LEDGER_STATE_STATEDATABASE=CouchDB");
				containerEnv.add("CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS=couchdb" + num + ".org" + orgName
						+ ".com:5984");
				containerEnv.add("CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME=admin");
				containerEnv.add("CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD=password");
			}

		} else if (type.equals("orderer")) {

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

		} else if (type.equals("couchdb")) {

			containerEnv.add("COUCHDB_USER=admin");
			containerEnv.add("COUCHDB_PASSWORD=password");
			containerEnv.add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
//			containerEnv.add("GOPATH=/opt/gopath");
//			containerEnv.add("GOROOT=/opt/go");
//			containerEnv.add("GOCACHE=off");
			containerEnv.add("GOSU_VERSION=1.11");
			containerEnv.add("TINI_VERSION=0.18.0");
			containerEnv.add("GPG_COUCH_KEY=8756C4F765C9AC3CB6B85D62379CE192D401AB61");
			containerEnv.add("COUCHDB_VERSION=3.1.1");

//			
//            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
//            "GOSU_VERSION=1.11",
//            "TINI_VERSION=0.18.0",
//            "GPG_COUCH_KEY=8756C4F765C9AC3CB6B85D62379CE192D401AB61",
//            "COUCHDB_VERSION=3.1.1"

		} else {

			containerEnv.add("ADMINCERTS=true");
			// containerEnv.add("PROD_USER=1001");
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
//				containerEnv.add("PEER_ORGS=" + orgName);
				containerEnv.add("SETUP_TYPE=channel");
				containerEnv.add("CHANNEL_NAME=" + orgName);
			}
		}

		return containerEnv;
	}

	public List<String> setCmd() {
		List<String> cmd = new ArrayList<>();
		if (!type.equals("couchdb")) {

			cmd.add("/bin/sh");
			cmd.add("-c");
		}

		switch (type) {
		case "peer":
			cmd.add("/scripts/start-peer.sh");
			break;
		case "setup_peer":
//			cmd.add("/scripts/setup-peer.sh; sleep 9999");
			cmd.add("/scripts/setup-fabric.sh; sleep 9999");
			break;
		case "setup_orderer":
//			cmd.add("/scripts/setup-orderer.sh; sleep 9999");
			cmd.add("/scripts/setup-fabric.sh; sleep 9999");
			break;
		case "setup_channel":
			cmd.add("/scripts/setup-fabric.sh; sleep 9999");
			break;
		case "ca":
			cmd.add("/scripts/start-root-ca.sh");
			break;
		case "couchdb":
			cmd.add("/opt/couchdb/bin/couchdb");
			break;
		case "orderer":
			cmd.add("/scripts/start-orderer0.sh");
			break;
		}
		return cmd;
	}

//	public Map<String, Map> setVolumes() {
//		Map<String, String> map = new HashMap<String, String>();
//		Map<String, Map> volumes = new HashMap<String, Map>();
//		volumes.put("/etc/hyperledger/fabric-ca", map);
//		volumes.put("/log", map);
//		volumes.put("/crypto-config", map);
//		volumes.put("/scripts", map);
//		volumes.put("/root/data", map);
//		switch (type) {
//		case "peer":
//			volumes.put("/host/var/run", map);
//			volumes.put("/log", map);
//			volumes.put("/opt/gopath/src/github.com/hyperledger/fabric/peer", map);
//			volumes.put("/scripts", map);
//			volumes.put("/var/hyperledger/production", map);
//			break;
//		case "setup_peer":
//		case "setup_orderer":
//		case "setup_channel":
//		case "ca":
//			volumes.put("/etc/hyperledger/fabric-ca", map);
//			volumes.put("/log", map);
//			volumes.put("/crypto-config", map);
//			volumes.put("/scripts", map);
//			volumes.put("/root/data", map);
//			break;
//		case "couchdb":
//			volumes.put("/opt/couchdb/data", map);
////			volumes.put("/opt/couchdb/etc/local.d", map);
////			volumes.put("/opt/couchdb/etc/local.ini", map);
//			volumes.put("/opt/couchdb/log", map);
//			break;
//		case "orderer":
//			volumes.put("/etc/hyperledger/orderer", map);
//			volumes.put("/etc/hyperledger/orderer/admin/msp", map);
//			volumes.put("/etc/hyperledger/orderer/orderer.genesis.block", map);
//			volumes.put("/log", map);
//			volumes.put("/scripts", map);
//			volumes.put("/var/hyperledger/production", map);
//
//			break;
//		}
//
//		return volumes;
//
//	}

	public Set<String> setExposedPort(String[] ports) {
		Set<String> exposedPorts = new HashSet<>();
		if (ports != null) {

			for (String port : ports) {
				exposedPorts.add(port);
			}
		}
		return exposedPorts;
	}

	public String setImages() {
		String imageName = "";

		switch (type) {
		case "peer":
			imageName = "hyperledger/fabric-peer:2.2.0";
			break;
		case "setup_peer":
		case "setup_orderer":
		case "setup_channel":
		case "ca":
			imageName = "hyperledger/fabric-ca:1.4.8";
			break;
		case "couchdb":
//			imageName = "hyperledger/fabric-couchdb:0.4.15";
			imageName = "couchdb:3.1.1";
			break;
		case "orderer":
			imageName = "hyperledger/fabric-orderer:2.2.0";
			break;
		}
		return imageName;
	}

//	public String setWorkingDir() {
//		String imageName = "";
//
//		switch (type) {
//		case "peer":
//			imageName = "/opt/gopath/src/github.com/hyperledger/fabric/peer";
//			break;
//		case "ca":
//			imageName = "/opt/gopath/src/github.com/hyperledger/fabric";
//			break;
//		case "couchdb":
//			imageName = "";
//			break;
//		case "orderer":
//			imageName = "hyperledger/fabric-orderer:1.4.3";
//			break;
//		}
//		return imageName;
//	}

}
