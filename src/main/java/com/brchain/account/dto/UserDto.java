package com.brchain.account.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDto extends RegisterDto{

	private Long id;
	private boolean active;

}
