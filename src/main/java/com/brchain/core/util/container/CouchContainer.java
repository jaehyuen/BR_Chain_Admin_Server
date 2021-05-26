package com.brchain.core.util.container;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CouchContainer extends Container {

	@Override
	public List<String> getBinds() {

		List<String> binds = new ArrayList<>();

		binds.add(dataDir + "/couchdb/couchdb" + num + ".org" + orgName + ".com:/opt/couchdb/data:rw");
		binds.add(logDir + "/container_logs/" + type + num + ".org" + orgName + ".com:/opt/couchdb/log:rw");

		return binds;
	}

	@Override
	public List<String> getContainerEnv(String param, Boolean couchdbYn) {

		List<String> containerEnv = new ArrayList<>();

		containerEnv.add("TZ=Asia/Seoul");
		containerEnv.add("FABRIC_LOGGING_SPEC=INFO");
		containerEnv.add("FABRIC_CFG_PATH=/etc/hyperledger/fabric");
		containerEnv.add("COUCHDB_USER=admin");
		containerEnv.add("COUCHDB_PASSWORD=password");
		containerEnv.add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
		containerEnv.add("GOSU_VERSION=1.11");
		containerEnv.add("TINI_VERSION=0.18.0");
		containerEnv.add("GPG_COUCH_KEY=8756C4F765C9AC3CB6B85D62379CE192D401AB61");
		containerEnv.add("COUCHDB_VERSION=3.1.1");

		return containerEnv;
	}

	@Override
	public List<String> getCmd() {

		List<String> cmd = new ArrayList<>();

		cmd.add("/opt/couchdb/bin/couchdb");

		return cmd;
	}

	@Override
	public String getImages() {

		return "couchdb:3.1.1";
	}

}
