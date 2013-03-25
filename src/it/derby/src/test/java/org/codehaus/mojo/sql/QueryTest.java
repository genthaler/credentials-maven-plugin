package org.codehaus.mojo.credentials;

import java.credentials.Connection;
import java.credentials.Driver;
import java.credentials.ResultSet;
import java.credentials.Statement;

import junit.framework.TestCase;

public class QueryTest
    extends TestCase
{

    public void testQuery()
        throws Exception
    {
        Class dc = Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
        Driver   driverInstance = (Driver) dc.newInstance();

        Connection conn = driverInstance.connect( "jdbc:derby:target/testdb", null );

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery( "select count(*) from derbyDB" );
        rs.next();
        assertEquals( 2, rs.getInt(1) );  
        
    }
}
