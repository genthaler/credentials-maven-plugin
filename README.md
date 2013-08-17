# credentials-maven-plugin

A Maven 2/3 plugin to store credentials from settings.xml and security-settings.xml into properties where they can be used by other plugins and/or Java code.

Please see the [Project Page](http://genthaler.github.com/credentials-maven-plugin).

There's also the [Issues](https://github.com/genthaler/credentials-maven-plugin/issues) page.

This is plugin is also something of a testbed for Maven plugin testing strategies. I'm using [Cucumber-JVM](https://github.com/cucumber/cucumber-jvm) to drive standard [JUnit](http://junit.org)-style tests as well as [Maven Verifier](http://maven.apache.org/shared/maven-verifier) integration tests. I'm also using the [Maven Invoker Plugin](http://maven.apache.org/plugins/maven-invoker-plugin) for standalone functional tests.

