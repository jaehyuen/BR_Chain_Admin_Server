package com.brchain.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "CCINFO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcInfoEntity {


    @Id
    @Column(name ="CC_NAME" , nullable = false)
	private String ccName;
    
    @Column(name ="CC_PATH" , nullable = false)
	private String ccPath;
    
    @Column(name ="CC_LANG" , nullable = false)
	private String ccLang;;
	
    @Column(name ="CC_DESC" , nullable = false)
	private String ccDesc;;
	

    
 
}
