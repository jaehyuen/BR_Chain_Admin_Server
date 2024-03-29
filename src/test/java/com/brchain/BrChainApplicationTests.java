package com.brchain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.brchain.core.fabric.service.FabricService;

@AutoConfigureMockMvc
@SpringBootTest
@RunWith(SpringRunner.class)
//@WebMvcTest(controllers =AuthController.class)
class BrChainApplicationTests {
//	@Autowired
//    private MockMvc mvc;
	
	@Autowired
	private FabricService fabricService;

	
    @Test
    void contextLoads() {
    }
    
    @Test
    void removeOrgTest() {
    	
    	fabricService.removeOrg("lalapeer");
    }
    
 
}
