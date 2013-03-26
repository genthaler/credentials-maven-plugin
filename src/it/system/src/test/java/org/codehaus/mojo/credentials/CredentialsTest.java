package org.codehaus.mojo.credentials;

import java.credentials.Connection;
import java.credentials.Driver;
import java.credentials.ResultSet;
import java.credentials.Statement;

import junit.framework.TestCase;

public class QueryTest extends TestCase {

	public void testQuery() throws Exception {

		assertEquals("thePassword", System.getProperty("me"));
	}
}
