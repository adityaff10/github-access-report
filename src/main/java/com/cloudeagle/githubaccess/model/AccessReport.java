package com.cloudeagle.githubaccess.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AccessReport {

	private String organization;
	private int totalRepositories;
	private int totalUsers;
	private Instant generatedAt;
	private Map<String, List<UserRepoAccess>> userAccessMap;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public int getTotalRepositories() {
		return totalRepositories;
	}

	public void setTotalRepositories(int totalRepositories) {
		this.totalRepositories = totalRepositories;
	}

	public int getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}

	public Instant getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(Instant generatedAt) {
		this.generatedAt = generatedAt;
	}

	public Map<String, List<UserRepoAccess>> getUserAccessMap() {
		return userAccessMap;
	}

	public void setUserAccessMap(Map<String, List<UserRepoAccess>> userAccessMap) {
		this.userAccessMap = userAccessMap;
	}

	public static class UserRepoAccess {

		private String repositoryName;
		private String fullName;
		private String accessLevel;
		private boolean isPrivate;
		private String repositoryUrl;

		public String getRepositoryName() {
			return repositoryName;
		}

		public void setRepositoryName(String repositoryName) {
			this.repositoryName = repositoryName;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getAccessLevel() {
			return accessLevel;
		}

		public void setAccessLevel(String accessLevel) {
			this.accessLevel = accessLevel;
		}

		public boolean isPrivate() {
			return isPrivate;
		}

		public void setPrivate(boolean isPrivate) {
			this.isPrivate = isPrivate;
		}

		public String getRepositoryUrl() {
			return repositoryUrl;
		}

		public void setRepositoryUrl(String repositoryUrl) {
			this.repositoryUrl = repositoryUrl;
		}
	}
	
}