package com.capitalone.dashboard.request;

import static org.junit.Assert.*;

import org.junit.Test;

import com.capitalone.dashboard.model.Authentication;


public class AuthenticationRequestTest {

    
    @Test
	public void test_Equals(){
    	AuthenticationRequest test = new AuthenticationRequest();
    	test.setPassword("test");
    	test.setUsername("test1");
    	assertEquals(test.getPassword(),"test");
    	assertEquals(test.getUsername(),"test1");
    	Authentication auth = new Authentication("test1", "test");
    	assertEquals(test.toAuthentication().getPassword(), auth.getPassword());
    	assertNotEquals(test.copyTo(auth), auth);
    }
    

}
