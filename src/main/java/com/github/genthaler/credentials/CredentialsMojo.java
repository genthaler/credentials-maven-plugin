package com.github.genthaler.credentials;

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
 * This plugin looks up credentials in ~/.m2/settings.xml, and sets them to
 * specifiable Maven and/or Java system properties.
 * 
 * Also provides the ability to set the username and password via system
 * properties. This is to support situations where the credentials may not be
 * available in all execution environments, such as continuous integration
 * servers.
 * 
 * If the computed {@link #usernameProperty} and/or {@link #passwordProperty}
 * already exist as either Maven or system properties, neither will be looked up
 * in settings.xml, since it's assumed that these have been deliberately set in
 * order to override the pom.xml settings.
 * 
 * @goal set
 * @phase validate
 * @threadSafe
 */
public class CredentialsMojo extends AbstractMojo {

	/**
	 * Property to which the username will be set. If not given as a system
	 * parameter, it will be set to <code>${settingsKey}.username</code>.
	 * 
	 * @since 1.0
	 * @parameter property="credentials.usernameProperty"
	 * @required="false"
	 */
	private String usernameProperty;

	/**
	 * Property to which the password will be set. If not given as a system
	 * parameter, it will be set to <code>${settingsKey}.password</code> as key.
	 * 
	 * @since 1.0
	 * @parameter property="credentials.passwordProperty"
	 * @required="false"
	 */
	private String passwordProperty;

	/**
	 * Whether to set system properties (as well as the default Maven project
	 * properties). This is to support situations where it's not possible or
	 * convenient to propagate Maven properties. An example is the
	 * maven-antrun-plugin, where only certain Maven properties are propagated
	 * into the Ant project context.
	 * 
	 * @since 1.0
	 * @parameter property="credentials.useSystemProperties"
	 *            default-value="false"
	 * @required="false"
	 */
	private boolean useSystemProperties;

	/**
	 * Server's <code>id</code> in <code>settings.xml</code> to look up
	 * username/password credentials. Defaults to <code>${url}</code> if not
	 * given.
	 * 
	 * @since 1.0
	 * @parameter property="credentials.settingsKey"
	 * @required="false"
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
	 * @component role-hint="mojo"
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
	 * Execute the mojo.
	 * 
	 * Requirements:
	 * 
	 * Do not overwrite any existing Maven or system property.
	 * 
	 * @throws MojoExecutionException
	 */
	public void execute() throws MojoExecutionException {

		if (null == usernameProperty) {
			if (null == settingsKey)
				throw new MojoExecutionException(
						"At least one of settingsKey and usernameProperty must be set");
			usernameProperty = settingsKey + "." + "username";
		}

		if (null == passwordProperty) {
			if (null == settingsKey)
				throw new MojoExecutionException(
						"At least one of settingsKey and passwordProperty must be set");
			passwordProperty = settingsKey + "." + "password";
		}

		// Only lookup credentials if they're not already set.
		if (null == project.getProperties().getProperty(usernameProperty)
				|| null == project.getProperties()
						.getProperty(passwordProperty)
				&& (!useSystemProperties || null == System
						.getProperty(usernameProperty)
						&& null == System.getProperty(passwordProperty))) {

			if (null == settingsKey) {
				throw new MojoExecutionException(
						String.format(
								"If %s/%s properties not set manually, then the settings key must be specified, either at the command line or in the pom.xml",
								usernameProperty, passwordProperty));
			}
			Server server = this.settings.getServer(this.settingsKey);

			String username = null;
			String password = null;

			if (null == server) {
				throw new MojoExecutionException(
						String.format(
								"You have specified a settingsKey property value of %s, there must be a server entry with id %s in your settings.xml",
								settingsKey, settingsKey));
			} else {
				username = server.getUsername();
				password = server.getPassword();

				try {
					password = securityDispatcher.decrypt(password);
				} catch (SecDispatcherException e) {
					// Don't care if we can't decrypt, either it wasn't
					// encrypted in the first place, and/or it'll just fail
					// on the target system.
					getLog().warn(e);
				}
			}

			if (null == username) {
				username = "";
			}

			if (null == password) {
				password = "";
			}

			project.getProperties().setProperty(usernameProperty, username);
			project.getProperties().setProperty(passwordProperty, password);

			if (useSystemProperties) {
				System.setProperty(usernameProperty, username);
				System.setProperty(passwordProperty, password);
			}
		}
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
