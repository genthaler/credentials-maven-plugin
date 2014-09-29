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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.settings.Server;

/**
 * This plugin looks up credentials in <code>~/.m2/settings.xml</code>, and sets
 * them all to Maven properties using the pattern <em>id</em> .username and
 * <em>id</em>.password.
 * 
 * Also provides the ability to set the username and password via system
 * properties. This is to support situations where the credentials may not be
 * available in all execution environments, such as continuous integration
 * servers.
 * 
 * If the computed <em>id</em>.username and <em>id</em>.password properties
 * already exist as either Maven or system properties, these will not be
 * overridden, since it's assumed that these have been deliberately set in order
 * to override the <code>settings.xml</code> settings.
 */
@Mojo(name = "set-all", requiresProject = true, defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, requiresDirectInvocation = false)
public class SetAllMojo extends AbstractCredentialsMojo {

	/**
	 * Execute the mojo.
	 * 
	 * @throws MojoExecutionException
	 */
	@Override
	public void execute() throws MojoExecutionException {
		Log log = getLog();
		boolean debugEnabled = log.isDebugEnabled();

		for (Server server : this.settings.getServers()) {
			String settingsKey = server.getId();
			String username = server.getUsername();
			String password = server.getPassword();

			String usernameProperty = settingsKey + "."
					+ DEFAULT_USERNAME_PROPERTY_SUFFIX;

			String passwordProperty = settingsKey + "."
					+ DEFAULT_PASSWORD_PROPERTY_SUFFIX;

			if (!project.getProperties().containsKey(usernameProperty))
				project.getProperties().setProperty(usernameProperty, username);
			if (!project.getProperties().containsKey(passwordProperty))
				project.getProperties().setProperty(passwordProperty, password);

			if (useSystemProperties) {
				if (System.getProperty(usernameProperty) == null)
					System.setProperty(usernameProperty, username);
				if (System.getProperty(passwordProperty) == null)
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
}
