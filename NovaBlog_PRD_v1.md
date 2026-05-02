  
**NovaBlog**  
Multi-Tenant SaaS Blogging Platform

Product Requirements Document  ·  v1.0

| Prepared For | Antigravity (Development Partner) |
| :---- | :---- |
| **Product Owner** | TBD |
| **Document Date** | April 2025 |
| **Version** | 1.0 — Initial Release |
| **Status** | APPROVED FOR DEVELOPMENT |
| **Classification** | CONFIDENTIAL |

| SECTION 1 — EXECUTIVE SUMMARY |
| :---: |

# **1\. Executive Summary**

NovaBlog is a cloud-native, multi-tenant SaaS blogging platform that enables organizations, teams, and content creators to operate fully isolated blog workspaces under a single unified infrastructure. Each tenant owns their own branded environment, user roles, and content — with zero cross-tenant data leakage.

The platform is purpose-built to address three major gaps in the current market: (1) lack of true multi-tenancy in mainstream blogging tools, (2) absence of real-time collaborative authoring without third-party add-ons, and (3) no developer-first API access for headless deployments.

## **1.1 Vision Statement**

| *To become the most developer-friendly, enterprise-grade blogging infrastructure \- where every organization can spin up a fully branded collaborative workspace in under 5 minutes.* |
| :---- |

## **1.2 Core Value Propositions**

* Schema-per-tenant isolation — complete data separation at the database level

* Real-time co-authoring via WebSocket/STOMP — no external plugins required

* OAuth2 social login (Google, GitHub) — enterprise-ready authentication out of the box

* S3-compatible media storage per tenant — unlimited asset management

* Headless-ready REST API with HATEOAS — plug into any front-end or mobile app

* Event-driven post lifecycle system — draft → review → published → archived

## **1.3 Technology Stack (Spring Boot Focus)**

| Layer | Technology | Spring Boot Concept |
| ----- | ----- | ----- |
| Backend Framework | Spring Boot 3.x | Auto-configuration, Starters |
| Data / ORM | Spring Data JPA \+ PostgreSQL | Multi-tenancy, Custom Queries |
| Real-Time | Spring WebSocket \+ STOMP | Live Collaborative Editing |
| Authentication | Spring Security OAuth2 | Social Login, JWT, Role Hierarchy |
| File Storage | Spring Content \+ AWS S3 | Media Upload per Tenant |
| API Design | Spring HATEOAS | Hypermedia REST Links |
| Events | Spring Application Events | Post Lifecycle Pub/Sub |
| Caching | Spring Cache \+ Redis | Dashboard & Feed Caching |
| Dev Tools | Spring Boot DevTools | Hot Reload, Live Reload |
| Monitoring | Spring Boot Actuator | Health, Metrics, Info Endpoints |
| Pagination | Spring Data Pageable | Blog Listing, Search |
| Validation | Spring Validation (JSR-380) | Input Guard on All Requests |

| SECTION 2 — PROBLEM STATEMENT & MARKET CONTEXT |
| :---: |

# **2\. Problem Statement & Market Context**

## **2.1 Problem Definition**

Organizations today struggle with three compounding problems when deploying blogging infrastructure:

1. Data Isolation — Shared-database multi-tenancy exposes tenant data to cross-contamination risks. Enterprise clients require guaranteed data segregation, especially under GDPR and SOC 2\.

2. Collaboration Bottleneck — Existing platforms (WordPress, Ghost) offer no native real-time co-authoring. Teams rely on external tools (Google Docs) and then copy-paste into CMSes — creating friction and version conflicts.

3. Developer Fragmentation — Engineers building headless front-ends need a proper REST API with predictable link navigation. Current platforms either lock content into proprietary formats or offer undocumented internal APIs.

## **2.2 Target Users**

| Persona | Who They Are | Their Core Pain | NovaBlog Solution |
| ----- | ----- | ----- | ----- |
| Org Admin | CTO / Head of Content at a company | Can't isolate team blogs without separate infra | Tenant provisioning in \< 5 min |
| Writer | Content creator, journalist, blogger | No real-time co-authoring in CMS | WebSocket live editing |
| Editor | Senior content reviewer | Manual review workflows via email/Slack | Built-in draft→review→publish flow |
| Developer | Front-end / mobile engineer | No clean API to consume blog content | HATEOAS REST \+ Swagger docs |
| Super Admin | Platform owner (Antigravity team) | No single pane to manage all tenants | Admin panel across all tenants |

## **2.3 Market Opportunity**

The global Content Management System market was valued at $23.4B in 2023 and is projected to grow at a CAGR of 12.3% through 2030\. The multi-tenant SaaS CMS sub-segment — currently underserved — represents an estimated $2.1B TAM by 2027\. No dominant platform currently combines true schema-level multi-tenancy with native real-time collaboration.

| SECTION 3 — COMPETITIVE ANALYSIS |
| :---: |

# **3\. Competitive Analysis**

## **3.1 Competitor Landscape**

We analyzed five primary competitors across three dimensions: multi-tenancy architecture, real-time collaboration, and developer API quality.

| Feature | NovaBlog | WordPress Multi | Ghost Pro | Contentful | Strapi | Medium |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| Multi-Tenancy | Schema-level | Table-prefix | None native | Spaces (paid) | Plugin only | None |
| Real-Time Collab | WebSocket/STOMP | Plugin (paid) | None | None | None | None |
| OAuth2 Login | Google \+ GitHub | Plugin | Built-in | SSO (enterprise) | Plugin | Google only |
| S3 Media Storage | Per-tenant bucket | Plugin | Built-in | CDN (paid) | Plugin | Managed |
| REST \+ HATEOAS | Full HATEOAS | Limited REST | REST API | REST \+ GraphQL | REST \+ GraphQL | None |
| Event-Driven Flow | Spring Events | Hooks only | Webhooks | Webhooks | Lifecycle hooks | None |
| Caching Layer | Redis | Object cache plugin | Built-in CDN | CDN | None | CDN |
| Open Source | Yes (Spring Boot) | Yes (PHP) | Yes (Node) | No | Yes (Node) | No |
| Dev Learning Curve | Medium (Java) | Low (PHP) | Low (Node) | Low (API) | Low (Node) | N/A |
| Pricing Model | SaaS / Self-hosted | Freemium | Per-site fee | API calls | Free \+ cloud | Freemium |

## **3.2 Competitive Advantages (NovaBlog Wins)**

* Only platform combining schema-level multi-tenancy \+ WebSocket real-time collab in a single OSS product

* Spring Boot architecture gives developers a structured, enterprise-grade codebase to extend

* Per-tenant S3 buckets provide true asset isolation — not available in any competitor out of the box

* HATEOAS API design allows front-ends to navigate the API without hardcoded URL knowledge

## **3.3 Competitive Risks & Mitigations**

| Risk | Competitor Threat | NovaBlog Mitigation |
| ----- | ----- | ----- |
| WordPress ecosystem lock-in | 30,000+ plugins | Developer-first API & Spring ecosystem |
| Contentful's GraphQL API | Superior query flexibility | Add Spring GraphQL support in Phase 2 |
| Ghost's simplicity | Faster onboarding | Admin dashboard wizard for tenant setup |
| Strapi's Node.js popularity | Larger dev community | Leverage Spring Boot's enterprise adoption |

| SECTION 4 — PRODUCT SCOPE & FEATURES |
| :---: |

# **4\. Product Scope & Features**

## **4.1 Feature Breakdown by Module**

### **Module A — Tenant Management**

* Tenant registration: org name, subdomain, branding (logo, colors)

* Schema-per-tenant database provisioning on signup (Flyway migrations per schema)

* Tenant settings: timezone, language, custom domain CNAME support

* Tenant suspension, archival, and deletion with cascading data cleanup

* Super Admin panel: view all tenants, metrics, toggle status

### **Module B — Authentication & Authorization**

* OAuth2 Social Login: Google, GitHub (Spring Security OAuth2 Client)

* JWT-based stateless session management (access \+ refresh tokens)

* Role hierarchy: SUPER\_ADMIN \> ORG\_ADMIN \> EDITOR \> WRITER \> READER

* Tenant-scoped roles: a user can be EDITOR in Tenant A but WRITER in Tenant B

* Permission guards on all endpoints using Spring Method Security (@PreAuthorize)

* API Key generation for headless clients (readers, mobile apps)

### **Module C — Post & Content Management**

* Rich text editor integration (front-end agnostic — API-first)

* Post states: DRAFT → UNDER\_REVIEW → SCHEDULED → PUBLISHED → ARCHIVED

* Spring Application Events fired on each state transition

* Tagging, categorization, and SEO metadata per post

* Post versioning: full history of edits stored with diff tracking

* Scheduled publishing via Spring @Scheduled cron expressions

* Soft-delete with recycle bin and permanent deletion

### **Module D — Real-Time Collaborative Editing**

* WebSocket endpoint per post (STOMP protocol over SockJS fallback)

* Presence indicators: show connected collaborators per post

* Operational Transform (OT) lite: last-write-wins with conflict notification

* Edit lock requests: writer can lock a section to prevent concurrent edits

* Live cursor position broadcast to collaborators

* Auto-save to draft every 30 seconds via STOMP heartbeat

### **Module E — Media Management**

* S3-compatible bucket per tenant (AWS S3 or MinIO for self-hosted)

* Spring Content integration for file upload/download abstraction

* Image resizing pipeline (thumbnail, medium, full) on upload

* File type validation (image/video/document whitelist)

* File size limits configurable per tenant plan

* Soft-delete media with orphan detection (media not linked to any post)

### **Module F — REST API (HATEOAS)**

* All responses include \_links (self, collection, next, prev) via Spring HATEOAS

* Paginated endpoints with configurable page size and sort fields

* Full-text search via custom JPA queries (Phase 1\) → Elasticsearch (Phase 2\)

* API versioning via URL prefix: /api/v1/...

* Rate limiting per API key (Spring Bucket4j / Redis token bucket)

* OpenAPI 3.0 documentation auto-generated via SpringDoc

### **Module G — Notifications & Emails**

* Spring Mail \+ Thymeleaf templates for: welcome, post published, review requested

* In-platform notifications (stored in DB, polled or SSE-pushed)

* Email digest: weekly summary of published posts (Spring @Scheduled)

* Configurable notification preferences per user

### **Module H — Analytics & Monitoring**

* Post view counts tracked via Redis INCR (eventually persisted to DB)

* Spring Boot Actuator: /health, /metrics, /info, /env exposed to Super Admins

* Custom Actuator health indicator for tenant database connectivity

* Audit log: all admin actions logged (user, action, timestamp, tenant)

| SECTION 5 — USER STORIES |
| :---: |

# **5\. User Stories**

User stories are organized by module. Priority: P0 \= Must Have, P1 \= Should Have, P2 \= Could Have. Story points use Fibonacci scale.

## **5.1 Tenant Management**

| US-001  Register New Organization Tenant   \[P0\]  8 pts |  |
| :---- | :---- |
| **As a** | Organization Administrator |
| **I want to** | register my organization by providing a name, subdomain, and uploading a logo, so the system provisions a schema-isolated workspace for my team |
| **So that** | my team gets a fully isolated blog environment without any manual infrastructure setup |
| **Acceptance Criteria:** • Registration form validates subdomain uniqueness in real-time• Schema is created in PostgreSQL within 10 seconds of registration• Admin receives a welcome email with workspace URL• Default WRITER role is assigned to admin upon first login |  |

| US-002  Tenant Custom Domain Mapping   \[P1\]  13 pts |  |
| :---- | :---- |
| **As a** | Organization Administrator |
| **I want to** | map my own custom domain (e.g., blog.mycompany.com) to my NovaBlog workspace |
| **So that** | our readers see our branded domain and not the NovaBlog subdomain |
| **Acceptance Criteria:** • Admin can enter a custom domain in Tenant Settings• System shows DNS CNAME instructions• Domain is validated via HTTP challenge before activation• SSL certificate provisioning status shown (Let's Encrypt integration) |  |

## **5.2 Authentication & Authorization**

| US-003  Social Login with Google / GitHub   \[P0\]  5 pts |  |
| :---- | :---- |
| **As a** | any platform user (Writer, Editor, or Admin) |
| **I want to** | log in using my Google or GitHub account without creating a separate username/password |
| **So that** | I have a seamless, passwordless onboarding experience |
| **Acceptance Criteria:** • OAuth2 flow redirects correctly and returns JWT access \+ refresh tokens• New users are created in the tenant's schema on first login• Users are assigned WRITER role by default• Login failure (revoked token) shows a clear error with retry option |  |

| US-004  Assign Tenant-Scoped Roles   \[P0\]  5 pts |  |
| :---- | :---- |
| **As a** | Organization Administrator |
| **I want to** | assign or change a team member's role within my tenant (e.g., promote a Writer to Editor) |
| **So that** | I can control who can publish vs who can only draft content |
| **Acceptance Criteria:** • Role assignment UI lists all tenant members with current roles• Only ORG\_ADMIN or higher can change roles• Role change takes effect immediately (JWT re-validation on next request)• Audit log entry is created for every role change |  |

## **5.3 Post & Content Management**

| US-005  Create and Save Post as Draft   \[P0\]  8 pts |  |
| :---- | :---- |
| **As a** | Writer |
| **I want to** | create a new blog post, write content with formatting, and save it as a draft |
| **So that** | I can work on content at my own pace without it going live until I'm ready |
| **Acceptance Criteria:** • Post form includes title, body (rich text), tags, category, and SEO fields• Auto-save triggers every 30 seconds via STOMP heartbeat• Manual save creates a version snapshot in post\_versions table• Draft is only visible to Writers and above in the same tenant |  |

| US-006  Submit Draft for Editorial Review   \[P0\]  5 pts |  |
| :---- | :---- |
| **As a** | Writer |
| **I want to** | submit my completed draft to an Editor for review with a single action |
| **So that** | the Editor is notified and can review my post before it is published |
| **Acceptance Criteria:** • Writer clicks 'Submit for Review' — post state changes to UNDER\_REVIEW• Spring Event fires → Editor receives in-platform \+ email notification• Writer cannot edit post while it is under review (read-only mode)• Editor can approve, reject (with comment), or request changes |  |

| US-007  Schedule Post for Future Publishing   \[P1\]  8 pts |  |
| :---- | :---- |
| **As a** | Editor |
| **I want to** | approve a post and schedule it to be published at a specific future date and time |
| **So that** | content is released at the optimal time without anyone needing to be online |
| **Acceptance Criteria:** • Date-time picker with timezone conversion (tenant's default timezone)• Post enters SCHEDULED state; Spring @Scheduled job checks every minute• Post auto-publishes at the set time; Spring Event fires post-publish hooks• Editor can reschedule or cancel up until 5 minutes before publish time |  |

## **5.4 Real-Time Collaborative Editing**

| US-008  See Live Collaborators on a Post   \[P0\]  8 pts |  |
| :---- | :---- |
| **As a** | Writer or Editor |
| **I want to** | see which other team members are currently editing the same post as me |
| **So that** | I can coordinate with them to avoid conflicting edits |
| **Acceptance Criteria:** • Presence panel shows avatar \+ name of all connected users on the post• Users appear within 2 seconds of opening the post editor• Users are removed from presence panel within 5 seconds of disconnecting• WebSocket connection uses STOMP over SockJS with automatic fallback |  |

| US-009  Receive Real-Time Edit Notifications on Post   \[P0\]  13 pts |  |
| :---- | :---- |
| **As a** | Writer collaborating on a post |
| **I want to** | receive immediate visual notifications when a co-author makes changes to the post I am editing |
| **So that** | I always see the latest version and avoid overwriting others' work |
| **Acceptance Criteria:** • Changes from other users appear in the editor within 500ms• Changed sections are briefly highlighted in a distinct color• Conflict toast shown if two users edit the exact same paragraph simultaneously• Full edit history is accessible from the post sidebar |  |

## **5.5 Media Management**

| US-010  Upload Media Files to Post   \[P0\]  8 pts |  |
| :---- | :---- |
| **As a** | Writer |
| **I want to** | upload images or documents directly within the post editor and embed them in my content |
| **So that** | my posts are visually rich without needing external image hosting |
| **Acceptance Criteria:** • Supports drag-and-drop and file picker upload• Accepted formats: JPEG, PNG, GIF, WEBP, PDF, MP4 (configurable)• Max file size enforced per tenant plan (default: 10MB images, 50MB video)• Uploaded file URL auto-inserted at cursor position in editor• Upload progress bar shown; error shown if file exceeds size limit |  |

## **5.6 API & Headless Access**

| US-011  Consume Published Posts via REST API   \[P0\]  8 pts |  |
| :---- | :---- |
| **As a** | Front-end Developer integrating NovaBlog |
| **I want to** | fetch a paginated list of published posts for a specific tenant via a REST API with hypermedia links |
| **So that** | I can build a custom front-end without being coupled to NovaBlog's UI |
| **Acceptance Criteria:** • GET /api/v1/{tenant}/posts returns paginated list (default 20 per page)• Each post resource includes \_links.self, \_links.author, \_links.next, \_links.prev• Supports query params: ?tag=X\&category=Y\&sort=publishedAt,desc• API Key required in Authorization header; rate limit: 1000 req/hr• OpenAPI spec available at /api/v1/docs |  |

| US-012  Generate and Manage API Keys   \[P1\]  5 pts |  |
| :---- | :---- |
| **As a** | Organization Administrator |
| **I want to** | generate, name, and revoke API keys for my tenant's headless integrations |
| **So that** | I can control exactly which external systems have read access to our published content |
| **Acceptance Criteria:** • Admin panel shows all active API keys with name, created date, last used date• New key is shown only once at creation; must be copied immediately• Keys can be revoked instantly; revocation takes effect within 60 seconds• Each key can be scoped to READ\_ONLY or FULL\_ACCESS |  |

| SECTION 6 — NON-FUNCTIONAL REQUIREMENTS |
| :---: |

# **6\. Non-Functional Requirements**

| NFR Category | Requirement | Acceptance Threshold |
| ----- | ----- | ----- |
| Performance | API P99 response time (read endpoints) | \< 300ms under 100 concurrent users |
| Performance | WebSocket message delivery latency | \< 500ms end-to-end |
| Scalability | Concurrent tenant schemas supported | Up to 500 tenants without degradation |
| Availability | Platform uptime SLA | 99.5% monthly (Phase 1\) |
| Security | JWT token expiry (access / refresh) | 15 min access / 7 days refresh |
| Security | Password hashing (if added) | BCrypt with cost factor ≥ 12 |
| Security | OWASP Top 10 compliance | Verified via OWASP ZAP scan before launch |
| Data Isolation | Cross-tenant data access | Zero tolerance — enforced at schema level |
| Observability | Spring Boot Actuator endpoints | All endpoints secured to SUPER\_ADMIN only |
| Maintainability | Unit test coverage | \> 80% coverage on service layer |
| Compliance | GDPR data deletion | Full tenant data erasure within 30 days of request |

| SECTION 7 — PRODUCT ROADMAP |
| :---: |

# **7\. Product Roadmap**

## **7.1 Phased Delivery Plan**

The roadmap is structured in 4 phases across 6 months, each building on the previous. Each phase ends with a shippable, demonstrable build.

| PHASE 1  |  Foundation & Core  |  Weeks 1–6 |
| :---- |

| Sprint | Deliverable | Stories | Spring Concepts |
| ----- | ----- | ----- | ----- |
| S1 (W1-2) | Project setup, multi-tenant DB schema provisioning, Flyway migrations | US-001 | Spring Data JPA, Flyway, Multi-Tenancy |
| S2 (W3-4) | OAuth2 login (Google/GitHub), JWT issuance, role hierarchy | US-003, US-004 | Spring Security OAuth2, JWT |
| S3 (W5-6) | Post CRUD (create/read/update/delete), draft state, versioning | US-005 | Spring Data JPA, @Transactional, @Scheduled |

| MILESTONE | End of Phase 1: A writer can log in via Google, create a post as a draft, and the post is stored in their tenant's isolated PostgreSQL schema. |
| :---: | :---- |

| PHASE 2  |  Collaboration & Publishing  |  Weeks 7–12 |
| :---- |

| Sprint | Deliverable | Stories | Spring Concepts |
| ----- | ----- | ----- | ----- |
| S4 (W7-8) | WebSocket real-time collab, STOMP, presence indicators | US-008, US-009 | Spring WebSocket, STOMP, SockJS |
| S5 (W9-10) | Review workflow, Spring Events, email notifications (Thymeleaf) | US-006, US-007 | Spring Events, Spring Mail, Thymeleaf |
| S6 (W11-12) | Media upload to S3, per-tenant bucket, image resizing | US-010 | Spring Content, AWS S3 SDK, Multipart |

| MILESTONE | End of Phase 2: Multiple writers can collaborate on a post in real-time, submit for review, and publish with media attachments stored in tenant-isolated S3 buckets. |
| :---: | :---- |

| PHASE 3  |  API, Caching & Observability  |  Weeks 13–18 |
| :---- |

| Sprint | Deliverable | Stories | Spring Concepts |
| ----- | ----- | ----- | ----- |
| S7 (W13-14) | HATEOAS REST API, pagination, sorting, OpenAPI docs | US-011 | Spring HATEOAS, SpringDoc, Pageable |
| S8 (W15-16) | API Key management, rate limiting (Redis token bucket) | US-012 | Spring Cache, Redis, Custom Filters |
| S9 (W17-18) | Actuator endpoints, custom health indicators, audit log | — | Spring Actuator, Micrometer, AOP Logging |

| MILESTONE | End of Phase 3: A developer can consume the full HATEOAS API with an API key, rate limiting is enforced, and the Super Admin has health monitoring across all tenants. |
| :---: | :---- |

| PHASE 4  |  Hardening, Analytics & Beta Launch  |  Weeks 19–24 |
| :---- |

| Sprint | Deliverable | Stories | Spring Concepts |
| ----- | ----- | ----- | ----- |
| S10 (W19-20) | Custom domain CNAME mapping, tenant branding settings | US-002 | Spring MVC, Custom Domain Filter, Config |
| S11 (W21-22) | Post analytics (Redis view counts), weekly digest email | — | Spring @Scheduled, Redis INCR, Spring Mail |
| S12 (W23-24) | Security hardening (OWASP), load testing, beta onboarding | — | Spring Security, Profiles, Test Containers |

| MILESTONE | End of Phase 4: Platform is production-ready, OWASP-compliant, load-tested to 500 concurrent users, and ready for beta customer onboarding. |
| :---: | :---- |

| SECTION 8 — SPRINT VELOCITY & ESTIMATION |
| :---: |

# **8\. Sprint Velocity & Estimation**

| Phase | Sprints | Story Points | Recommended Team Size | Duration |
| ----- | ----- | ----- | ----- | ----- |
| Phase 1 — Foundation | 3 Sprints (S1–S3) | 42 pts | 2 Backend Dev \+ 1 DevOps | 6 weeks |
| Phase 2 — Collaboration | 3 Sprints (S4–S6) | 55 pts | 2 Backend Dev \+ 1 Frontend | 6 weeks |
| Phase 3 — API & Ops | 3 Sprints (S7–S9) | 38 pts | 2 Backend Dev \+ 1 QA | 6 weeks |
| Phase 4 — Hardening | 3 Sprints (S10–S12) | 30 pts | 1 Backend Dev \+ 1 QA \+ 1 DevOps | 6 weeks |
| TOTAL | 12 Sprints | 165 pts | Core team: 3–4 engineers | 24 weeks (\~6 months) |

## **8.1 Definition of Done (DoD)**

4. Code is reviewed via Pull Request with at least 1 approver

5. Unit tests written for all service-layer methods (\>80% coverage on new code)

6. Integration tests pass using Spring Boot Test \+ Testcontainers (PostgreSQL \+ Redis)

7. API contract documented in OpenAPI spec and verified against implementation

8. Feature passes manual QA checklist for the relevant user story acceptance criteria

9. No CRITICAL or HIGH severity security findings in OWASP ZAP scan

10. Deployed to staging environment and smoke-tested by Product Owner

| SECTION 9 — TECHNICAL ARCHITECTURE NOTES |
| :---: |

# **9\. Technical Architecture Notes**

## **9.1 Multi-Tenancy Strategy**

NovaBlog uses Schema-per-Tenant isolation (not row-level). Each tenant registration triggers a Flyway migration that creates a dedicated PostgreSQL schema (e.g., schema: 'tenant\_acme'). The TenantContext (ThreadLocal) is populated by a TenantIdentificationFilter that extracts the tenant ID from the JWT or the subdomain of the request.

* TenantIdentificationFilter → sets TenantContext via subdomain or JWT claim

* MultiTenantConnectionProvider → resolves JDBC connection to tenant's schema

* CurrentTenantIdentifierResolver → reads from TenantContext ThreadLocal

* Flyway Tenant Migration → runs on every new tenant registration

## **9.2 WebSocket Architecture**

The real-time collaboration system uses Spring's WebSocket support with STOMP message broker. Each post gets a dedicated STOMP topic (/topic/post/{tenantId}/{postId}). The in-memory STOMP broker is used in Phase 1; Phase 2 upgrade to RabbitMQ STOMP relay for horizontal scaling.

## **9.3 Event-Driven Post Lifecycle**

Spring Application Events are fired synchronously within the same thread by default, but annotated with @Async for email notifications. Events include: PostCreatedEvent, PostSubmittedForReviewEvent, PostApprovedEvent, PostPublishedEvent, PostArchivedEvent. Each event carries the postId, tenantId, actorUserId, and timestamp.

## **9.4 Caching Strategy**

| Cache Key Pattern | TTL | Invalidation Trigger | Spring Component |
| ----- | ----- | ----- | ----- |
| post::{tenantId}::{postId} | 10 min | Post update / publish | @CachePut, @CacheEvict |
| feed::{tenantId}::page::{n} | 5 min | New post published | @CacheEvict on publish |
| tenant::{tenantId}::settings | 30 min | Settings update | @CacheEvict on save |
| post::{postId}::viewcount | Real-time | Every view (Redis INCR) | Redis Template |

| SECTION 10 — RISKS, ASSUMPTIONS & DEPENDENCIES |
| :---: |

# **10\. Risks, Assumptions & Dependencies**

## **10.1 Risk Register**

| Risk | Probability | Impact | Mitigation |
| ----- | ----- | ----- | ----- |
| Schema migration failure on tenant registration | Medium | High | Rollback transaction on Flyway failure; retry mechanism |
| WebSocket scalability beyond 200 concurrent sessions | Low | High | Upgrade to RabbitMQ STOMP relay in Phase 2 |
| S3 bucket cost overrun per tenant | Medium | Medium | File size limits \+ lifecycle policies on S3 buckets |
| OAuth2 provider API changes (Google/GitHub) | Low | Medium | Pin OAuth2 library version; monitor provider changelogs |
| GDPR compliance for EU tenant data | Medium | High | Implement data export \+ erasure API before Phase 4 |
| Spring Boot version upgrade breaking changes | Low | Low | Pin Spring Boot version; upgrade only in dedicated sprints |

## **10.2 Assumptions**

11. Front-end (web UI) is out of scope for this PRD — NovaBlog is API-first; Antigravity's front-end team handles the UI separately.

12. AWS credentials and S3 bucket provisioning automation scripts are provided by the DevOps engineer.

13. PostgreSQL 15+ is used as the primary database; Redis 7+ for caching and real-time counters.

14. A single AWS region deployment is sufficient for Phase 1–3; multi-region is Phase 5+ scope.

15. The team has access to a shared staging environment throughout development.

## **10.3 Out of Scope (Phase 1–4)**

* Mobile native applications (iOS / Android)

* Payment / subscription billing integration (Stripe, Razorpay)

* Elasticsearch full-text search (deferred to Phase 5\)

* GraphQL API layer

* Multi-region deployment or CDN integration

* AI-assisted content suggestions

| SECTION 11 — APPENDIX |
| :---: |

# **11\. Appendix**

## **11.1 Glossary**

| Term | Definition |
| ----- | ----- |
| Tenant | An isolated organizational workspace with its own database schema |
| Schema-per-Tenant | Database isolation strategy where each tenant has its own PostgreSQL schema |
| STOMP | Simple Text Oriented Messaging Protocol — used over WebSocket for real-time messaging |
| HATEOAS | Hypermedia as the Engine of Application State — REST API links included in each response |
| Spring Event | Spring's built-in publish/subscribe mechanism for decoupled inter-component communication |
| JWT | JSON Web Token — stateless authentication token containing user claims |
| Flyway | Database schema version migration tool integrated with Spring Boot |
| OT (lite) | Operational Transform — algorithm for real-time collaborative document editing |
| TenantContext | ThreadLocal variable storing the current request's tenant identifier |
| Story Point | Relative effort estimation unit using Fibonacci sequence (1, 2, 3, 5, 8, 13, 21...) |

## **11.2 User Story Summary Index**

| Story ID | Title | Priority | Points | Phase |
| ----- | ----- | ----- | ----- | ----- |
| US-001 | Register New Organization Tenant | P0 | 8 | Phase 1 |
| US-002 | Tenant Custom Domain Mapping | P1 | 13 | Phase 4 |
| US-003 | Social Login with Google / GitHub | P0 | 5 | Phase 1 |
| US-004 | Assign Tenant-Scoped Roles | P0 | 5 | Phase 1 |
| US-005 | Create and Save Post as Draft | P0 | 8 | Phase 1 |
| US-006 | Submit Draft for Editorial Review | P0 | 5 | Phase 2 |
| US-007 | Schedule Post for Future Publishing | P1 | 8 | Phase 2 |
| US-008 | See Live Collaborators on a Post | P0 | 8 | Phase 2 |
| US-009 | Receive Real-Time Edit Notifications | P0 | 13 | Phase 2 |
| US-010 | Upload Media Files to Post | P0 | 8 | Phase 2 |
| US-011 | Consume Published Posts via REST API | P0 | 8 | Phase 3 |
| US-012 | Generate and Manage API Keys | P1 | 5 | Phase 3 |

**END OF DOCUMENT**

NovaBlog PRD v1.0  ·  Confidential  ·  Prepared for Antigravity  ·  April 2025