# Barber App Backend

# Setup

1. Copy the env file and adjust values if needed:

   ```bash
   cp .env.example .env
   ```

2. Start the PostgreSQL and Mailpit containers:

   ```bash
   docker compose up -d
   ```

   Mailpit is also started automatically together with the app (Spring Boot Docker Compose
   support). Emails sent by the app can be read in the Mailpit web UI: http://localhost:8025

3. Run the app:

   ```bash
   ./mvnw spring-boot:run
   ```

## API docs

Once the application is running (default port 8080):

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

Endpoints are secured with a JWT bearer token. In Swagger UI, click **Authorize** and provide the token as `Bearer <token>`.

## Code style and formatting

This project follows the conventions described by the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), enforced via the [Spotless Maven plugin](https://github.com/diffplug/spotless) with `googleJavaFormat`. To keep the code format consistent, do the following:

### Add pre-commit to githooks

To install the pre-commit hook in the repository, run:

```bash
git config core.hooksPath .githooks
```

This command tells Git to use the hooks located in the `.githooks` folder of the repository. Inside this folder is the `pre-commit` hook, which does the following:

- Checks modified `.java` files against the Spotless format (`mvn spotless:check`).
- If the format is correct, the commit proceeds.
- If formatting issues are found, it automatically formats the files (`mvn spotless:apply`), stages the changes, and continues with the commit.
