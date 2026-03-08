package com.cloudeagle.githubaccess.controller;

import com.cloudeagle.githubaccess.model.AccessReport;
import com.cloudeagle.githubaccess.service.AccessReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Access Report", description = "GitHub Organization Access Report API")
public class AccessReportController {

	private final AccessReportService accessReportService;

	public AccessReportController(AccessReportService accessReportService) {
		this.accessReportService = accessReportService;
	}

	private static final Logger log = LoggerFactory.getLogger(AccessReportController.class);

	@GetMapping("/access-report")
	@Operation(
		    summary = "Generate GitHub Organization Access Report",
		    description = "Fetches all repositories in a GitHub organization and returns a user-centric " +
		                  "access map showing which repositories each user can access and their permission level. " +
		                  "Supports large organizations with 100+ repos via parallel API calls and full pagination."
		)
	public ResponseEntity<AccessReport> getAccessReport(@RequestParam String org) {
		log.info("Access report requested for org: {}", org);
		String cleanOrg = org.trim();
		AccessReport report = accessReportService.generateReport(cleanOrg);
		return ResponseEntity.ok(report);
	}
}