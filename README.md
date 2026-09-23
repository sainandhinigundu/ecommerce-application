# E-Commerce Backend — Member 2 Deliverable (Testing & SonarQube)

A sample Spring Boot backend (User, Product, Cart, Order modules) with a full JUnit 5 +
Mockito unit test suite for the service layer, built specifically to give Member 2
something real to run through SonarQube.

## Project layout

```
src/main/java/com/ecommerce/demo/
  model/        User, Product, Cart, CartItem, Order, OrderItem, OrderStatus
  repository/   Spring Data JPA repositories
  service/      Interfaces
  service/impl/ Business logic (this is what the tests target)
  controller/   REST endpoints
  exception/    Custom exceptions + global handler

src/test/java/com/ecommerce/demo/service/
  UserServiceTest.java     10 tests
  ProductServiceTest.java   8 tests
  CartServiceTest.java     12 tests
  OrderServiceTest.java     9 tests
```

Tests run against an in-memory H2 database (`src/test/resources/application.properties`),
so `mvn test` works without a live MySQL server. The main app still targets MySQL
(`src/main/resources/application.properties`) — update the URL/username/password there
for your own environment.

## What the tests cover

Each service test class mocks its repository dependencies with Mockito and checks:
- **Happy paths**: successful create/read/update/delete operations
- **Not-found cases**: `ResourceNotFoundException` when an id doesn't exist
- **Business rules**: duplicate email rejection, insufficient-stock checks, quantity
  validation, empty-cart order rejection, invalid status transitions, cancelling a
  delivered order
- **Interaction verification**: `verify(...)` calls confirm side effects (e.g. that
  `save()` is *not* called when validation fails)

## Running the tests

```bash
mvn test
```

This generates a Jacoco coverage report at `target/site/jacoco/jacoco.xml` and an
HTML report at `target/site/jacoco/index.html`.

## Wiring up SonarQube

1. Start SonarQube locally (or use SonarCloud) and create a project + token.
2. Add the Sonar Scanner Maven plugin invocation:
   ```bash
   mvn clean verify sonar:sonar \
     -Dsonar.projectKey=ecommerce-backend \
     -Dsonar.host.url=http://localhost:9000 \
     -Dsonar.login=YOUR_TOKEN
   ```
   (`pom.xml` already points `sonar.coverage.jacoco.xmlReportPaths` at the Jacoco
   report so SonarQube picks up test coverage automatically.)
3. In the SonarQube dashboard, check the four pillars: **Bugs, Code Smells,
   Vulnerabilities, Duplications** — plus the **Coverage** percentage from Jacoco.
4. Set a **Quality Gate** (the default "Sonar way" gate is fine to start) and fix
   any flagged issues until the project shows a passing gate — that's the
   deliverable for Member 2.

## Jenkins pipeline snippet (for Member 3/4 integration)

```groovy
stage('Test') {
    steps { sh 'mvn test' }
}
stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('SonarQubeServer') {
            sh 'mvn sonar:sonar'
        }
    }
}
stage('Quality Gate') {
    steps {
        timeout(time: 5, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}
```

## Notes

- Lombok is used to cut boilerplate (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
  — make sure your IDE has the Lombok plugin installed.
- I could not run `mvn test` in this sandbox (no Maven/network available here), so
  double check the build on your machine before treating it as final — the code has
  been hand-reviewed for consistency but hasn't been machine-verified.
