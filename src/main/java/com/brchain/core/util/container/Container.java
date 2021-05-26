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
	protected String type;
	protected String containerName;
	protected String port;
	protected int    num;

	public abstract void initSetting(String orgName, String port, int num);

	public abstract List<String> setBinds();

	public abstract List<String> setContainerEnv(String param, boolean couchdb);

	public abstract List<String> setCmd();

	public Set<String> setExposedPort(String[] ports) {
		Set<String> exposedPorts = new HashSet<>();
		if (ports != null) {

			for (String port : ports) {
				exposedPorts.add(port);
			}
		}
		return exposedPorts;
	}

	public abstract String setImages();

}
