package com.brchain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private Long id;
	private String userName;
	private String userId;
	private String password;
	private String email;
	private boolean active;

}
