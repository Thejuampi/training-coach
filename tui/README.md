# TUI module

## Overview
The TUI (terminal user interface) module provides a text-based UI for the Training Coach application.
It follows a simple presenter + declarative UI spec approach. Presenters build UiSpec models
from application state and gateway calls. TuiRenderer renders the UiSpec via Lanterna.

## Architecture
- `TuiApp`: Application entrypoint. Wires state, gateway, presenters, and navigation.
- `TuiState`: Mutable state used across presenters (session, base URL, form inputs).
- `TrainingCoachGateway`: Port for backend calls; `RestTrainingCoachGateway` is the HTTP adapter.
- `presenter/*`: Presentation logic. Each presenter produces a `UiSpec` and wires button actions.
- `ui/*`: Declarative UI spec types (`UiSpec`, `UiLabelSpec`, `UiButtonSpec`, etc.).
- `TuiNavigator`: Simple router. Maps routes to `UiSpec` builders and invokes the renderer.
- `TuiRenderer`: Converts `UiSpec` into Lanterna components.

### Flow (typical)
1) `TuiApp` registers routes in `TuiNavigator`.
2) `TuiNavigator.show(route)` calls the registered supplier.
3) Presenter builds a `UiSpec` from state and gateway data.
4) `TuiRenderer.render(spec)` draws a window and blocks until close.

## Testing guide
### Principles
- Test presenters and navigation first; avoid deep Lanterna rendering tests.
- Assert on `UiSpec` contents (labels, buttons, routes) instead of screen output.
- Use fakes for `TrainingCoachGateway` and `TuiNavigator` to keep tests deterministic.
- Avoid terminal dependencies or integration tests; keep everything in-memory.

### Recommended test structure
Create test helpers in `tui/src/test/java/com/training/coach/tui/testsupport/`:
- `FakeTrainingCoachGateway`: configurable responses, records inputs
- `FakeNavigator`: captures last route + last UiSpec
- `UiSpecAsserts`: helper to extract labels/buttons from UiSpec

### Suggested coverage
- Presenters: MainMenu, Session, Connection, PlanGenerateWizard, PlanPreview, UserAdmin, UserList
- Navigation: `TuiNavigator` register/show behavior
- Renderer: optional smoke test only (construct minimal UiSpec and ensure no exceptions)

### Running tests
```bash
./mvnw -pl tui test
```

## Example presenter test (pattern)
```java
class MainMenuPresenterTest {
  @Test
  void adminSeesUserManagement() {
    TuiState state = new TuiState();
    state.setUserRole(UserRole.ADMIN);
    FakeNavigator navigator = new FakeNavigator();

    MainMenuPresenter presenter = new MainMenuPresenter(state, navigator);
    UiSpec spec = presenter.build();

    assertThat(spec.title()).isEqualTo("Training Coach - Main Menu");
    assertThat(UiSpecAsserts.labels(spec)).contains("Role: ADMIN");
    assertThat(UiSpecAsserts.buttons(spec)).contains("Users & Roles");
  }
}
```

## Example test helpers
```java
public final class FakeNavigator extends TuiNavigator {
  private String lastRoute;

  public FakeNavigator() {
    super(new TuiRenderer(null));
  }

  @Override
  public void show(String route) {
    this.lastRoute = route;
  }

  public String lastRoute() {
    return lastRoute;
  }
}
```

```java
public final class FakeTrainingCoachGateway implements TrainingCoachGateway {
  private SystemUser[] users = new SystemUser[0];

  public void withUsers(SystemUser... users) {
    this.users = users;
  }

  @Override
  public SystemUser[] listUsers() {
    return users;
  }

  // Add remaining methods as needed for each presenter test
}
```

```java
public final class UiSpecAsserts {
  public static List<String> labels(UiSpec spec) {
    return spec.components().stream()
        .filter(c -> c instanceof UiLabelSpec)
        .map(c -> ((UiLabelSpec) c).text())
        .toList();
  }

  public static List<String> buttons(UiSpec spec) {
    return spec.components().stream()
        .filter(c -> c instanceof UiButtonSpec)
        .map(c -> ((UiButtonSpec) c).label())
        .toList();
  }
}
```

## Notes
- Keep presenters free of direct Lanterna calls.
- Keep UI text stable when possible to make tests resilient.
- If you add new presenter routes, update `TuiApp` registration and add tests.
