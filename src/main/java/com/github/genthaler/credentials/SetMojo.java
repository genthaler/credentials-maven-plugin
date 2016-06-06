package com.github.genthaler.credentials;

/*
 * #%L
 * Credentials Maven Plugin
 * %%
 * Copyright (C) 2013 - 2016 Günther Enthaler
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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * This plugin looks up credentials in <code>~/.m2/settings.xml</code>, and sets
 * them to specifiable Maven and/or Java system properties.
 * 
 * Also provides the ability to set the username and password via system
 * properties. This is to support situations where the credentials may not be
 * available in all execution environments, such as continuous integration
 * servers.
 * 
 * If the computed {@link #usernameProperty} and/or {@link #passwordProperty}
 * already exist as either Maven or system properties, neither will be looked up
 * in <code>~/.m2/settings.xml</code>, since it's assumed that these have been
 * deliberately set in order to override the <code>settings.xml</code> settings.
 */
@Mojo(name = "set", requiresProject = true, defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresDirectInvocation = false)
public class SetMojo extends AbstractCredentialsMojo {

	/**
	 * Property to which the username will be set. If not given, it will be set
	 * to <code>${settingsKey}.username</code>.
	 */

	@Parameter(property = "credentials.usernameProperty", required = false)
	private String usernameProperty;

	/**
	 * Property to which the password will be set. If not given, it will be set
	 * to <code>${settingsKey}.password</code>.
	 */
	@Parameter(property = "credentials.passwordProperty", required = false)
	private String passwordProperty;

	/**
	 * Server <code>id</code> in <code>~/.m2/settings.xml</code> to look up
	 * username/password credentials.
	 */
	@Parameter(property = "credentials.settingsKey", required = false)
	private String settingsKey;

	/**
	 * Execute the mojo.
	 * 
	 * @throws MojoExecutionException
	 */
	@Override
	public void execute() throws MojoExecutionException {
		Log log = getLog();
		boolean debugEnabled = log.isDebugEnabled();

		if (null == usernameProperty) {
			if (null == settingsKey)
				throw new MojoExecutionException(
						"At least one of settingsKey and usernameProperty must be set");
			usernameProperty = settingsKey + "."
					+ DEFAULT_USERNAME_PROPERTY_SUFFIX;
		}

		if (debugEnabled)
			log.debug(String.format("usernameProperty is %s", usernameProperty));

		if (null == passwordProperty) {
			if (null == settingsKey)
				throw new MojoExecutionException(
						"At least one of settingsKey and passwordProperty must be set");
			passwordProperty = settingsKey + "."
					+ DEFAULT_PASSWORD_PROPERTY_SUFFIX;
		}

		if (debugEnabled)
			log.debug(String.format("passwordProperty is %s", passwordProperty));

		String username = coalesce(System.getProperty(usernameProperty),
				project.getProperties().getProperty(usernameProperty));

		if (debugEnabled)
			log.debug(String.format("username so far is %s", username));

		String password = coalesce(System.getProperty(passwordProperty),
				project.getProperties().getProperty(passwordProperty));

		if (debugEnabled)
			log.debug(String.format("password so far is %s", password));

		// Only lookup credentials if they're not already set.
		if (null == username || null == password) {
			if (null == settingsKey) {
				throw new MojoExecutionException(
						String.format(
								"If %s/%s properties not set manually, then the settings key must be specified, either at the command line or in the pom.xml",
								usernameProperty, passwordProperty));
			}

			Server server = this.settings.getServer(this.settingsKey);

			if (null == server) {
				throw new MojoExecutionException(
						String.format(
								"You have specified a settingsKey property value of %s, there must be a server entry with id %s in your settings.xml",
								settingsKey, settingsKey));
			} else {
				if (null == username)
					username = server.getUsername();

				if (null == password) {
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
			}
		}

		if (null == username)
			username = "";
		if (null == password)
			password = "";

		project.getProperties().setProperty(usernameProperty, username);
		project.getProperties().setProperty(passwordProperty, password);

		if (useSystemProperties) {
			System.setProperty(usernameProperty, username);
			System.setProperty(passwordProperty, password);
		}

		if (debugEnabled)
			log.debug(String.format("username property '%s' is '%s'",
					usernameProperty, username));

		if (debugEnabled)
			log.debug(String.format("password property '%s' is '%s'",
					passwordProperty, password));
	}
}
