package com.cloudeagle.githubaccess.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.cloudeagle.githubaccess.exception.GitHubApiException;
import com.cloudeagle.githubaccess.exception.OrganizationNotFoundException;
import com.cloudeagle.githubaccess.model.GitHubCollaborator;
import com.cloudeagle.githubaccess.model.GitHubRepo;

@Service
public class GitHubApiClient {

	private final RestTemplate restTemplate;

	public GitHubApiClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static final Logger log = LoggerFactory.getLogger(GitHubApiClient.class);
	
	@Value("${github.api.base-url}")
	private String baseUrl;

	@Value("${github.api.page-size:100}")
	private int pageSize;

	private static final Pattern NEXT_PAGE_PATTERN = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");

	public List<GitHubRepo> fetchAllRepositories(String org) {
		String url = baseUrl + "/orgs/" + org + "/repos?type=all&per_page=" + pageSize;
		log.info("Fetching repositories for org: {}", org);
		try {
			return fetchAllPages(url, new ParameterizedTypeReference<List<GitHubRepo>>() {
			});
		} catch (HttpClientErrorException.NotFound e) {
			throw new OrganizationNotFoundException(org);
		} catch (HttpClientErrorException e) {
			throw new GitHubApiException("Failed to fetch repositories: HTTP " + e.getStatusCode());
		}
	}

	public List<GitHubCollaborator> fetchCollaborators(String org, String repoName) {
		String url = baseUrl + "/repos/" + org + "/" + repoName + "/collaborators?affiliation=all&per_page=" + pageSize;
		log.debug("Fetching collaborators for repo: {}/{}", org, repoName);
		try {
			return fetchAllPages(url, new ParameterizedTypeReference<List<GitHubCollaborator>>() {
			});
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
				log.warn("No permission for collaborators in {}/{}. Skipping.", org, repoName);
				return Collections.emptyList();
			}
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				log.warn("Repo {}/{} not found. Skipping.", org, repoName);
				return Collections.emptyList();
			}
			throw new GitHubApiException("Failed to fetch collaborators for " + org + "/" + repoName);
		}
	}

	private <T> List<T> fetchAllPages(String firstUrl, ParameterizedTypeReference<List<T>> typeRef) {
		List<T> allItems = new ArrayList<>();
		String nextUrl = firstUrl;

		while (nextUrl != null) {
			ResponseEntity<List<T>> response = restTemplate.exchange(nextUrl, HttpMethod.GET, null, typeRef);

			List<T> body = response.getBody();
			if (body != null) {
				allItems.addAll(body);
			}

			nextUrl = extractNextUrl(response);
		}

		return allItems;
	}

	private <T> String extractNextUrl(ResponseEntity<T> response) {
		List<String> linkHeaders = response.getHeaders().get("Link");
		if (linkHeaders == null || linkHeaders.isEmpty())
			return null;

		for (String header : linkHeaders) {
			Matcher matcher = NEXT_PAGE_PATTERN.matcher(header);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}
