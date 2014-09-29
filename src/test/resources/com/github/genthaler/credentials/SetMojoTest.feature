@unit-test @set
Feature: Unit test of credentials-maven-plugin:set
  In order to expose username and password properties
  As a Maven developer
  I want to execute a credentials maven plugin which will populate them from server settings or command line properties

  Background:
    Given a SetMojo Mojo

  Scenario: No information
    When the Mojo is executed
    Then a MojoExecutionException should be thrown with the message 'At least one of settingsKey and usernameProperty must be set'

  Scenario: passwordProperty & no usernameProperty
    Given the Mojo's passwordProperty property is bar
    When the Mojo is executed
    Then a MojoExecutionException should be thrown with the message 'At least one of settingsKey and usernameProperty must be set'

  Scenario: usernameProperty & no passwordProperty
    Given the Mojo's usernameProperty property is foo
    When the Mojo is executed
    Then a MojoExecutionException should be thrown with the message 'At least one of settingsKey and passwordProperty must be set'

  Scenario: usernameProperty & passwordProperty
    Given the Mojo's usernameProperty property is foo
    And the Mojo's passwordProperty property is bar
    When the Mojo is executed
    Then a MojoExecutionException should be thrown with the message 'If foo/bar properties not set manually, then the settings key must be specified, either at the command line or in the pom.xml'

  Scenario: usernameProperty & passwordProperty & settingsKey but no corresponding settings
    Given the Mojo's usernameProperty property is foo
    And the Mojo's passwordProperty property is bar
    And the Mojo's settingsKey property is blah
    When the Mojo is executed
    Then a MojoExecutionException should be thrown with the message 'You have specified a settingsKey property value of blah, there must be a server entry with id blah in your settings.xml'

  Scenario: usernameProperty & passwordProperty & settingsKey with corresponding settings
    Given the Mojo's usernameProperty property is foo
    And the Mojo's passwordProperty property is bar
    And the Mojo's settingsKey property is blah
    And Server blah's username property is whiz
    And Server blah's password property is bang
    When the Mojo is executed
    Then the Project should have a foo property with value whiz
    And the Project should have a bar property with value bang

  Scenario: settingsKey only with corresponding settings
    Given the Mojo's settingsKey property is blah
    And Server blah's username property is whiz
    And Server blah's password property is bang
    When the Mojo is executed
    Then the Project should have a blah.username property with value whiz
    And the Project should have a blah.password property with value bang

  Scenario: settingsKey only with corresponding settings and encrypted password
    Given the Mojo's settingsKey property is blah
    And Server blah's username property is me
    And Server blah's password property is {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}
    And the settings.security System property is src/it/ant-secure/settings-security.xml
    When the Mojo is executed
    Then the Project should have a blah.username property with value me
    And the Project should have a blah.password property with value mine

  Scenario: settingsKey only with corresponding settings and encrypted password but no decryption key
    Given the Mojo's settingsKey property is blah
    And Server blah's username property is me
    And Server blah's password property is {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}
    When the Mojo is executed
    Then the Project should have a blah.username property with value me
    And the Project should have a blah.password property with value {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}

  Scenario: settingsKey only with SSH style server entry i.e. no username or password
    Given the Mojo's settingsKey property is blah
    And an empty Server with id blah
    When the Mojo is executed
    Then the Project should have a blah.username property with value ""
    And the Project should have a blah.password property with value ""

  Scenario: settingsKey only with server entry with username but no password
    Given the Mojo's settingsKey property is blah
    And Server blah's username property is me
    When the Mojo is executed
    Then the Project should have a blah.username property with value me
    And the Project should have a blah.password property with value ""

  Scenario: settingsKey only with corresponding settings and using System properties
    Given the Mojo's settingsKey property is blah
    And the Mojo's useSystemProperties property is true
    And Server blah's username property is whiz
    And Server blah's password property is bang
    When the Mojo is executed
    Then the Project should have a blah.username property with value whiz
    And the Project should have a blah.password property with value bang
    And the System should have a blah.username property with value whiz
    And the System should have a blah.password property with value bang

  Scenario: usernameProperty & passwordProperty & settingsKey with corresponding settings AND previously existing properties, the existing properties should win
    Given the Mojo's usernameProperty property is foo
    And the Mojo's passwordProperty property is bar
    And the Mojo's settingsKey property is blah
    And Server blah's username property is whiz
    And Server blah's password property is bang
    And the Project's foo property is ping
    And the Project's bar property is pong
    When the Mojo is executed
    Then the Project should have a foo property with value ping
    And the Project should have a bar property with value pong
