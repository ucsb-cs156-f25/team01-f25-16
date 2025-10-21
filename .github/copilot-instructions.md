# Spring Boot Team01 Application

This is a Spring Boot 3.4.3 backend application using Java 21, Maven, H2 database for development, and Google OAuth2 authentication. The application provides REST API endpoints with Swagger documentation and supports both unit and integration testing with 100% coverage requirements.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Bootstrap and Build
Set up Java 21 environment and build the application:
```bash
# Set up Java 21 (required - application will not compile with Java 17)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Copy environment file (required for local development)
cp .env.SAMPLE .env

# Clean and compile - takes 45 seconds to 1 minute. NEVER CANCEL.
mvn clean compile

# Full build with tests - takes 45 seconds to 1 minute. NEVER CANCEL.
mvn test

# Package application - takes 40 seconds. NEVER CANCEL.
mvn package
```

### Testing Commands
Run comprehensive testing suite:
```bash
# Unit tests - takes 45 seconds. NEVER CANCEL. Set timeout to 90+ seconds.
mvn test

# Integration tests - takes 35 seconds but requires Playwright browsers. NEVER CANCEL.
INTEGRATION=true mvn test-compile failsafe:integration-test failsafe:verify

# Mutation testing - takes 4 minutes. NEVER CANCEL. Set timeout to 6+ minutes.
mvn pitest:mutationCoverage

# Generate code coverage report - takes under 1 second
mvn jacoco:report
```

### Run the Application
Start the application for development:
```bash
# Method 1: Using Maven (preferred for development)
mvn spring-boot:run

# Method 2: Using packaged JAR
java -jar target/team01-1.0.0.jar
```

Application will be available at:
- **Homepage**: http://localhost:8080/
- **Swagger API**: http://localhost:8080/swagger-ui/index.html
- **H2 Console**: http://localhost:8080/h2-console (username: sa, password: password)

## Critical Timing and Timeout Guidelines

**NEVER CANCEL** any of these long-running commands:

- **Compilation**: 45-60 seconds - Set timeout to 120+ seconds
- **Unit Tests**: 45 seconds - Set timeout to 90+ seconds  
- **Integration Tests**: 35 seconds - Set timeout to 60+ seconds
- **Mutation Testing**: 4 minutes - Set timeout to 6+ minutes
- **Package Build**: 40 seconds - Set timeout to 90+ seconds

All these commands must complete successfully for the CI pipeline to pass.

## Handling workflow failures

* Any time workflow 12-backend-jacoco.yml fails, it means you need to look at the Jacoco output to discover which lines of code are not covered by tests. Please write additional tests to cover the uncovered lines, until you have 100% test coverage.
* Any time workflow 35-frontend-format.yml fails, please `cd` into `frontend` and use `npm run format` to fix this.
* Any time workflow 36-frontend-eslint.yml fails, look at the messages and use those to make the necessary changes to the code so that the workflow will pass.
* Any time workflow 32-frontend-coverage.yml files, it is because you created or edited code under the frontend directory in this project, and did not add tests for the new code.  When that happens, please check the coverage reports which can be found in `coverage/index.html` and follow up by writing tests to get to 100% coverage.

## Validation Requirements

### Manual Testing Scenarios
After making changes, ALWAYS validate:

1. **Application Startup**: Ensure `mvn spring-boot:run` starts without errors
2. **Homepage Access**: Verify http://localhost:8080/ loads and shows navigation links
3. **Swagger UI**: Confirm http://localhost:8080/swagger-ui/index.html is accessible
4. **H2 Console**: Test database console at http://localhost:8080/h2-console
5. **API Endpoints**: Use Swagger UI to test at least one GET endpoint

### Coverage Requirements
This application enforces 100% test coverage:
- **Jacoco**: 100% line and branch coverage required
- **Pitest**: 100% mutation coverage required
- Both must pass for successful builds

### Integration Test Requirements
Integration tests use Playwright and require browser drivers:
- Tests may fail locally if Playwright browsers not installed
- Command: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" -D exec.classpathScope=test`
- Browser installation can fail due to network issues - this is a known limitation

## Environment Configuration

### Required Environment Variables (.env file)
Copy `.env.SAMPLE` to `.env` and configure:
```
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
ADMIN_EMAILS=admin@example.com
```

### Maven Profiles
Application supports multiple runtime profiles:
- **localhost** (default): H2 database, development mode
- **wiremock**: Development with mocked services (`WIREMOCK=true mvn spring-boot:run`)
- **integration**: Integration testing profile (`INTEGRATION=true`)
- **production**: Production deployment (`PRODUCTION=true`)

## Key Technologies and Frameworks

- **Java**: 21 (required - will not compile with Java 17)
- **Spring Boot**: 3.4.3
- **Database**: H2 (development), PostgreSQL (production)
- **Authentication**: Google OAuth2
- **Testing**: JUnit 5, Mockito, Playwright
- **Build Tool**: Maven
- **Database Migration**: Liquibase

## Project Structure

### Important Directories
```
├── src/main/java/               # Application source code
├── src/test/java/               # Unit and integration tests
├── docs/                        # Documentation files
├── target/                      # Build artifacts (JAR, reports, H2 database)
├── .env.SAMPLE                  # Environment variables template
└── pom.xml                     # Maven configuration
```

### Key Files
- **Main Application**: `src/main/java/edu/ucsb/cs156/example/ExampleApplication.java`
- **Controllers**: `src/main/java/edu/ucsb/cs156/example/controllers/`
- **Entities**: `src/main/java/edu/ucsb/cs156/example/entities/`
- **Unit Tests**: `src/test/java/edu/ucsb/cs156/example/controllers/`
- **Integration Tests**: `src/test/java/edu/ucsb/cs156/example/integration/`

## Common Issues and Solutions

### Build Failures
- **Java Version**: Ensure Java 21 is installed and JAVA_HOME is set correctly
- **Missing .env**: Copy `.env.SAMPLE` to `.env` before running application
- **Database Lock**: Run `mvn clean` to reset H2 database if needed

### Test Failures
- **Integration Tests**: May fail if Playwright browsers not installed
- **Coverage**: Must maintain 100% coverage - add tests for any new code
- **Mutation Testing**: Must achieve 100% mutation score

### Application Issues
- **OAuth Login**: Requires valid Google OAuth credentials in `.env` file
- **Port Conflicts**: Application uses port 8080 - ensure it's available
- **Database**: H2 database files stored in `target/` directory

## Validation Checklist

Before committing changes, verify:
- [ ] `mvn test` passes (45 seconds)
- [ ] `mvn package` succeeds (40 seconds)  
- [ ] `mvn pitest:mutationCoverage` achieves 100% (4 minutes)
- [ ] Application starts with `mvn spring-boot:run`
- [ ] Homepage accessible at http://localhost:8080/
- [ ] Swagger UI loads at http://localhost:8080/swagger-ui/index.html
- [ ] At least one API endpoint tested via Swagger UI

## GitHub Actions Integration

CI workflows expect these exact timeout values:
- **Unit Tests**: 10 minutes maximum in CI
- **Integration Tests**: 10 minutes maximum in CI  
- **Mutation Testing**: 10 minutes maximum in CI

All commands listed above are validated to complete within these timeframes.