# credentials-maven-plugin

A Maven 2/3 plugin to store credentials from settings.xml and security-settings.xml into properties where they can be used by other plugins and/or Java code.

Please also see the [Project Page](http://genthaler.github.com/credentials-maven-plugin/)

## Approach

<table>
<tr><th>Task</th> <th>Status</th></tr>
<tr><td>clone the sql-maven-plugin</td><td> Done </td></tr>
<tr><td>copy in the parts of the properties-maven-plugin that publish properties</td><td> Done </td></tr>
<tr><td> Fix up unit and integration tests </td><td> Done </td></tr>
<tr><td> Add links to site </td><td> Pending </td></tr>
<tr><td> fix github upload plugin to upload as a Maven 2 repository </td><td> Pending </td></tr>
<tr><td> mvn release </td><td> Pending </td></tr>
<tr><td> Publish to Central </td><td> Pending </td></tr>
</table>

## Current status

Written most of the code, now sorting out unit and integration tests

## Links

- http://mojo.codehaus.org/sql-maven-plugin/
- http://mojo.codehaus.org/sql-maven-plugin/examples/settings.html
- http://mojo.codehaus.org/properties-maven-plugin
- http://docs.codehaus.org/display/MAVENUSER/MavenPropertiesGuide
- http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/expose-security-credentials-mojo.html
- http://www.sonatype.com/books/mvnref-book/reference/resource-filtering-sect-properties.html
- http://stackoverflow.com/questions/1559179/how-to-get-the-values-of-server-defined-in-the-settings-xml-to-use-them-in-my-po
- http://maven.apache.org/ref/2.1.0/maven-settings/apidocs/org/apache/maven/settings/Settings.html#getServer%28java.lang.String%29
- http://docs.codehaus.org/display/GMAVEN/Executing+Groovy+Code
