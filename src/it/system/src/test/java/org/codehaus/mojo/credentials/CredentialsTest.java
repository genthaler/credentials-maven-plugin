package org.codehaus.mojo.credentials;

import junit.framework.TestCase;

public class CredentialsTest extends TestCase {

	public void testCredentials() throws Exception {
		System.out.println(System.getProperties().toString());
		assertEquals("me", System.getProperty("username"));
		assertEquals("mine", System.getProperty("password"));
	}
}
