# AGENTS.md

This file provides quick guidance for automated agents working in this repo.

## Project layout
- Root `pom.xml` is an aggregator (packaging: `pom`).
- `yudao-server` is the main Spring Boot app.
- `yudao-framework` and `yudao-module-*` are backend modules.
- `yudao-ui/*` contains frontend apps (separate build toolchains).

## Requirements
- JDK 8 (see `pom.xml` -> `java.version=1.8`).
- Maven 3.8+.

## Build
- Build all modules without tests:
  - `mvn -DskipTests package`
- Build a specific module and its dependencies:
  - `mvn -pl yudao-server -am package`

## Test
- Run tests for a module:
  - `mvn -pl yudao-server -am test`
  - `mvn -pl yudao-module-system -am test`

## Run (server)
- `mvn -pl yudao-server -am spring-boot:run`
- Or run `YudaoServerApplication` in your IDE.

## Notes
- Several optional modules are commented out in root `pom.xml`; uncomment to include them.
- Server config files live in `yudao-server/src/main/resources/application*.yaml`.
