# NeuroForge Nexus — changes in this build

## Bug reporting
- Added `/bugs` in the signed-in application.
- Create reports with title, description, project/product, module/feature, environment, severity, priority and optional assignee.
- Reporter and creation time are populated by the backend from the authenticated session.
- Supports screenshots, screen recordings, logs, console logs and other files (15 MB/file limit).
- Files are stored under `./data/bug-attachments` by default; configure `neuroforge.upload-dir` to change the location. Keep this directory private and backed up.
- Bug reports and attachment downloads are scoped to the current organization.
- Workflow: New → Triaged → Assigned → In Progress → Fixed → Retest → Closed; failed retests can be Reopened and returned to In Progress.

## Organization isolation fixes
- Team directory results now filter loaded colleague records by the authenticated user's organization as a defense-in-depth check.
- User lookup authorization compares the Mongo user document ID with the requested ID (not the separate UUID `userId`).
- Task assignees must belong to the same organization as the project as well as be valid project members.
- Bug assignment validates organization membership and report lookups/downloads are organization-scoped.

## Build tool
The project uses Maven only. No additional build tool has been added.
- Run backend tests with `cd backend && ./mvnw test` (Windows: `mvnw.cmd test`).

## Notes
- Requires the project's configured MongoDB and AI/model settings as before.
- Attachment files are stored on the backend filesystem; in multi-instance deployments use shared object storage or a shared volume and configure access controls.
- The ZIP intentionally excludes `.git` history and `node_modules`; install frontend dependencies with `npm ci` in `frontend`.


## Jenkins CI/CD integration

- Added an organization-scoped Jenkins integration at **Settings → Jenkins CI/CD**.
- Configure the Jenkins base URL, username, API token, and optional default job.
- Use **Test connection** to verify that the backend can authenticate to the Jenkins API.
- Jenkins credentials are stored per organization; API tokens are never sent back to the frontend.
- The backend must be able to reach the Jenkins URL. Use HTTPS in production and a dedicated Jenkins account with least-privilege permissions.
- This version verifies connectivity; it does not automatically trigger builds or install/configure Jenkins pipelines.

## Sprint planning, releases, deployments and monitoring

### Project delivery workspace
- Each project has a **Sprints & DevOps** page at `/projects/{projectId}/delivery`.
- Sprints support name, goal, dates, status lifecycle (Planned, Active, Completed, Cancelled), task associations, progress summary and task list.
- A project can have only one active sprint. Dates are validated. Sprint task IDs are validated against both the current organization and the same project.
- Releases track version, title, notes, linked tasks and lifecycle (Draft, Ready, Released, Cancelled).
- Deployment records track environment, version, status, source, commit SHA, workflow URL, notes/logs, initiator and timestamps. Status changes are auditable in the record's current state.
- Production deployment records linked to a release require that release to be Ready or Released.
- These deployment entries are currently manual records; automatic GitHub Actions event ingestion/webhooks are not implemented in this change and must be added before treating the UI as an automated deployment source of truth.
- All sprint/release/deployment queries are scoped to the user's organization and project access. Create/update actions require project task-management permission.

### Prometheus and Grafana
- Added Micrometer's Prometheus registry and Actuator endpoint exposure for `health`, `info`, `metrics`, and `prometheus`.
- `/actuator/prometheus` is permitted for Prometheus scraping; restrict network exposure to trusted/local networks. Other Actuator endpoints remain authenticated by the application security chain.
- Docker Compose starts Prometheus on `127.0.0.1:9090` and Grafana on `127.0.0.1:3001`, with persistent volumes and a provisioned Prometheus datasource/dashboard.
- Prometheus targets `host.docker.internal:8080`, assuming the Spring Boot backend runs on the host at port 8080. Update `monitoring/prometheus/prometheus.yml` if your backend uses another host/port or runs inside Compose.
- Copy `.env.example` to `.env` and replace all sample passwords before running Compose. Never commit the real `.env` file.
- Start monitoring from the repository root with `docker compose up -d mongodb prometheus grafana`. Run the backend with its existing environment configuration. Visit Grafana at `http://localhost:3001` and sign in with the values from `.env`.
- Monitoring configuration is local-development oriented. Before production use, add TLS, secret management, authentication/network controls, retention policy, alert rules and verified deployment-event integration.
