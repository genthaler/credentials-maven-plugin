package com.github.genthaler.credentials;

/*
 * #%L
 * Credentials Maven Plugin
 * %%
 * Copyright (C) 2013 - 2014 Günther Enthaler
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Integration test for SetMojo and SetAllMojo.
 */
public class MojoITSteps {

	private Throwable thrown = null;
	private MojoITState state;
	private Verifier verifier;
	private static int folder = 0;

	@Before
	public void setUp() throws VerificationException, IOException,
			XmlPullParserException {
		state = new MojoITState(folder++);

		verifier = new Verifier(state.getExecDir().getAbsolutePath());
		verifier.setLocalRepo(state.getLocalRepository().getAbsolutePath());
		verifier.setAutoclean(false);
		// verifier.setForkJvm(false);
		thrown = null;
	}

	@Given("^no credentials plugin configuration$")
	public void emptyMojo() {
		state.getModel().getBuild().getPlugins()
				.remove(state.getCredentialsPlugin());
	}

	@SuppressWarnings("serial")
	@Given("^the credentials-maven-plugin (.*) property is (.*)$")
	public void setMojoProperty(String property, final String propertyValue) {
		Xpp3Dom configuration = (Xpp3Dom) state.getCredentialsPlugin()
				.getConfiguration();
		configuration.addChild(new Xpp3Dom(property) {
			{
				setValue(propertyValue);
			}
		});
	}

	@Given("^the (.*) Server username is (.*)$")
	public void setServerUsername(String id, String value) {
		state.getServer(id).setUsername(value);
	}

	@Given("^the (.*) Server password is (.*)$")
	public void setServerPassword(String id, String value) {
		state.getServer(id).setPassword(value);
	}

	@Given("^the (.*) commandline property is (.*)$")
	public void setSystemProperty(String property, String value) {
		verifier.setSystemProperty(property, value);
	}

	@When("^the (.*) goal is executed")
	public void executeMojo(String goal) throws VerificationException,
			IOException {
		try {
			state.writeState();
			verifier.addCliOption(String.format("-s %s", state
					.getSettingsFile().getAbsolutePath()));

			verifier.executeGoals(Arrays.asList(state.getGroupId() + ":"
					+ state.getArtifactId() + ":" + state.getVersion() + ":"
					+ goal, MojoITState.SCRIPT_GROUP_ID + ":"
					+ MojoITState.SCRIPT_ARTIFACT_ID + ":"
					+ MojoITState.SCRIPT_VERSION + ":execute"));
		} catch (VerificationException e) {
			thrown = e;
		} catch (IOException e) {
			thrown = e;
		}
	}

	@Then("^there should have been a Project (.*) property with value (.*)$")
	public void assertProjectProperty(String property, String value)
			throws VerificationException {
		assertThat("An exception was thrown", thrown, nullValue());
		verifier.verifyTextInLog("project." + property + "=" + value);
	}

	@Then("^there should have been a System (.*) property with value (.*)$")
	public void assertSystemProperty(String property, String value)
			throws VerificationException {
		assertThat("An exception was thrown", thrown, nullValue());
		verifier.verifyTextInLog("system." + property + "=" + value);
	}

	@Then("^there should have been no errors$")
	public void noErrors() throws VerificationException {
		// try {
		assertThat("An exception was thrown", thrown, nullValue());
		verifier.verifyErrorFreeLog();
		// } catch (VerificationException e) {
		// e.printStackTrace();
		// }
	}

	@Then("^an exception should be thrown with the message '(.*)'$")
	public void anExceptionIsThrown(String message)
			throws VerificationException {
		assertThat("It looks like there was no exception thrown", thrown,
				not(nullValue()));

		assertThat(
				"Thrown exception doesn't contain the expected message fragment",
				thrown.getMessage(), containsString(message));
	}

	@After
	public void tearDown() throws Exception {
		verifier.resetStreams();
	}
}
