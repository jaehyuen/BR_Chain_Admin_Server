package com.brchain.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;



@Component
public class Config {

//	@Value("${brchain.sourcedir}")
	public static String sourceDir;

//	@Value("${brchain.logdir}")
	public static String logDir;

//	@Value("${brchain.datadir}")
	public static String dataDir;
	
	@Value("${brchain.sourcedir}")
    private void setSourceDir(String sourceDir){
		Config.sourceDir = sourceDir;
    }
	
	@Value("${brchain.logdir}")
    private void setLogdir(String logDir){
		Config.logDir = logDir;
    }
	
	@Value("${brchain.datadir}")
    private void setDataDir(String dataDir){
		Config.dataDir = dataDir;
    }
	
//	public Config() {
//		this.setDataDir(dataDir);
//this.setLogdir(logDir);
//this.setSourceDir(sourceDir);
//	}
}
