@integration-test @set-all
Feature: Integration test of credentials-maven-plugin:set-all
  In order to expose username and password properties
  As a Maven developer
  I want to execute a credentials maven plugin which will populate them from server settings or command line properties

  Scenario: Set all known credentials to properties
    Given the credentials-maven-plugin useSystemProperties property is true
    Given the ding Server username is whiz
    And the ding Server password is bang
    And the dong Server username is sing
    And the dong Server password is song
    When the set-all goal is executed
    Then there should have been a Project ding.username property with value whiz
    And there should have been a Project ding.password property with value bang
    And there should have been a System ding.username property with value whiz
    And there should have been a System ding.password property with value bang
    Then there should have been a Project dong.username property with value sing
    And there should have been a Project dong.password property with value song
    And there should have been a System dong.username property with value sing
    And there should have been a System dong.password property with value song

