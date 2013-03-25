File outputFile = new File( basedir, 'target/oracle-credentialsplus.credentials')
outputFile.getParentFile().mkdirs();
new FileReader( 'src/main/credentials/oracle-credentialsplus.credentials' ).transformLine( outputFile.newWriter() ) { line->
  line.replaceAll( '&1', 'customer' ).replaceAll( '&&schema..', 'SOME_SCHEMA.' ) 
}
