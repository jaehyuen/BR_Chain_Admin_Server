package com.brchain.core.ca.service;

import java.net.MalformedURLException;
import java.util.Properties;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.stereotype.Service;

import com.brchain.core.util.BrchainUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaService {




	public void test() throws Exception{

		Properties props = new Properties();
		props.put("pemFile","crypto-config/ca-certs/ca.orgapeer.com-cert.pem");
//		props.put("allowAllHostNames", "true");
		HFCAClient caClient = HFCAClient.createNewInstance("http://192.168.65.169:1111", props);
		CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
		caClient.setCryptoSuite(cryptoSuite);
		
		Enrollment admin =caClient.enroll("admin", "adminpw");
		
		System.out.println(admin.getCert());
		
		User adminUser = new BrchainUser("peer1.orgapeertest2.com ", "apeer", "apeerMSP", (X509Enrollment) admin);
		
		RegistrationRequest registrationRequest = new RegistrationRequest("peer1.orgapeertest2.com ");
		registrationRequest.setAffiliation("org1.department1");
		registrationRequest.setEnrollmentID("peer1.orgapeertest2.com");
		String enrollmentSecret = caClient.register(registrationRequest, adminUser);
		
		System.out.println(enrollmentSecret);
		
		Enrollment enrollment = caClient.enroll("appUser", enrollmentSecret);
		
		System.out.println(enrollment.getCert());
	

	}

	

}
