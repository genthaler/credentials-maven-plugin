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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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

/**
 * Unit test for SetMojo.
 */
public class MojoITState {

	static final String SCRIPT_GROUP_ID = "com.alexecollins.maven.plugin";
	static final String SCRIPT_ARTIFACT_ID = "script-maven-plugin";
	static final String SCRIPT_VERSION = "1.0.0";
	private String groupId;
	private String artifactId;
	private String version;
	private Model model;
	private final File execDir;
	private File target;
	private File localRepository;
	private File settingsFile;
	private Settings settings;
	private Plugin credentialsPlugin;
	private Model projectModel;

	public MojoITState(int folder) throws IOException, XmlPullParserException {
		super();
		extractCurrentModel();
		extractCurrentSettings();
		createModel();
		execDir = FileUtils.getFile(getBasedir(), "target", "it2",
				Integer.toString(folder));

		// Pointless return value from mkdirs(). In particular, it returns false
		// if the path already exists.
		execDir.mkdirs();
	}

	public Server getServer(String id) {
		Server server = settings.getServer(id);
		if (null == server) {
			server = new Server();
			server.setId(id);
			settings.addServer(server);
		}
		return server;
	}

	public void extractCurrentModel() throws IOException,
			XmlPullParserException {
		MavenXpp3Reader pomReader = new MavenXpp3Reader();
		projectModel = pomReader.read(ReaderFactory.newXmlReader(new File(
				getBasedir(), "pom.xml")));

		groupId = projectModel.getGroupId();
		artifactId = projectModel.getArtifactId();
		version = projectModel.getVersion();
		target = new File(getBasedir(), "target");
		localRepository = new File(target, "local-repo");
	}

	public void extractCurrentSettings() throws IOException,
			XmlPullParserException {
		SettingsXpp3Reader settingsReader;
		settingsReader = new SettingsXpp3Reader();
		settings = settingsReader
				.read(ReaderFactory.newXmlReader(FileUtils.getFile(
						getBasedir(), "src", "it", "settings-unencrypted.xml")));
		settings.getServers().clear();
		settings.getPluginGroups().add(groupId);
		for (Profile profile : settings.getProfiles()) {
			for (Repository repository : profile.getRepositories())
				if ("local.central".equals(repository.getId()))
					repository.setUrl(localRepository.toURI().toString());
			for (Repository repository : profile.getPluginRepositories())
				if ("local.central".equals(repository.getId()))
					repository.setUrl(localRepository.toURI().toString());
		}
	}

	public Model createModel() {
		model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(groupId);
		model.setArtifactId(artifactId + "-it");
		model.setVersion(version);
		model.addProperty("project.build.sourceEncoding", "UTF-8");
		model.setBuild(new Build());
		model.getBuild().addPlugin(
				createCredentialsPlugin(groupId, artifactId, version));
		model.getBuild().addPlugin(createScriptPlugin());
		return model;
	}

	public Plugin createCredentialsPlugin(String groupId, String artifactId,
			String version) {
		credentialsPlugin = new Plugin();
		credentialsPlugin.setGroupId(groupId);
		credentialsPlugin.setArtifactId(artifactId);
		credentialsPlugin.setVersion(version);
		Xpp3Dom config = new Xpp3Dom("configuration");
		credentialsPlugin.setConfiguration(config);
		return credentialsPlugin;
	}

	@SuppressWarnings("serial")
	public Plugin createScriptPlugin() {
		Plugin plugin = new Plugin();
		plugin.setGroupId(SCRIPT_GROUP_ID);
		plugin.setArtifactId(SCRIPT_ARTIFACT_ID);
		plugin.setVersion(SCRIPT_VERSION);
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

	public void writeState() throws IOException {
		new MavenXpp3Writer()
				.write(WriterFactory.newXmlWriter(new File(execDir, "pom.xml")),
						model);

		settingsFile = new File(execDir, "settings.xml");
		new SettingsXpp3Writer().write(
				WriterFactory.newXmlWriter(settingsFile), settings);

	}

	public String getBasedir() {
		String basedir;
		basedir = System.getProperty("basedir");
		if (basedir == null) {
			basedir = new File("").getAbsolutePath();
		}
		return basedir;
	}

	/**
	 * @return the groupId
	 */
	public final String getGroupId() {
		return groupId;
	}

	/**
	 * @return the artifactId
	 */
	public final String getArtifactId() {
		return artifactId;
	}

	/**
	 * @return the version
	 */
	public final String getVersion() {
		return version;
	}

	/**
	 * @return the model
	 */
	public final Model getModel() {
		return model;
	}

	/**
	 * @return the execDir
	 */
	public final File getExecDir() {
		return execDir;
	}

	/**
	 * @return the target
	 */
	public final File getTarget() {
		return target;
	}

	/**
	 * @return the localRepository
	 */
	public final File getLocalRepository() {
		return localRepository;
	}

	/**
	 * @return the settings
	 */
	public final Settings getSettings() {
		return settings;
	}

	/**
	 * @return the settingsFile
	 */
	public File getSettingsFile() {
		return settingsFile;
	}

	/**
	 * @return the credentialsPlugin
	 */
	public final Plugin getCredentialsPlugin() {
		return credentialsPlugin;
	}
}
