package com.brchain.core.util.container;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public abstract class Container {

	@Value("${brchain.sourcedir}")
	protected String sourceDir;

	@Value("${brchain.logdir}")
	protected String logDir;

	@Value("${brchain.datadir}")
	protected String dataDir;

	@Value("${brchain.networkmode}")
	protected String networkMode;

	protected String orgName;
	protected String containerName;
	protected String port;
	protected String type;
	protected int    num;

	public void initSetting(String orgName, String type, String port, int num) {

		this.type          = type;
		this.orgName       = orgName;
		this.port          = port;
		this.num           = num;
		this.containerName = type + num + ".org" + orgName + ".com";
	}

	public abstract List<String> getBinds();

	public abstract List<String> getContainerEnv(String param, Boolean couchdbYn);

	public abstract List<String> getCmd();

	public Set<String> getExposedPort(String[] ports) {
		Set<String> exposedPorts = new HashSet<>();
		if (ports != null) {

			for (String port : ports) {
				exposedPorts.add(port);
			}
		}
		return exposedPorts;
	}

	public abstract String getImages();

}
