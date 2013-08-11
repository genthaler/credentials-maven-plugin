package com.github.mojo.credentials;

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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.apache.maven.shared.utils.ReaderFactory;
import org.apache.maven.shared.utils.WriterFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
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

import com.github.mojo.credentials.CredentialsMojoIT.MyReportBuilder;
import com.github.mojo.credentials.CredentialsMojoIT.MyStoryControls;
import com.github.mojo.credentials.CredentialsMojoIT.MyStoryLoader;

/**
 * Unit test for CredentialsMojo.
 */
@RunWith(AnnotatedPathRunner.class)
@Configure(storyControls = MyStoryControls.class, storyLoader = MyStoryLoader.class, storyReporterBuilder = MyReportBuilder.class)
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true, storyTimeoutInSecs = 100, threads = 1, metaFilters = "-skip", systemProperties = "java.awt.headless=true")
@UsingSteps(instances = { CredentialsMojoIT.class })
@UsingPaths(searchIn = "src/test/resources", includes = { "**/credentials_it.story" })
public class CredentialsMojoIT {

	private static String basedir;
	private static String groupId;
	private static String artifactId;
	private static String version;
	private Verifier verifier;
	private Model model;
	private Throwable thrown;
	private Plugin credentialsPlugin;
	private int folder = 0;
	private File execDir;
	private static File target;
	private static File testDir;
	private static File localRepository;
	private static Settings settings;

	@BeforeStories()
	public static void beforeClass() throws IOException, XmlPullParserException {
		target = new File(getBasedir(), "target");
		testDir = new File(target, "it2");
		if (testDir.exists())
			testDir.delete();
		testDir.mkdir();
		extractCurrentModel();
		extractCurrentSettings();
	}

	@BeforeScenario
	public void setUp() throws VerificationException {
		model = createModel();
		folder++;
		execDir = new File(testDir, Integer.toString(folder));
		execDir.mkdirs();
		verifier = new Verifier(execDir.getAbsolutePath());
		verifier.setLocalRepo(localRepository.getAbsolutePath());
		verifier.setAutoclean(false);
	}

	@Given("no credentials plugin configuration")
	public void emptyMojo() {
		model.getBuild().getPlugins().remove(credentialsPlugin);
	}

	@SuppressWarnings("serial")
	@Given("the credentials-maven-plugin $property property is $value")
	public void setMojoProperty(String property, final String propertyValue) {
		Xpp3Dom configuration = (Xpp3Dom) credentialsPlugin.getConfiguration();
		configuration.addChild(new Xpp3Dom(property) {
			{
				setValue(propertyValue);
			}
		});
	}

	@Given("the $id Server username is $value")
	public void setServerUsername(String id, String value) {
		getServer(id).setUsername(value);
	}

	@Given("the $id Server password is $value")
	public void setServerPassword(String id, String value) {
		getServer(id).setPassword(value);
	}

	@Given("the $property commandline property is $value")
	public void setSystemProperty(String property, String value) {
		verifier.setSystemProperty(property, value);
	}

	@When("the project is executed")
	public void executeMojo() throws VerificationException, IOException {
		try {
			new MavenXpp3Writer().write(
					WriterFactory.newXmlWriter(new File(execDir, "pom.xml")),
					model);

			File settingsFile = new File(execDir, "settings.xml");
			new SettingsXpp3Writer().write(
					WriterFactory.newXmlWriter(settingsFile), settings);

			verifier.addCliOption(String.format("-s %s",
					settingsFile.getAbsolutePath()));

			verifier.executeGoals(Arrays.asList("credentials:set",
					"script:execute"));
		} catch (VerificationException e) {
			thrown = e;
		} catch (IOException e) {
			thrown = e;
		}
	}

	@Then("there should have been a Project $property property with value $value")
	public void assertProjectProperty(String property, String value)
			throws VerificationException {
		verifier.verifyTextInLog("project." + property + "=" + value);
	}

	@Then("there should have been a System $property property with value $value")
	public void assertSystemProperty(String property, String value)
			throws VerificationException {
		verifier.verifyTextInLog("system." + property + "=" + value);
	}

	@Then("nothing happens")
	@Alias("there are no errors")
	public void nothingHappens() throws VerificationException {
		try {
			verifier.verifyErrorFreeLog();

		} catch (VerificationException e) {
			e.printStackTrace();
		}
	}

	@Then("an exception should be thrown with the message '$message'")
	public void anExceptionIsThrown(String message)
			throws VerificationException {
		assertThat("It looks like there was no exception thrown", thrown,
				not(nullValue()));

		assertThat(
				"Thrown exception doesn't contain the expected message fragment",
				thrown.getMessage(), containsString(message));
	}

	@AfterScenario
	public void tearDown() throws Exception {
		verifier.resetStreams();
	}

	private Server getServer(String id) {
		Server server = settings.getServer(id);
		if (null == server) {
			server = new Server();
			server.setId(id);
			settings.addServer(server);
		}
		return server;
	}

	private static void extractCurrentModel() throws IOException,
			XmlPullParserException {
		MavenXpp3Reader pomReader;
		pomReader = new MavenXpp3Reader();
		Model projectModel = pomReader.read(ReaderFactory
				.newXmlReader(new File(getBasedir(), "pom.xml")));

		groupId = projectModel.getGroupId();
		artifactId = projectModel.getArtifactId();
		version = projectModel.getVersion();
		target = new File(getBasedir(), "target");
		localRepository = new File(target, "local-repo");
	}

	private static void extractCurrentSettings() throws IOException,
			XmlPullParserException {
		SettingsXpp3Reader settingsReader;
		settingsReader = new SettingsXpp3Reader();
		settings = settingsReader
				.read(ReaderFactory.newXmlReader(FileUtils.getFile(
						getBasedir(), "src", "it", "settings-unencrypted.xml")));
		settings.getServers().clear();
		settings.getPluginGroups().add(groupId);
		for (Profile profile : settings.getProfiles()) {
			for (Repository repository : profile.getRepositories()) {
				if ("@localRepositoryUrl@"
						.equalsIgnoreCase(repository.getUrl()))
					repository.setUrl(localRepository.getAbsolutePath());
			}
		}
	}

	private Model createModel() {
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(groupId);
		model.setArtifactId(artifactId + "-it");
		model.setVersion(version);
		model.addProperty("project.build.sourceEncoding", "UTF-8");
		model.setBuild(new Build());
		model.getBuild().addPlugin(createCredentialsPlugin());
		model.getBuild().addPlugin(createScriptPlugin());
		return model;
	}

	private Plugin createCredentialsPlugin() {
		credentialsPlugin = new Plugin();
		credentialsPlugin.setGroupId(groupId);
		credentialsPlugin.setArtifactId(artifactId);
		credentialsPlugin.setVersion(version);
		Xpp3Dom config = new Xpp3Dom("configuration");
		credentialsPlugin.setConfiguration(config);
		return credentialsPlugin;
	}

	@SuppressWarnings("serial")
	private Plugin createScriptPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId("com.alexecollins.maven.plugin");
		plugin.setArtifactId("script-maven-plugin");
		plugin.setVersion("1.0.0");
		Xpp3Dom config = new Xpp3Dom("configuration") {
			{
				addChild(new Xpp3Dom("script") {
					{
						StringBuffer buf = new StringBuffer();
						buf.append("for ( property : project.properties.stringPropertyNames() ) ");
						buf.append("print( \"project.\" + property + \"=\" + project.properties.getProperty(property) ); ");
						buf.append("for ( property : System.getProperties().stringPropertyNames() ) ");
						buf.append("print( \"system.\" + property + \"=\" + System.getProperties().getProperty(property) );");
						setValue(buf.toString());
					}
				});
			}
		};
		plugin.setConfiguration(config);
		plugin.addDependency(new Dependency() {
			{
				setGroupId("org.beanshell");
				setArtifactId("bsh");
				setVersion("2.0b5");
			}
		});
		return plugin;
	}

	private static String getBasedir() {
		if (basedir != null) {
			return basedir;
		}
		basedir = System.getProperty("basedir");
		if (basedir == null) {
			basedir = new File("").getAbsolutePath();
		}
		return basedir;
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
