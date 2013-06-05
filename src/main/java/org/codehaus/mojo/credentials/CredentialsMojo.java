package org.codehaus.mojo.credentials;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * Looks up credentials in settings.xml, and sets them to specifiable Maven or
 * JVM properties.
 * 
 * @goal set
 * @phase validate
 * @threadSafe
 */
public class CredentialsMojo extends AbstractMojo {
	/**
	 * Username. If not given, it will be looked up through
	 * <code>settings.xml</code>'s server with <code>${settingsKey}</code> as
	 * key.
	 * 
	 * If given, the username will not be looked up in settings.xml.
	 * 
	 * @since 1.0
	 * @parameter property="username"
	 */
	private String username;

	/**
	 * Property to which the username will be set. If not given, it will be set
	 * to <code>${settingsKey}.username</code> as key.
	 * 
	 * @since 1.0
	 * @parameter property="usernameProperty" default="username"
	 */
	private String usernameProperty;

	/**
	 * (Decrypted) password. If not given, it will be looked up through
	 * <code>settings.xml</code>'s server with <code>${settingsKey}</code> as
	 * key.
	 * 
	 * If given, the password will not be looked up in settings.xml.
	 * 
	 * @since 1.0
	 * @parameter property="password"
	 */
	private String password;

	/**
	 * Property to which the password will be set. If not given, it will be set
	 * to <code>${settingsKey}.password</code> as key.
	 * 
	 * @since 1.0
	 * @parameter property="passwordProperty" default="password"
	 */
	private String passwordProperty;

	/**
	 * Whether to set system properties (instead of the default Maven project
	 * properties)
	 * 
	 * @since 1.0
	 * @parameter property="useSystemProperties" default-value="false"
	 */
	private boolean useSystemProperties;

	/**
	 * Server's <code>id</code> in <code>settings.xml</code> to look up username
	 * and password. Defaults to <code>${url}</code> if not given.
	 * 
	 * @since 1.0
	 * @parameter property="settingsKey"
	 */
	private String settingsKey;

	/**
	 * @parameter property="settings"
	 * @required
	 * @since 1.0
	 * @readonly
	 */
	private Settings settings;

	/**
	 * @since 1.0
	 * @component role=
	 *            "hidden.org.sonatype.plexus.components.sec.dispatcher.SecDispatcher"
	 * @required
	 */
	private SecDispatcher securityDispatcher;

	/**
	 * The Maven Project Object
	 * 
	 * @since 1.0
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @since 1.0
	 * @parameter default-value="${session}"
	 * @required
	 * @readonly
	 */
	@SuppressWarnings("unused")
	private MavenSession mavenSession;

	// ////////////////////////////// Source info /////////////////////////////

	/**
	 * Load username and password from settings if user has not set them in JVM
	 * properties
	 * 
	 * @throws MojoExecutionException
	 */
	public void execute() throws MojoExecutionException {
		if ((username == null || password == null) && (settings != null)) {
			Server server = this.settings.getServer(this.settingsKey);

			if (server != null) {
				if (username == null) {
					username = server.getUsername();
				}

				if (password == null && server.getPassword() != null) {
					try {
						password = securityDispatcher.decrypt(server
								.getPassword());
					} catch (SecDispatcherException e) {
						throw new MojoExecutionException(e.getMessage());
					}
				}
			}
		}

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}

		if (usernameProperty == null) {
			usernameProperty = settingsKey + "." + "username";
		}

		if (passwordProperty == null) {
			passwordProperty = settingsKey + "." + "password";
		}

		if (useSystemProperties) {
			System.setProperty(usernameProperty, username);
			System.setProperty(passwordProperty, password);
		} else {
			project.getProperties().setProperty(usernameProperty, username);
			project.getProperties().setProperty(passwordProperty, password);
		}
	}

	// Getters and setters for the sake of unit tests
	@Deprecated
	String getUsername() {
		return this.username;
	}

	@Deprecated
	void setUsername(String username) {
		this.username = username;
	}

	final String getUsernameProperty() {
		return usernameProperty;
	}

	final void setUsernameProperty(String usernameProperty) {
		this.usernameProperty = usernameProperty;
	}

	@Deprecated
	String getPassword() {
		return this.password;
	}

	@Deprecated
	void setPassword(String password) {
		this.password = password;
	}

	final String getPasswordProperty() {
		return passwordProperty;
	}

	final void setPasswordProperty(String passwordProperty) {
		this.passwordProperty = passwordProperty;
	}

	final boolean isUseSystemProperties() {
		return useSystemProperties;
	}

	final void setUseSystemProperties(boolean useSystemProperties) {
		this.useSystemProperties = useSystemProperties;
	}

	void setSettings(Settings settings) {
		this.settings = settings;
	}

	void setSettingsKey(String key) {
		this.settingsKey = key;
	}

	public void setSecurityDispatcher(SecDispatcher securityDispatcher) {
		this.securityDispatcher = securityDispatcher;
	}
}
