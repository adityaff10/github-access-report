package com.cloudeagle.githubaccess.service;

import com.cloudeagle.githubaccess.exception.GitHubApiException;
import com.cloudeagle.githubaccess.model.AccessReport;
import com.cloudeagle.githubaccess.model.GitHubCollaborator;
import com.cloudeagle.githubaccess.model.GitHubRepo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class AccessReportService {

	private final GitHubApiClient gitHubApiClient;
	private final ExecutorService githubExecutorService;

	public AccessReportService(GitHubApiClient gitHubApiClient, ExecutorService githubExecutorService) {
		this.gitHubApiClient = gitHubApiClient;
		this.githubExecutorService = githubExecutorService;
	}

	private static final Logger log = LoggerFactory.getLogger(AccessReportService.class);

	public AccessReport generateReport(String org) {
		log.info("Starting report generation for org: {}", org);
		long start = System.currentTimeMillis();

		List<GitHubRepo> repos = gitHubApiClient.fetchAllRepositories(org);
		log.info("Found {} repositories in org: {}", repos.size(), org);

		Map<GitHubRepo, Future<List<GitHubCollaborator>>> futures = new LinkedHashMap<>();
		for (GitHubRepo repo : repos) {
			Future<List<GitHubCollaborator>> future = (Future<List<GitHubCollaborator>>) githubExecutorService
					.submit(() -> gitHubApiClient.fetchCollaborators(org, repo.getName()));
			futures.put(repo, future);
		}

		Map<String, List<AccessReport.UserRepoAccess>> userAccessMap = new TreeMap<>();

		for (Map.Entry<GitHubRepo, Future<List<GitHubCollaborator>>> entry : futures.entrySet()) {
			GitHubRepo repo = entry.getKey();
			try {
				List<GitHubCollaborator> collaborators = entry.getValue().get(30, TimeUnit.SECONDS);

				for (GitHubCollaborator collaborator : collaborators) {

					String accessLevel = resolveAccessLevel(collaborator);

					AccessReport.UserRepoAccess repoAccess = new AccessReport.UserRepoAccess();
					repoAccess.setRepositoryName(repo.getName());
					repoAccess.setFullName(repo.getFullName());
					repoAccess.setAccessLevel(accessLevel);
					repoAccess.setPrivate(repo.isPrivate());
					repoAccess.setRepositoryUrl(repo.getHtmlUrl());

					userAccessMap.computeIfAbsent(collaborator.getLogin(), k -> new ArrayList<>()).add(repoAccess);
				}

			} catch (TimeoutException e) {
				log.warn("Timed out fetching collaborators for: {}. Skipping.", repo.getName());
				entry.getValue().cancel(true);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new GitHubApiException("Interrupted while fetching collaborators", e);
			} catch (ExecutionException e) {
				log.error("Error for repo {}: {}", repo.getName(), e.getCause().getMessage());
			}
		}

		userAccessMap.values()
				.forEach(list -> list.sort(Comparator.comparing(AccessReport.UserRepoAccess::getRepositoryName)));

		long elapsed = System.currentTimeMillis() - start;
		log.info("Report done in {}ms — {} repos, {} users", elapsed, repos.size(), userAccessMap.size());

		AccessReport report = new AccessReport();
		report.setOrganization(org);
		report.setTotalRepositories(repos.size());
		report.setTotalUsers(userAccessMap.size());
		report.setGeneratedAt(Instant.now());
		report.setUserAccessMap(userAccessMap);
		return report;
	}

	private String resolveAccessLevel(GitHubCollaborator collaborator) {
		if (collaborator.getPermissions() != null) {
			return collaborator.getPermissions().getHighestRole();
		}
		return collaborator.getRoleName() != null ? collaborator.getRoleName() : "unknown";
	}
}