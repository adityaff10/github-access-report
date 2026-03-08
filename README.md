# GitHub Organization Access Report

A Spring Boot service that connects to GitHub and generates a report showing which users have access to which repositories within a given organization.

---

## How to Run the Project

### Prerequisites
- Java 21
- Maven 3.6+
- A GitHub Personal Access Token (classic)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/adityaff10/github-access-report.git
   cd github-access-report
   ```

2. **Set the environment variable**

   On Windows (Command Prompt):
   ```cmd
   set GITHUB_TOKEN=your_token_here
   ```
   On Mac/Linux:
   ```bash
   export GITHUB_TOKEN=your_token_here
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   Or import into STS/IntelliJ and run as a Spring Boot App.

4. The service starts on **port 9090**.

---

## How Authentication is Configured

A GitHub Personal Access Token (classic) is required with the following scopes:
- `repo` — Full control of repositories
- `read:org` — Read org and team membership

The token is loaded from the `GITHUB_TOKEN` environment variable via `application.properties`:

```properties
github.token=${GITHUB_TOKEN}
```

It is injected into every outgoing request as a Bearer token through a `RestTemplate` interceptor in `GitHubConfig.java` — no manual header setup is needed per request.

---

## How to Call the API Endpoint

### Endpoint
```
GET /api/v1/access-report?org={orgName}
```

### Example Request
```
GET http://localhost:9090/api/v1/access-report?org=guardian
```

### Example Response
```json
{
  "generatedAt": "2026-03-09T00:49:34.144830100Z",
  "organization": "guardian",
  "totalRepositories": 2050,
  "totalUsers": 85,
  "userAccessMap": {
    "johndoe": [
      {
        "repositoryName": "frontend",
        "fullName": "guardian/frontend",
        "accessLevel": "write",
        "private": false,
        "repositoryUrl": "https://github.com/guardian/frontend"
      }
    ]
  }
}
```

### Swagger UI
Interactive API documentation is available at:
```
http://localhost:9090/swagger-ui.html
```

---

## Assumptions and Design Decisions

### Parallel Fan-out for Scale
For each repository fetched, a collaborator-fetch task is submitted to a **20-thread `ExecutorService`** concurrently. This ensures the service handles organizations with 100+ repositories efficiently.

### Full Pagination
Both the repository list and collaborator list endpoints follow GitHub's `Link` header (`rel="next"`) until there are no more pages. Page size is set to 100 (GitHub's maximum) to minimize the total number of API calls.

### Graceful Degradation
If an individual repository returns `403 Forbidden` or `404 Not Found` when fetching collaborators (e.g., due to token permission restrictions), that repository is logged and skipped — the report continues for all remaining repositories rather than failing the entire request.

### Error Handling
- Missing or invalid `org` parameter → `400 Bad Request`
- Organization not found → `404 Not Found`
- GitHub API failures → `502 Bad Gateway`
- Unexpected errors → `500 Internal Server Error`

### Note
The GitHub collaborators API requires the token owner to be a member of the queried organization. For external public organizations, repository data is fetched successfully, but collaborator data may be restricted by GitHub's API permissions. Use an organization you are a member of for full results.

---

## Project Structure

```
src/main/java/com/cloudeagle/githubaccess/
├── GitHubAccessReportApplication.java
├── config/                                 # RestTemplate and thread pool setup
│   └── GitHubConfig.java               
├── controller/                             # API endpoint
│   └── AccessReportController.java     
├── exception/                              # Custom exceptions and error handler
│   ├── OrganizationNotFoundException.java
│   ├── GitHubApiException.java
│   └── GlobalExceptionHandler.java     
├── model/                                  # Request/response models
│   ├── GitHubRepo.java
│   ├── GitHubCollaborator.java
│   └── AccessReport.java               
└── service/                                # GitHub API calls and report generation
    ├── GitHubApiClient.java            
    └── AccessReportService.java        
```
