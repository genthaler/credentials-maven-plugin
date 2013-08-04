# credentials-maven-plugin

A Maven 2/3 plugin to store credentials from settings.xml and security-settings.xml into properties where they can be used by other plugins and/or Java code.

Please see the [Project Page](http://genthaler.github.com/credentials-maven-plugin).

There's also the [Issues](https://github.com/genthaler/credentials-maven-plugin/issues) page.

This is plugin is also something of a testbed for Maven plugin testing strategies. I'm using [JBehave](http://jbehave.org) to drive standard [JUnit](http://junit.org)-style tests as well as [Maven Verifier](http://maven.apache.org/shared/maven-verifier) integration tests. I'm also using the [Maven Invoker Plugin](http://maven.apache.org/plugins/maven-invoker-plugin) for standalone functional tests.

I had some issues with the Maven Verifier; some of the behaviour I wanted and expected, worked fine in the unit and Maven Invoker plugin tests, but not with the verifier.

I like the specification language of JBehave, but the feedback is pretty poor when iteratively trying to get the tests to work; running from Eclipse, there's no per-scenario feedback in the JUnit view, and if you have more than one story and/or Step class, it can be a bit non-deterministic getting the right combination testing. I had to use different language in the different stories for the sole purpose of getting the right feedback in the story editor.
