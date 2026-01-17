# Meeting Outcome: Training Coach 2026 Roadmap

**Date:** January 17, 2026
**Participants:** @user-athlete, @user-coach, @product-owner, @user-admin
**Focus:** Define clear action plan for next year prioritizing TUI app users (local-first, eventual web deployment)

---

## 1. Current Status Summary

### What Works Today (Backend APIs)
- **Auth & Session**: Login, refresh token, logout (JWT-based)
- **Athlete Management**: CRUD operations, profile, preferences, baseline metrics
- **Integrations**: Intervals.icu credential configuration (status only)
- **Data Sync**: Manual/scheduled sync of activities and wellness data
- **Wellness & Readiness**: Submit wellness, query dashboards, reports, recommendations
- **Plan Generation**: Generate training plans (draft-only, no persistence)
- **Coach Notes**: Create/query notes (in-memory, lost on restart)
- **User Management**: CRUD users, roles, password management
- **Analysis**: Compliance calculator (planned vs completed), adjustment suggestions (text only)

### What Works Today (TUI)
- Lanterna-based TUI with declarative UI specs
- Authentication flow: connection -> session -> main menu
- Plan generation wizard (athlete ID, phase, start date, target hours)
- Plan preview (shows generated plan structure)
- User admin (list, create, set/reset password, disable/enable)
- Role-based menu options (COACH/ADMIN show different items)

### Gaps / Non-Working Items
- **Plan Lifecycle**: No persistence, no list/fetch/versioning, no publish status
- **Workout Execution**: No RPE/notes endpoints, no plan-activity matching
- **Compliance**: Calculator only, no real matching/persistence
- **Adjustments**: Calculator text only, no plan persistence to apply to
- **Notes**: In-memory only, lost on restart
- **Zones & Testing**: No endpoints for zone management or test protocols
- **Availability**: No availability exception model/endpoints
- **Events/Races**: No events model/endpoints
- **Notifications**: No delivery mechanism
- **Reports/Exports**: Wellness report exists; compliance/plan reports missing
- **Privacy/Retention**: No export/delete endpoints

### TUI Placeholder Routes
Main menu has many items that route to "main" (not implemented):
- Athletes
- Wellness & Readiness
- Activities
- Compliance & Progress
- Adjust Plan
- Notes / Communication
- Testing & Zones (LT1/LT2)
- Availability & Calendar
- Events / Races
- Notifications
- Reports & Exports
- Safety & Guardrails
- Integrations (COACH/ADMIN role-specific)

---

## 2. Persona Perspectives

### @user-athlete Perspective

**My Goals for 2026:**
- Know exactly what to do each day (today's workout + targets)
- Log how I felt (RPE, notes) without friction
- See my progress over time (trends, compliance)
- Get alerts when something looks wrong (fatigue, missed sessions)
- Easily share data with my coach if needed

**What I Need Daily:**
- "What do I do today?" view (workout details, targets)
- Quick wellness check-in (5 questions or less)
- Post-workout RPE + notes entry
- At-a-glance readiness score

**What I Need Weekly:**
- Compliance summary (did I hit the plan?)
- Load trend (am I ramping too fast?)
- Key messages/notes from coach

**Minimum Data I'll Enter:**
- Daily: wellness subjective scores
- Post-workout: RPE, optional notes
- Weekly: nothing else if sync handles activities

**MVP Acceptance Criteria (Athlete):**
1. Login works, I see my plan for today
2. I can log wellness in <30 seconds
3. I can see what I did last week vs planned
4. I see a readiness number that makes sense
5. Coach messages appear in context

**Pain Points / Risks:**
- Too many forms/fields → I won't use it daily
- Data sync errors → frustration, trust loss
- Confusing metrics → I'll ignore them

**Questions:**
1. Will I need to enter training data manually, or will Intervals.icu sync handle it?
2. How do I request a change to my plan (e.g., take a rest day)?
3. Can I see my training zones (Z1/Z2/Z3) after a test?
4. Will the app alert me if my load is spiking?
5. Can I export my data if I want to switch platforms?

---

### @user-coach Perspective

**My Coaching Goals for 2026:**
- Plan training efficiently (polarized distribution, phase-appropriate)
- Monitor athlete compliance and load trends
- Adjust plans safely when readiness drops or events change
- Communicate clearly with athletes (notes, recommendations)
- Use data without drowning in it

**What I Need Weekly:**
- Athlete roster with status (compliance, readiness, load trend)
- Quick drill-down to individual athlete detail
- Plan generation wizard (phase, volume, key sessions)
- Ability to publish/review/revise plans
- Place to leave notes linked to athlete or date

**What I Need to Coach Effectively:**
- LT1/LT2 thresholds for Seiler 3-zone distribution
- FTP/FTHR for prescription bands (FATMAX, VO2-optimal, sprint)
- Training load metrics (acute/chronic ratio, ramp rate)
- Compliance metrics (duration, intensity distribution, key sessions)
- Safety guardrails (caps, minimum recovery, fatigue blocks)

**MVP Acceptance Criteria (Coach):**
1. I can create an athlete and set baseline metrics
2. I can generate a 4-week plan with polarized structure
3. I can see last week's compliance (planned vs completed)
4. I can leave a note for an athlete
5. I can see readiness trend and fatigue flags

**Anti-Patterns to Avoid:**
- Z2 creep (too much moderate intensity)
- Junk miles (low-intensity without purpose)
- Ramping volume too fast (injury risk)
- Skipping key sessions (lose specificity)
- Over-relying on AI without deterministic guardrails

**What I'm Worried About:**
- Plan adjustments being too easy → breaking principles
- Compliance matching being inaccurate → wrong conclusions
- Wellness data being incomplete → low-confidence readiness

**Questions:**
1. Can I schedule a formal test protocol and have zones auto-update?
2. How do I "publish" a plan so the athlete sees it?
3. Can I adjust a single week without breaking the whole plan?
4. Will the system warn me if an athlete's load is unsafe?
5. Can I export a weekly summary for my records?

---

### @product-owner Perspective

**2026 Product Vision:**
Deliver a credible, coach-ready training platform with local-first TUI by Q2 2026, enabling endurance athletes and coaches to manage planning, execution, and wellness with Seiler polarized methodology.

**Measurable Outcomes (Year-End 2026):**
1. **MVP Usability**: 1 coach can manage 5-10 athletes with full plan lifecycle (generate, publish, revise)
2. **Athlete Engagement**: Athletes log wellness ≥80% of days and see compliance trends
3. **Coverage**: Core use cases UC1-UC9 working end-to-end with TUI
4. **Data Quality**: Sync reliability >95%, readiness score confidence ≥70%
5. **Operations**: Admin can manage users/integrations without code

**Q1 2026 (Foundation):**
- Fix in-memory notes → persistence
- Persist training plans (list, fetch, version, publish, archive)
- Implement plan-activity matching for compliance
- Add TUI screens for:
  - Athletes list (CRUD)
  - Wellness dashboard (trends, flags)
  - Plan preview (calendar view)
- Define of done for each feature (F1-F6)

**Q2 2026 (MVP):**
- Athlete-facing TUI screens:
  - Today's workout (targets, execute)
  - Wellness check-in (quick form)
  - Compliance summary (weekly)
- Coach-facing enhancements:
  - Plan revision workflow
  - Weekly review dashboard (compliance + load + readiness)
- Implement zone management (LT1/LT2, prescription bands)
- Add basic notifications (wellness reminder, missed key session)

**Q3 2026 (Enhancements):**
- Availability exceptions (travel, rest weeks)
- Events/races (goal events drive taper)
- Reports/exports (weekly summary CSV/JSON)
- Improved plan wizard (key session configuration)
- Safety guardrails UI (caps, fatigue blocks)

**Q4 2026 (Polish):**
- Multi-platform reconciliation (if needed)
- Advanced notifications (digests, athlete nudges)
- Data retention/privacy (export, delete requests)
- Performance optimization (large datasets)
- Migration readiness checklist for web deployment

**Scope (In MVP vs Out):**
**In (MVP):**
- UC0 (Auth), UC1 (Athlete), UC2 (Sync), UC3 (Plan lifecycle), UC5 (Wellness), UC6 (Compliance), UC8 (Notes), UC9 (Intervals.icu), UC10 (Zones)
- Basic UC7 (Adjustments - manual only)
- TUI screens for all above
- Local-only operation (single user on same PC)

**Out (Post-MVP):**
- UC11 (Availability exceptions) → Q3
- UC12 (Events/Races) → Q3
- UC13 (Notifications) → partial Q2, full Q4
- UC14 (Reports/Exports) → partial Q2, full Q3
- UC15 (Admin & Roles) → basic now, full auth later
- UC16 (Safety & Guardrails) → backend now, UI Q3
- UC17 (Workout Library) → 2027
- UC18 (Multi-platform) → 2027
- UC19 (Athlete self-service) → 2027
- UC20 (Privacy/Retention) → Q4

**Risks & Mitigations:**
| Risk | Impact | Mitigation |
|------|--------|------------|
| Plan persistence complexity | High | Use simple versioning (planId + version + status) |
| Compliance matching accuracy | High | Start with simple date/type matching, add coach override |
| Wellness data completeness | Medium | Graceful degradation, show confidence/coverage |
| TUI usability for non-technical users | Medium | Simplify forms, add help text, iterate with feedback |
| Backend stability for local deployment | Low | Use H2, embed server, provide clear start/stop instructions |

**Feedback Loop:**
- Weekly demo with coach and athlete (even if simulated initially)
- Bi-weekly review of acceptance criteria vs implementation
- Monthly roadmap adjustment based on learnings

---

### @user-admin Perspective

**Admin Goals for 2026:**
- Manage users and roles reliably
- Ensure integrations stay healthy
- Protect data (backup, audit, retention)
- Enable operations without code changes

**What I Need Now:**
- User CRUD works (verified: yes)
- Password management works (set, rotate, disable/enable)
- Integration status visibility (Intervals.icu health)
- Data backup/restore process
- Audit trail for plan changes and sync runs

**What I Need for Web Migration (Future):**
- Clean separation of config vs runtime
- Environment variables for secrets
- Database migration scripts
- Backup/restore automation
- Rate limits and feature flags

**Non-Functional Requirements:**
- **Stability**: App starts reliably, no data loss on restart
- **Backup**: Daily DB backup, export of user/plan/wellness data
- **Observability**: Logs for sync runs, plan changes, auth failures
- **Security**: Passwords hashed, JWT tokens, role enforcement
- **Data Portability**: Export endpoints for athlete data (Q4)
- **Upgrade Path**: Migration scripts for schema changes

**Pain Points / Risks:**
- In-memory notes lost on restart → data integrity issue
- No audit trail → can't debug sync failures
- No backup process → data loss risk
- API key exposed in GET response → security issue

**P0 Admin Backlog (Q1):**
1. Persist notes (currently in-memory)
2. Add sync run logging (status, counts, errors)
3. Add plan change audit (who, when, what changed)
4. Document backup/restore procedure
5. Mask API key in integration GET response

**P1 Admin Backlog (Q2-Q3):**
6. Add health check endpoint (DB, integration connectivity)
7. Add data export endpoint (athlete data, plans, wellness)
8. Implement rate limiting configuration
9. Add feature flag configuration
10. Add retention policy configuration

**P2 Admin Backlog (Q4+):**
11. Data deletion endpoint (GDPR compliance)
12. Consent logging (explicit opt-in tracking)
13. Admin dashboard (coverage, compliance, risk flags)
14. Token revocation visibility
15. Multi-tenant support (if needed for web deployment)

**Questions:**
1. Where should backups be stored locally?
2. How do I rotate JWT signing keys?
3. Can I disable an athlete without deleting their data?
4. What data retention policy should we enforce?
5. How do I monitor sync health in production?

---

## 3. Consolidated Prioritized Backlog

### P0 (Q1 2026 - Foundation)
1. **Persist Notes** - Move from in-memory to DB storage
2. **Plan Persistence** - CRUD operations for plans, list, fetch, version, publish status, archive
3. **Compliance Matching** - Match planned workouts to completed activities, persist compliance results
4. **TUI: Athletes List** - Show all athletes, CRUD from TUI
5. **TUI: Wellness Dashboard** - Trends, flags, readiness score
6. **TUI: Plan Preview** - Calendar view of plan structure
7. **Sync Run Logging** - Log sync runs (status, counts, errors) for observability

### P1 (Q2 2026 - MVP)
8. **TUI: Today's Workout** - Athlete view of planned workout + targets
9. **TUI: Wellness Check-in** - Quick daily wellness form
10. **TUI: Compliance Summary** - Athlete view of weekly compliance
11. **Plan Revision Workflow** - Revise plan with guardrails, version history
12. **Weekly Review Dashboard** - Coach view: compliance, load, readiness, notes
13. **Zone Management** - LT1/LT2 thresholds, prescription bands
14. **Basic Notifications** - Wellness reminder, missed key session alert

### P2 (Q3 2026 - Enhancements)
15. **Availability Exceptions** - Travel, rest weeks
16. **Events/Races** - Goal events drive taper
17. **Reports/Exports** - Weekly summary CSV/JSON, plan reports
18. **Plan Wizard Enhancement** - Key session configuration
19. **Safety Guardrails UI** - Caps, fatigue blocks
20. **Admin Health Check** - DB, integration status

### P3 (Q4 2026 - Polish)
21. **Multi-platform Reconciliation** - If needed
22. **Advanced Notifications** - Digests, athlete nudges
23. **Data Retention/Privacy** - Export, delete requests
24. **Performance Optimization** - Large datasets
25. **Web Migration Readiness** - Checklist, migration scripts

---

## 4. Definition of Done (DoD) per Feature

| Feature | DoD Checklist |
|---------|---------------|
| Notes | - Persist to DB, restart-safe<br>- CRUD API + TUI screen<br>- Audit: who/when<br>- Tests: integration + Cucumber |
| Plan Lifecycle | - CRUD API (create, list, fetch, update, delete)<br>- Versioning (planId + version + status)<br>- Publish/unpublish workflow<br>- TUI: list, preview, generate, revise<br>- Tests: integration + acceptance |
| Compliance Matching | - Match logic (date/type tolerance)<br>- Persist compliance results<br>- Query by athlete, date range<br>- TUI: athlete view + coach view<br>- Tests: edge cases (no match, conflicts) |
| Zones & Testing | - Zone model (LT1/LT2, Z1/Z2/Z3, prescription bands)<br>- Test result endpoints<br>- Auto-update zones on test entry<br>- Apply to plan generator and analysis<br>- Tests: zone calculation, band derivation |
| Notifications | - Scheduler for reminders<br>- Alert triggers (fatigue, missed session)<br>- User notification preferences<br>- TUI: notification center<br>- Tests: delivery, preference filters |

---

## 5. Architecture Notes for Local-First → Web Migration

**Current Architecture (Local-First):**
- Single JVM: Spring Boot + H2 on same PC
- TUI: Lanterna-based, direct REST calls to localhost:8080
- Data: H2 file in `data/` directory
- No external dependencies beyond Intervals.icu (optional)

**Migration-Ready Design Principles:**
- Keep configuration external (environment variables, .env file)
- Avoid hardcoded file paths
- Use JPA for DB abstraction (H2 → PostgreSQL easy)
- REST API boundaries already exist for Vaadin UI
- Security: JWT tokens already implemented

**Web Deployment Checklist (Future):**
1. Replace H2 with PostgreSQL
2. Containerize (Docker) or deploy to server
3. Configure reverse proxy (Nginx) + SSL
4. Set up backup/restore automation
5. Configure environment variables for secrets
6. Add health checks and monitoring
7. Rate limiting and feature flags
8. Multi-user concurrency testing

**What Won't Change:**
- Domain model and business logic
- REST API contracts
- Seiler intensity model
- Coach/Athlete/Admin workflows

---

## 6. Next Immediate Actions (This Sprint)

**For Developers:**
1. Implement plan persistence (entity, repo, service, controller, tests)
2. Persist notes (entity, repo, service, controller, tests)
3. Implement compliance matching (logic, storage, API, tests)
4. Add TUI screens for athletes list and wellness dashboard
5. Add sync run logging

**For Product Owner:**
1. Prioritize P0 backlog items into 2-week sprints
2. Write acceptance criteria for each P0 item
3. Schedule weekly demo with coach/athlete (simulated if needed)
4. Update DoD checklist for each feature

**For Admin:**
1. Document backup/restore procedure
2. Add health check endpoint
3. Mask API key in integration GET response
4. Define retention policy

**For Coach/Athlete (Simulated):**
1. Provide feedback on TUI wireframes (mock screens)
2. Validate compliance matching logic (edge cases)
3. Test wellness dashboard clarity
4. Review plan revision workflow

---

## 7. Open Questions & Decisions Needed

| ID | Question | Priority | Owner | Decision By |
|----|----------|----------|-------|-------------|
| Q1 | Which backup storage mechanism for local deployment? | P1 | Admin | Week 2 |
| Q2 | Should plan revision require coach approval (safety)? | P0 | PO/Coach | Week 2 |
| Q3 | What tolerance windows for compliance matching? | P0 | Coach | Week 3 |
| Q4 | Should athletes see Z1/Z2/Z3 time or prescription bands? | P1 | Coach/Athlete | Week 4 |
| Q5 | Data retention period for wellness snapshots? | P2 | Admin | Q2 |
| Q6 | Should notifications be in-app only or email too? | P2 | PO | Q2 |
| Q7 | Web deployment timeline (when to start planning)? | P2 | PO | Q3 |

---

## 8. Meeting Summary

**Consensus Reached:**
- Q1 2026: Fix foundation (notes persistence, plan persistence, compliance matching)
- Q2 2026: Deliver MVP (athlete screens, coach dashboards, zones, basic notifications)
- Q3 2026: Enhance (availability, events, reports, safety UI)
- Q4 2026: Polish (performance, privacy, web migration readiness)

**Key Decisions:**
- Local-first approach for now (single PC, H2, no cloud)
- TUI as primary UI for 2026
- Vaadin UI planned but not blocking MVP
- Web deployment as separate project (late 2026/2027)

**Action Items:**
1. PO: Prioritize P0 into sprints, write acceptance criteria
2. Tech Lead: Design plan persistence and compliance matching
3. Admin: Define backup/restore, health checks
4. Developers: Start P0 implementation (notes, plan persistence)

**Follow-Up:**
- Weekly demo standup (Fridays)
- Bi-weekly roadmap review
- Monthly retrospective and adjustment
