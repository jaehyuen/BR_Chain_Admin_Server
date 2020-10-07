package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;


import com.brchain.core.entity.ConInfoEntity;

public interface ConInfoRepository extends JpaRepository<ConInfoEntity,String>{
	
	ConInfoEntity findByConName(String conName);
	
	ArrayList<ConInfoEntity> findByConTypeAndOrgType(String conType,String orgType);
	
	ArrayList<ConInfoEntity> findByConTypeAndOrgName(String conType,String orgName);
	
	ArrayList<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType,String orgType,String orgName);
	
	ArrayList<ConInfoEntity> findByConType(String conType);
	
	ArrayList<ConInfoEntity> findByConPort(String conPort);
	
	ArrayList<ConInfoEntity> findByOrgName(String orgName);
	

}
