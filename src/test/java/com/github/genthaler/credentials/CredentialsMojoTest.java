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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Unit test for CredentialsMojo.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class CredentialsMojoTest {

	private CredentialsMojoTestSteps steps;

	@Before
	public void setUp() throws Exception {
		steps = new CredentialsMojoTestSteps();
		steps.setUp();
	}

	@Test
	public void testNoInformation() {
		steps.emptyMojo();
		steps.executeMojo();
		steps.anExceptionIsThrown("MojoExecutionException",
				"At least one of settingsKey and usernameProperty must be set");
	}
}
