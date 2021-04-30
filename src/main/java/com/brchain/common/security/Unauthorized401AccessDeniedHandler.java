package com.brchain.common.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class Unauthorized401AccessDeniedHandler  implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		// TODO Auto-generated method stub
//		response.setStatus(401);
		System.out.println("testeesteesteesteesteesteesteeste");
		response.sendError(401, "test");
		
	}

}
