Credentials Maven Plugin
------------------------

The ability to look up credentials in a file that's only visible to the user
running Maven is one of the great selling points of Maven. Some plugins that
support this functionality include the [Maven SCM Plugin](http://maven.apache.org/scm/maven-scm-plugin), 
[SQL Maven Plugin](http://mojo.codehaus.org/sql-maven-plugin) and 
[DbUnit Maven Plugin](http://mojo.codehaus.org/dbunit-maven-plugin).

Unfortunately some important plugins don't yet support this functionality,
such as the [SCM Publish Maven Plugin](http://maven.apache.org/plugins/maven-scm-publish-plugin).

This plugin looks up credentials in `~/.m2/settings.xml`, and sets them to specifiable Maven and/or Java system properties.

Also provides the ability to set the username and password via system
properties. This is to support situations where the credentials may not be
available in all execution environments, but setting the credentials in plain text is controlled, 
such as continuous integration servers.

By way of attribution, this plugin is essentially a mashup of the 
[SQL Maven Plugin](http://mojo.codehaus.org/sql-maven-plugin) and the
[Properties Maven Plugin](http://mojo.codehaus.org/properties-maven-plugin), 
with some help from [this example](http://svn.apache.org/repos/asf/maven/sandbox/trunk/examples/plugins/maven-security-mojo).


Goals Overview
--------------

- [credentials:set](./set-mojo.html) set properties with looked-up credentials.
- [credentials:help](./help-mojo.html) get help.

Usage
-----

General instructions on how to use the Credentials Maven Plugin can be found on the [usage page](./usage.html).

In case you still have questions regarding the plugin's usage, please feel
free to contact the [user mailing list](./mail-lists.html). The posts to the mailing list are archived and could
already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching
the [mail archive](./mail-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our
[issue tracker](./issue-tracking.html). When creating a new issue, please provide a comprehensive description of your
concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason,
entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated.
Of course, patches are most welcome too. Contributors can check out the project from our
[source repository](./source-repository.html) and will find supplementary information in the
[guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html). 

Examples
--------

- [Executions.](./examples/set.html)
- [Hide username/password in `settings.xml`.](./examples/settings.html)
