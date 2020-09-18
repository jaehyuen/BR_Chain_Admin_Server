package com.brchain.core.util;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import io.netty.util.internal.StringUtil;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

public class BrchainUser implements User {
 

    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private String organization;
    private String enrollmentSecret;
    Enrollment enrollment = null; //need access in test env.
    String mspId;


    public BrchainUser(String name, String org,String mspId, X509Enrollment enrollment ) {
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
    
    public void setRoles(Set<String> roles) {

        this.roles = roles;
    
    }
    @Override
    public String getAccount() {
        return this.account;
    }

    /**
     * Set the account.
     *
     * @param account The account.
     */
    public void setAccount(String account) {

        this.account = account;
        
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    /**
     * Set the affiliation.
     *
     * @param affiliation the affiliation.
     */
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
        
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    /**
     * Determine if this name has been registered.
     *
     * @return {@code true} if registered; otherwise {@code false}.
     */
    public boolean isRegistered() {
        return !StringUtil.isNullOrEmpty(enrollmentSecret);
    }

    /**
     * Determine if this name has been enrolled.
     *
     * @return {@code true} if enrolled; otherwise {@code false}.
     */
    public boolean isEnrolled() {
        return this.enrollment != null;
    }

 
   

    public String getEnrollmentSecret() {
        return enrollmentSecret;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
        
    }

    public void setEnrollment(Enrollment enrollment) {

        this.enrollment = enrollment;
        

    }

    public static String toKeyValStoreName(String name, String org) {
        return "user." + name + org;
    }

    @Override
    public String getMspId() {
        return mspId;
    }



    public void setMspId(String mspID) {
        this.mspId = mspID;
        

    }

}