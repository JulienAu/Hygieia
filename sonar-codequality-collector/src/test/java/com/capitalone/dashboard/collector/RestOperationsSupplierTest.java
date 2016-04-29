package com.capitalone.dashboard.collector;

import static org.junit.Assert.*;


import org.junit.Test;


public class RestOperationsSupplierTest {


	
    @Test
    public void RestOperations(){
    	RestOperationsSupplier rest = new RestOperationsSupplier();
		assertNotNull(rest.get());
    }


}

