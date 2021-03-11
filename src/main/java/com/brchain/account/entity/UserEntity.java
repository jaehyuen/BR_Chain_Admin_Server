package com.brchain.account.entity;

import com.brchain.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "USER")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "USER_NAME")
	private String userName;

	@Column(name = "USER_ID", unique=true)
	private String userId;
	
	@Column(name = "USER_PASSWORD")
	private String userPassword;

	@Email
	@Column(name = "USER_EMAIL")
	private String userEmail;

	@Column(name = "ACTIVE")
	private boolean active;
}
