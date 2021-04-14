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
		return this.name;
	}

	@Override
	public Set<String> getRoles() {
		return this.roles;
	}

	@Override
	public String getAccount() {
		return this.account;
	}

	@Override
	public String getAffiliation() {
		return this.affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		return this.enrollment;
	}

	@Override
	public String getMspId() {
		return this.mspId;
	}

	public String getOrganization() {
		return this.organization;
	}

}