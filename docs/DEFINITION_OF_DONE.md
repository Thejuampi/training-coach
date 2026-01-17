# Definition of Done (Checklist)

Use this checklist for each feature epic and UI view before calling it "done".

## Functional

- Acceptance scenarios added or updated in `src/test/resources/features`.
- UI view is mapped in `docs/UI_VIEWS.md` with routes and responsibilities.
- Use case documentation updated or linked (`docs/use-cases/UC*.md`).

## Quality

- Unit tests or component tests cover core logic.
- Edge cases and error states are documented and tested.
- Logs and audit records are produced where required.

## Security & Privacy

- Role checks enforced at the API layer.
- Secrets are not returned in responses or logs.
- Data deletion/export flows are documented with consent logging.

## UX

- Loading/empty/error states are defined.
- Keyboard shortcuts and accessibility notes are updated where applicable.
- ErrorDetailsView can show diagnostics with redaction.

## Operations

- Backend gaps are documented, and UI shows "pending" placeholders where required.
- Monitoring/alerting hooks identified (if applicable).
- Feature flagged or staged if backend is not ready.
