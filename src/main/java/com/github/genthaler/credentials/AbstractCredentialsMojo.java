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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public abstract class AbstractCredentialsMojo extends AbstractMojo {

	protected static final String DEFAULT_USERNAME_PROPERTY_SUFFIX = "username";
	protected static final String DEFAULT_PASSWORD_PROPERTY_SUFFIX = "password";

	/**
	 * Whether to set system properties (as well as the default Maven project
	 * properties). This is to support situations where it's not possible or
	 * convenient to propagate Maven properties. An example is the <a
	 * href="maven.apache.org/plugins/maven-antrun-plugin">Maven AntRun
	 * Plugin</a>, where only certain Maven properties are propagated into the
	 * Ant project context.
	 */
	@Parameter(property = "credentials.useSystemProperties", required = false)
	protected boolean useSystemProperties;

	@Parameter(readonly = true, required = true, property = "settings")
	protected Settings settings;

	@Parameter(required = true)
	@Component(hint = "mojo")
	protected SecDispatcher securityDispatcher;

	@Parameter(readonly = true, required = true, defaultValue = "${project}")
	protected MavenProject project;

	@Parameter(readonly = true, required = true, defaultValue = "${session}")
	protected MavenSession mavenSession;

	static String coalesce(String... values) {
		for (String value : values) {
			if (null != value)
				return value;
		}
		return null;
	}
}
