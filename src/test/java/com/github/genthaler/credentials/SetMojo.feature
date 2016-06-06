@set
Feature: Test of credentials-maven-plugin:set
  In order to expose username and password properties
  As a Maven developer
  I want to execute a credentials maven plugin which will populate them from server settings or command line properties

  Background:
    Given a set Plugin

  Scenario: No information
    When the set Goal is executed
    Then there should be an error message 'At least one of settingsKey and usernameProperty must be set'

  Scenario: passwordProperty & no usernameProperty
    Given the Plugin passwordProperty property is bar
    When the set Goal is executed
    Then there should be an error message 'At least one of settingsKey and usernameProperty must be set'

  Scenario: usernameProperty & no passwordProperty
    Given the Plugin usernameProperty property is foo
    When the set Goal is executed
    Then there should be an error message 'At least one of settingsKey and passwordProperty must be set'

  Scenario: usernameProperty & passwordProperty
    Given the Plugin usernameProperty property is foo
    And the Plugin passwordProperty property is bar
    When the set Goal is executed
    Then there should be an error message 'If foo/bar properties not set manually, then the settings key must be specified, either at the command line or in the pom.xml'

  Scenario: usernameProperty & passwordProperty & settingsKey but no corresponding settings
    Given the Plugin usernameProperty property is foo
    And the Plugin passwordProperty property is bar
    And the Plugin settingsKey property is blah
    When the set Goal is executed
    Then there should be an error message 'You have specified a settingsKey property value of blah, there must be a server entry with id blah in your settings.xml'

  Scenario: usernameProperty & passwordProperty & settingsKey with corresponding settings
    Given the Plugin usernameProperty property is foo
    And the Plugin passwordProperty property is bar
    And the Plugin settingsKey property is blah
    And Server blah's username is whiz
    And Server blah's password is bang
    When the set Goal is executed
    Then there should be a Project foo property with value whiz
    And there should be a Project bar property with value bang

  Scenario: settingsKey only with corresponding settings
    Given the Plugin settingsKey property is blah
    And Server blah's username is whiz
    And Server blah's password is bang
    When the set Goal is executed
    Then there should be a Project blah.username property with value whiz
    And there should be a Project blah.password property with value bang

  Scenario: settingsKey only with corresponding settings and encrypted password
    Given the Plugin settingsKey property is blah
    And Server blah's username is me
    And Server blah's password is {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}
    And the System settings.security property is src/it/ant-secure/settings-security.xml
    When the set Goal is executed
    Then there should be a Project blah.username property with value me
    And there should be a Project blah.password property with value mine

  Scenario: settingsKey only with corresponding settings and encrypted password but no decryption key
    Given the Plugin settingsKey property is blah
    And Server blah's username is me
    And Server blah's password is {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}
    When the set Goal is executed
    Then there should be a Project blah.username property with value me
    And there should be a Project blah.password property with value {iFtD2TFFjzoHEDN1RxW21zEBYW0Gt7GwbsOm6yDS63s=}

  Scenario: settingsKey only with SSH style server entry i.e. no username or password
    Given the Plugin settingsKey property is blah
    And an empty Server with id blah
    When the set Goal is executed
    Then there should be a Project blah.username property with value ""
    And there should be a Project blah.password property with value ""

  Scenario: settingsKey only with server entry with username but no password
    Given the Plugin settingsKey property is blah
    And Server blah's username is me
    When the set Goal is executed
    Then there should be a Project blah.username property with value me
    And there should be a Project blah.password property with value ""

  Scenario: settingsKey only with corresponding settings and using System properties
    Given the Plugin settingsKey property is blah
    And the Plugin useSystemProperties property is true
    And Server blah's username is whiz
    And Server blah's password is bang
    When the set Goal is executed
    Then there should be a Project blah.username property with value whiz
    And there should be a Project blah.password property with value bang
    And there should be a System blah.username property with value whiz
    And there should be a System blah.password property with value bang

  Scenario: usernameProperty & passwordProperty & settingsKey with corresponding settings AND previously existing properties, the existing properties should win
    Given the Plugin usernameProperty property is foo
    And the Plugin passwordProperty property is bar
    And the Plugin settingsKey property is blah
    And Server blah's username is whiz
    And Server blah's password is bang
    And the Project foo property is ping
    And the Project bar property is pong
    When the set Goal is executed
    Then there should be a Project foo property with value ping
    And there should be a Project bar property with value pong
