# Bookshelf Server

Bookshelf is a social reading application for tracking books, reading progress, reviews, friendships, shared shelves, and reading sessions. This repository contains the Spring Boot backend that exposes the REST API, persistence layer, security configuration, and WebSocket infrastructure used by the Bookshelf client.

The goal is to support a reading experience that feels both personal and social: users can maintain their own library, discover progress over time, connect with friends, and participate in shared reading activity.

## Technologies

- Java 17
- Spring Boot 4 with Web MVC, Spring Security, JPA, WebSocket, and H2
- Gradle Wrapper
- MapStruct for DTO/entity mapping
- JUnit 5, Spring Boot Test, Mockito, and JaCoCo
- SonarCloud analysis
- Google App Engine deployment workflow

## High-Level Components

### REST Controllers

The controller layer receives HTTP requests, validates route parameters and request bodies, and delegates business logic to services. Key entry points include [UserController](./src/main/java/ch/uzh/ifi/hase/soprafs26/controller/UserController.java) for registration, login, profiles, stats, leaderboard, activities, and friends; [LibraryController](./src/main/java/ch/uzh/ifi/hase/soprafs26/controller/LibraryController.java) for shelves and shelf books; [SessionController](./src/main/java/ch/uzh/ifi/hase/soprafs26/controller/SessionController.java) for reading sessions; [BookController](./src/main/java/ch/uzh/ifi/hase/soprafs26/controller/BookController.java) and [ReviewController](./src/main/java/ch/uzh/ifi/hase/soprafs26/controller/ReviewController.java) for book detail and review flows.

### Service Layer

The service layer owns the application rules. [UserService](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/UserService.java) handles authentication-related user behavior, [LibraryService](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/LibraryService.java) manages shelves/books/shared library access, [SessionService](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/SessionService.java) calculates reading session state and progress, [FriendService](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/FriendService.java) handles social relationships, and [NotificationService](./src/main/java/ch/uzh/ifi/hase/soprafs26/service/NotificationService.java) creates user-facing events.

### Persistence Model

Domain state is represented by JPA entities in [src/main/java/ch/uzh/ifi/hase/soprafs26/entity](./src/main/java/ch/uzh/ifi/hase/soprafs26/entity), including users, books, shelves, shelf books, sessions, participants, reviews, friendships, activities, notifications, and leaderboard entries. Repository interfaces in [src/main/java/ch/uzh/ifi/hase/soprafs26/repository](./src/main/java/ch/uzh/ifi/hase/soprafs26/repository) provide the database access used by services.

### DTO Mapping and API Boundaries

The REST API does not expose entities directly. Request/response DTOs live in [src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto](./src/main/java/ch/uzh/ifi/hase/soprafs26/rest/dto), and [DTOMapper](./src/main/java/ch/uzh/ifi/hase/soprafs26/rest/mapper/DTOMapper.java) converts between DTOs and entities with MapStruct.

### Security and Real-Time Messaging

[SecurityConfig](./src/main/java/ch/uzh/ifi/hase/soprafs26/SecurityConfig.java) and [AuthTokenFilter](./src/main/java/ch/uzh/ifi/hase/soprafs26/AuthTokenFilter.java) configure token-based request authentication. [WebSocketConfig](./src/main/java/ch/uzh/ifi/hase/soprafs26/WebSocketConfig.java) enables STOMP over SockJS at `/ws`, with `/topic` and `/queue` broker destinations for real-time notifications and session updates.

## Launch & Deployment

### Prerequisites

- Java 17
- No global Gradle installation is required; use the included Gradle Wrapper.
- The application uses an in-memory H2 database by default, so no external database is needed for local development.

### Local Setup

```bash
./gradlew build
./gradlew bootRun
```

The server runs on [http://localhost:8080](http://localhost:8080). The H2 console is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) while the server is running.

Use these local H2 settings from [src/main/resources/application.properties](./src/main/resources/application.properties):

```text
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password:
```

### Tests and Reports

```bash
./gradlew test
./gradlew jacocoTestReport
```

For the same quality checks used by CI:

```bash
./gradlew test jacocoTestReport sonar
```

The `sonar` task requires the `SONAR_TOKEN` environment variable in CI or your local shell.

### Development Mode

For automatic rebuilds during development, run these in two terminals:

```bash
./gradlew build --continuous -xtest
./gradlew bootRun
```

### Docker

```bash
docker build -t bookshelf-server .
docker run -p 8080:8080 bookshelf-server
```

### Releases

The repository includes a Google App Engine deployment workflow in [.github/workflows/main.yml](./.github/workflows/main.yml). Pushes to `main` run the configured CI/deployment pipeline when `GCP_SERVICE_CREDENTIALS`, `SONAR_TOKEN`, and the standard GitHub token are available. App Engine runtime settings are defined in [app.yaml](./app.yaml).

The Docker workflow can also publish a container image when Docker Hub secrets are configured.

## Roadmap

- Replace the local in-memory H2 setup with a persistent production database configuration and migration strategy.
- Add richer social challenge endpoints, such as reading goals, streaks, and friend competitions.
- Expand WebSocket usage so active shared sessions show participant progress in real time.

## Authors and Acknowledgment

Built by SoPra FS26 Group 35:

- [@juylein](https://github.com/juylein)
- [@missbo-cyber](https://github.com/missbo-cyber)
- [@vanmey](https://github.com/vanmey)
- [@fraiaperezrayonforsman-cloud](https://github.com/fraiaperezrayonforsman-cloud)
- [@minhgou](https://github.com/minhgou)

This project was developed for the Software Engineering Lab at the University of Zurich. We acknowledge the SoPra teaching team and the maintainers of Spring Boot, Gradle, H2, MapStruct, and the other open-source libraries used here.

## License

This project is licensed under the Apache License 2.0. See [LICENSE](./LICENSE) for the full license text.
