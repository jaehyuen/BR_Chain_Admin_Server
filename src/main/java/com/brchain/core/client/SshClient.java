package com.brchain.core.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.brchain.common.exception.BrchainException;
import com.brchain.core.util.BrchainStatusCode;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * ssh/sftp 연결을 위한 클라이언트 클래스
 * 
 * @author jaehyeon
 *
 */
@Component
public class SshClient {

	private Logger      logger      = LoggerFactory.getLogger(this.getClass());
	private Session     session     = null;
	private Channel     channel     = null;
	private ChannelSftp channelSftp = null;
	private ChannelExec channelExec = null;

	@Value("${brchain.ssh.user}")
	private String      username;

	@Value("${brchain.ip}")
	private String      ip;

	@Value("${brchain.ssh.pass}")
	private String      password;

	@Value("${brchain.ssh.port}")
	private int         port;

	@Value("${brchain.sourcedir}")
	private String      sourceDir;

	@Value("${brchain.logdir}")
	private String      logDir;

	@Value("${brchain.datadir}")
	private String      dataDir;

	/**
	 * ssh 및 sftp 연결 함수
	 * 
	 * @throws JSchException
	 */

	@PostConstruct
	public void connect() throws JSchException {

		JSch       jsch   = new JSch();

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");

		session = jsch.getSession(username, ip, port);
		session.setPassword(password);
		session.setConfig(config);
		session.connect();

		channel     = session.openChannel("exec");
		channelExec = (ChannelExec) channel;

		channel     = session.openChannel("sftp");
		channel.connect();
		channelSftp = (ChannelSftp) channel;

	}

	/**
	 * 폴더 삭제 함수
	 * 
	 * @param orgName 조직명
	 * 
	 * @return
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws JSchException
	 */
	public void removeDir(String orgName, String conName) {
		try {

			String command = "rm -rf " + logDir + " " + dataDir + "/*/*" + conName + "* " + sourceDir
					+ "/crypto-config/*/*" + orgName + "* " + dataDir + "/ca " + sourceDir + "/channel-artifacts/"
					+ orgName + " | mkdir -p  " + sourceDir + "/channel-artifacts | cp -r " + sourceDir + "/bin "
					+ sourceDir + "/channel-artifacts/";

			channelExec.setCommand(command);
			channelExec.connect();

		} catch (JSchException e) {
			throw new BrchainException(e, BrchainStatusCode.DELETE_DIR_ERROR);
		}

	}

	/**
	 * 커맨드 실행 함수
	 * 
	 * @param command 커맨드
	 * 
	 * @throws DockerException
	 * @throws InterruptedException
	 * @throws JSchException
	 */

	public void execCommand(String command) {

		try {
			
			if (channelExec.isClosed()) {
				connect();
			}

			logger.info("[커멘드 실행]" + command);
			channelExec.setCommand(command);
			channelExec.connect();
			channelExec.disconnect();
			
			Thread.sleep(1000);

		} catch (JSchException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.EXEC_COMMAND_ERROR);
		}

	}

//	public void deleteFolder(String path) {
//
//		logger.info("deleteFolder");
//		path = System.getProperty("user.dir") + "/" + path;
//		logger.info(path);
//		File folder = new File(path);
//
//		if (folder.exists()) {
//			File[] deleteFolderList = folder.listFiles();
//
//			for (int j = 0; j < deleteFolderList.length; j++) {
//				deleteFolderList[j].delete();
//			}
//
//			if (deleteFolderList.length == 0 && folder.isDirectory()) {
//				folder.delete();
//			}
//		}
//	}

	/**
	 * 파일 업로드 함수
	 * 
	 * @param path           업로드경로
	 * @param uploadFileName 파일명
	 * @throws FileNotFoundException
	 * 
	 * @throws Exception
	 */
	public void uploadFile(String path, String uploadFileName) {

		File file = new File(System.getProperty("user.dir") + "/" + path + "/" + uploadFileName);

		try (FileInputStream inputStream = new FileInputStream(file);) {

			String dir = sourceDir + "/" + path;
			execCommand("mkdir -p " + dir);

			logger.info("[파일 업로드 실행]" + dir + uploadFileName);

			// Change to output directory
			channelSftp.cd(dir);

			// 파일을 업로드한다.
			channelSftp.put(inputStream, file.getName());
			Thread.sleep(2000);

		} catch (SftpException | IOException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_UPLOAD_ERROR);
		}

	}

	/**
	 * 파일 다운로드 함수
	 * 
	 * @param path             파일경로
	 * @param downloadFileName 파일명
	 * 
	 * @throws SftpException
	 * @throws IOException
	 * @throws JSchException
	 */

	public void downloadFile(String path, String downloadFileName) {

		try {

			InputStream      inputStream  = null;
			FileOutputStream outputStream = null;

			String           dir          = sourceDir + "/" + path;
			logger.info("[파일 다운로드 실행]" + dir + downloadFileName);
			channelSftp.cd(dir);

			inputStream = channelSftp.get(downloadFileName);
			File file = new File(System.getProperty("user.dir") + "/" + path);

			if (!file.exists()) {
				try {
					file.mkdirs();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			outputStream = new FileOutputStream(
					new File(System.getProperty("user.dir") + "/" + path + downloadFileName));
			int i;

			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}

			outputStream.close();
			inputStream.close();
			Thread.sleep(1000);

		} catch (SftpException | IOException | InterruptedException e) {
			throw new BrchainException(e, BrchainStatusCode.FILE_DOWNLOAN_ERROR);
		}
	}
}
