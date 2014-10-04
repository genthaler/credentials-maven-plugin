@set-all
Feature: Test of credentials-maven-plugin:set-all
  In order to expose username and password properties
  As a Maven developer
  I want to execute a credentials maven plugin which will populate them from server settings or command line properties

  Scenario: Set all known credentials to properties
    Given the Plugin useSystemProperties property is true
    Given Server ding's username is whiz
    And Server ding's password is bang
    And Server dong's username is sing
    And Server dong's password is song
    When the set-all Goal is executed
    Then there should be a Project ding.username property with value whiz
    And there should be a Project ding.password property with value bang
    And there should be a System ding.username property with value whiz
    And there should be a System ding.password property with value bang
    Then there should be a Project dong.username property with value sing
    And there should be a Project dong.password property with value song
    And there should be a System dong.username property with value sing
    And there should be a System dong.password property with value song
