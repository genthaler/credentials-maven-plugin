@unit-test @set-all
Feature: Unit test of credentials-maven-plugin:set-all
  In order to expose all username and password properties in the current user's Maven settings
  As a Maven developer
  I want to execute a credentials maven plugin which will populate them from server settings or command line properties

  Background:
    Given a SetAllMojo Mojo

  Scenario: No information
    Given Server ding's username property is whiz
    And Server ding's password property is bang
    And Server dong's username property is sing
    And Server dong's password property is song
    When the Mojo is executed
    Then the Project should have a ding.username property with value whiz
    And the Project should have a ding.password property with value bang
    And the Project should have a dong.username property with value sing
    And the Project should have a dong.password property with value song
