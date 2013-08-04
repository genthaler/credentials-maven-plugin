package com.github.genthaler.credentials;

import junit.framework.TestCase;

public class CredentialsTest extends TestCase {

	public void testCredentials() throws Exception {
		System.out.println(System.getProperties().toString());
		assertEquals("you", System.getProperty("username"));
		assertEquals("yours", System.getProperty("password"));
	}
}
