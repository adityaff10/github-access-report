package com.cloudeagle.githubaccess.exception;

public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException(String orgName) {
        super("GitHub organization not found: " + orgName);
    }
}