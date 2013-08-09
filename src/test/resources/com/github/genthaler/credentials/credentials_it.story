Exercise credentials

Meta:
@category pom
@color red

Narrative: 

In order to expose username and password properties 
As a Maven developer
I want to execute a credentials maven plugin which will populate them from server settings or command line properties

Scenario: No information

Given no credentials plugin configuration
When the project is executed
Then an exception should be thrown with the message 'At least one of settingsKey and usernameProperty must be set'

Scenario: Given passwordProperty & no usernameProperty

Given the credentials-maven-plugin passwordProperty property is bar
When the project is executed
Then an exception should be thrown with the message 'At least one of settingsKey and usernameProperty must be set'

Scenario: Given usernameProperty & no passwordProperty

Given the credentials-maven-plugin usernameProperty property is foo
When the project is executed
Then an exception should be thrown with the message 'At least one of settingsKey and passwordProperty must be set'

Scenario: Given usernameProperty & passwordProperty

Given the credentials-maven-plugin usernameProperty property is foo
And the credentials-maven-plugin passwordProperty property is bar
When the project is executed
Then an exception should be thrown with the message 'If foo/bar properties not set manually, then the settings key must be specified, either at the command line or in the pom.xml'

Scenario: Given usernameProperty & passwordProperty & settingsKey but no corresponding settings

Given the credentials-maven-plugin usernameProperty property is foo
And the credentials-maven-plugin passwordProperty property is bar
And the credentials-maven-plugin settingsKey property is blah
When the project is executed
Then an exception should be thrown with the message 'You have specified a settingsKey property value of blah, there must be a server entry with id blah in your settings.xml'

Scenario: Given usernameProperty & passwordProperty & settingsKey with corresponding settings

Given the credentials-maven-plugin usernameProperty property is foo
And the credentials-maven-plugin passwordProperty property is bar
And the credentials-maven-plugin settingsKey property is blah
And the blah Server username is whiz
And the blah Server password is bang
When the project is executed
Then there should have been a Project foo property with value whiz
And there should have been a Project bar property with value bang

Scenario: Given settingsKey only with corresponding settings

Given the credentials-maven-plugin settingsKey property is blah
And the blah Server username is whiz
And the blah Server password is bang
When the project is executed
Then there should have been a Project blah.username property with value whiz
And there should have been a Project blah.password property with value bang

Scenario: Given settingsKey only with corresponding settings and using System properties

Given the credentials-maven-plugin settingsKey property is blah
And the credentials-maven-plugin useSystemProperties property is true
And the blah Server username is whiz
And the blah Server password is bang
When the project is executed
Then there should have been a Project blah.username property with value whiz
And there should have been a Project blah.password property with value bang
And there should have been a System blah.username property with value whiz
And there should have been a System blah.password property with value bang
