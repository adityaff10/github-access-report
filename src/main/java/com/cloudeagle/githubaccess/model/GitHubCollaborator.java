package com.cloudeagle.githubaccess.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCollaborator {

	@JsonProperty("login")
	private String login;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("html_url")
	private String htmlUrl;

	@JsonProperty("role_name")
	private String roleName;

	@JsonProperty("permissions")
	private Permissions permissions;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Permissions {

		@JsonProperty("admin")
		private boolean admin;

		@JsonProperty("maintain")
		private boolean maintain;

		@JsonProperty("push")
		private boolean push;

		@JsonProperty("triage")
		private boolean triage;

		@JsonProperty("pull")
		private boolean pull;

		public boolean isAdmin() {
			return admin;
		}

		public void setAdmin(boolean admin) {
			this.admin = admin;
		}

		public boolean isMaintain() {
			return maintain;
		}

		public void setMaintain(boolean maintain) {
			this.maintain = maintain;
		}

		public boolean isPush() {
			return push;
		}

		public void setPush(boolean push) {
			this.push = push;
		}

		public boolean isTriage() {
			return triage;
		}

		public void setTriage(boolean triage) {
			this.triage = triage;
		}

		public boolean isPull() {
			return pull;
		}

		public void setPull(boolean pull) {
			this.pull = pull;
		}

		public String getHighestRole() {
			if (admin)
				return "admin";
			if (maintain)
				return "maintain";
			if (push)
				return "write";
			if (triage)
				return "triage";
			if (pull)
				return "read";
			return "none";
		}
	}
	
}
