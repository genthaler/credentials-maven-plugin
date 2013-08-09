package com.github.genthaler.credentials;

/*
 * Copyright 2006 The Codehaus
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.When;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.junit.runner.RunWith;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import com.github.genthaler.credentials.CredentialsMojo;
import com.github.genthaler.credentials.CredentialsMojoTest.MyReportBuilder;
import com.github.genthaler.credentials.CredentialsMojoTest.MyStoryControls;
import com.github.genthaler.credentials.CredentialsMojoTest.MyStoryLoader;

/**
 * Unit test for CredentialsMojo.
 */
@RunWith(AnnotatedPathRunner.class)
@Configure(storyControls = MyStoryControls.class, storyLoader = MyStoryLoader.class, storyReporterBuilder = MyReportBuilder.class)
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true, storyTimeoutInSecs = 100, threads = 1, metaFilters = "-skip", systemProperties = "java.awt.headless=true")
@UsingSteps(instances = { CredentialsMojoTest.class })
@UsingPaths(searchIn = "src/test/resources", includes = { "**/credentials_test.story" })
public class CredentialsMojoTest extends AbstractMojoTestCase {
	private CredentialsMojo mojo;
	private MavenProjectStub project;
	private Settings settings;
	private Server server;
	private Properties properties;
	private Throwable thrown = null;

	@Override
	@BeforeScenario
	public void setUp() throws Exception {
		super.setUp();
		mojo = new CredentialsMojo();
		SecDispatcher securityDispatcher = (SecDispatcher) lookup(
				"org.sonatype.plexus.components.sec.dispatcher.SecDispatcher",
				"default");
		mojo.setSecurityDispatcher(securityDispatcher);
		properties = new Properties();
		project = new MavenProjectStub() {
			@Override
			public Properties getProperties() {
				return properties;
			}
		};
		setVariableValueToObject(mojo, "project", project);
		settings = new Settings();
		mojo.setSettings(settings);
		server = new Server();
		settings.addServer(server);
	}

	@Given("an empty Mojo")
	public void emptyMojo() {
	}

	@Given("the Mojo $property property is $value")
	public void setMojoProperty(String property, String value)
			throws IllegalAccessException {
		if ("useSystemProperties".equalsIgnoreCase(property))
			setVariableValueToObject(mojo, property,
					Boolean.parseBoolean(value));
		else
			setVariableValueToObject(mojo, property, value);
	}

	@Given("the Server $property property is $value")
	public void setServerProperty(String property, String value)
			throws IllegalAccessException {
		setVariableValueToObject(server, property, value);
	}

	@Given("the Project $property property is $value")
	public void setProjectProperty(String property, String value)
			throws IllegalAccessException {
		properties.setProperty(property, value);
	}

	@Given("the System $property property is $value")
	public void setSystemProperty(String property, String value)
			throws IllegalAccessException {
		System.setProperty(property, value);
	}

	@When("the Mojo is executed")
	public void executeMojo() {
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			thrown = e;
		}
	}

	@Then("the Project should have a $property property with value $value")
	public void assertProjectProperty(String property, String value) {
		assertEquals(value, properties.getProperty(property));
	}

	@Then("the System should have a $property property with value $value")
	public void assertSystemProperty(String property, String value) {
		assertEquals(value, System.clearProperty(property));
	}

	@Then("nothing happens")
	public void nothingHappens() {
	}

	@Then("a $exceptionClass should be thrown with the message '$message'")
	public void anExceptionIsThrown(String exceptionClass, String message) {
		assertEquals(exceptionClass, thrown.getClass().getSimpleName());
		assertEquals(message, thrown.getMessage());
	}

	public static class MyStoryControls extends StoryControls {
		public MyStoryControls() {
			doDryRun(false);
			doSkipScenariosAfterFailure(false);
		}
	}

	public static class MyStoryLoader extends LoadFromClasspath {
		public MyStoryLoader() {
			super(CredentialsMojoTest.class.getClassLoader());
		}
	}

	public static class MyReportBuilder extends StoryReporterBuilder {
		public MyReportBuilder() {
			this.withFormats(CONSOLE, TXT, HTML, XML).withDefaultFormats()
					.withFailureTrace(true);
		}
	}
}
