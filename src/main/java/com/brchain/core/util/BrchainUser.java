package com.brchain.core.util;

import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

public class BrchainUser implements User {

	private String name;
	private Set<String> roles;
	private String account;
	private String affiliation;
	private String organization;

	private Enrollment enrollment = null; // need access in test env.
	private String mspId;

	public BrchainUser(String name, String org, String mspId, X509Enrollment enrollment) {
		this.name = name;
		this.mspId = mspId;
		this.organization = org;
		this.enrollment = enrollment;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public Set<String> getRoles() {
		// TODO Auto-generated method stub
		return this.roles;
	}

	@Override
	public String getAccount() {
		// TODO Auto-generated method stub
		return this.account;
	}

	@Override
	public String getAffiliation() {
		// TODO Auto-generated method stub
		return this.affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		// TODO Auto-generated method stub
		return this.enrollment;
	}

	@Override
	public String getMspId() {
		// TODO Auto-generated method stub
		return this.mspId;
	}

	public String getOrganization() {
		// TODO Auto-generated method stub
		return this.organization;
	}

}