# Project Roadmap: Experiment4J Library

_Last updated: 2026-11-12_

## Vision & Mission

**Vision**: To empower software engineers to refactor legacy systems with confidence by providing a robust framework for live, data-driven experimentation.

**Mission**: To create a simple, powerful, and language-agnostic open-source library that allows developers to safely introduce new implementations by running them as experiments alongside existing code. This will enable them to verify correctness, measure performance, and roll out changes without impacting users.

This project is heavily inspired by the principles behind GitHub's [Scientist](https://github.com/github/scientist) library, and we aim to provide a modern, extensible implementation for a variety of languages.

## Target Audience

This library is for software engineers and teams who are:

- Maintaining and modernizing legacy codebases.
- Performing large-scale refactoring or architectural changes, such as migrating a monolith to microservices.
- Needing to verify the correctness and performance of new implementations against the old.
- Looking for a safer alternative to "big bang" releases.

## Core Concepts

The library is built around the idea of a controlled experiment.

- **Control**: The existing, legacy code path. This is the trusted implementation, and by default its result is returned to the caller.
- **Candidate**: The new, refactored code path. This is the experiment.
- **Experiment**: The process of running both the control and candidate code paths, comparing their results, measuring their performance, and publishing this data for analysis.

Crucially, if the candidate path produces a different result or throws an error, it is recorded and published, but the user is unaffected because, by default, they receive the result from the control path. A `returnChoice` can be configured to return the candidate's result once it has proven itself.

## Key Features

Status: ✅ done · 🟡 partly done · ⬜ not started

| Feature | Description | Priority | Status |
| :------ | :---------- | :------- | :----- |
| Experiment API | A simple, fluent API for defining an experiment, specifying the control and candidate implementations. | High | ✅ |
| Result Comparison | A mechanism to compare the return values of the control and candidate. This will be customizable to handle complex data types. | High | ✅ |
| Performance Tracking | Automatically measure and record the execution time of both the control and candidate paths. | High | ✅ |
| Graceful Error Handling | Any exceptions thrown by the candidate path will be caught, recorded, and will not affect the execution of the control path. | High | ✅ |
| Configurable Rollouts | Control the percentage of requests that will run an experiment, and stop it based on run or mismatch counts. This allows gradual, safe rollouts of the new implementation. | High | 🟡 |
| Candidate Isolation | Time-box the candidate so that a slow or hanging candidate cannot delay the caller, and run it on bounded, shared thread pools. | High | ⬜ |
| Side-Effect Safety | Prevent candidates from writing twice to shared state (read-only / dry-run modes, idempotency hooks). Essential when the candidate is a remote service. | High | ⬜ |
| Experiment State Persistence | Persist experiment definitions, rollout state and run history so that rollouts survive restarts and are shared across instances. Pluggable, with no framework required. | High | ⬜ |
| Automated Ramp-Up & Rollback | Increase candidate traffic step by step while mismatches stay low, and reduce or stop it automatically when they rise. | Medium | 🟡 |
| Context Propagation | Provide a mechanism to pass contextual information (e.g., user ID, request data) into the experiment for more detailed analysis. | Medium | ⬜ |
| Pluggable Publishers | An interface to allow developers to publish experiment data to their preferred monitoring and logging tools (e.g., Prometheus, Datadog, ELK stack, or a simple logger). | Medium | 🟡 |
| Async Support | Support for asynchronous programming models to ensure the library is non-blocking. | Low | ⬜ |

## Technology Stack & Architecture

**Initial Language**: We will start with a Java implementation (Java 21, built with Gradle), as it is a common language for enterprise and legacy systems.

**Architecture**: The library will be designed with a modular architecture, with a clear separation between the core experimentation engine and optional modules:

- `experiment4j-core`: the experimentation engine, with no persistence or framework dependencies and in-memory defaults.
- `experiment4j-jpa`: an optional JPA/Hibernate implementation of the persistence interfaces.
- `experiment4j-jdbc`: an optional plain-JDBC implementation of the persistence interfaces.

Adopters supply their own `DataSource` or `EntityManagerFactory`; the library does not require Spring or any other framework. This separation will also make it easier to port the library to other languages in the future.

## Development Roadmap & Milestones

### Phase 1: Core Engine & First Release

- [x] Implement the core Experiment API.
- [x] Basic result comparison (for primitive types and simple objects).
- [x] Performance measurement for both control and candidate.
- [x] Graceful handling of exceptions in the candidate.
- [x] A default logging publisher (`PrintStreamPublisher`, `StringConsumerPublisher`) and in-memory match counting (`ConcurrentMapMatchCounter`).
- [x] Percentage and sticky (object-hash) sampling (`Selectors`).
- [x] Return choices: control, candidate, fastest, best (`ReturnChoices`).
- [x] Entry guards that stop an experiment by run count, mismatch count, mismatch rate or time window, using running totals shared across trials.
- [ ] Release v0.1.0 with comprehensive documentation for getting started.

### Phase 2: Extensibility, Configuration & Persistence

- [x] Develop the pluggable publisher interface (`Publisher`, usable as a lambda).
- [ ] Implement publishers for common tools such as Micrometer (Prometheus, Datadog, …) and SLF4J.
- [x] Programmatic configuration for enabling/disabling experiments and setting sampling rates.
- [ ] External, reloadable configuration (file or database) for experiments and rollouts.
- [x] Custom result matching logic (`sameWhen`, `exceptionsSameWhen`).
- [x] Restructure into `experiment4j-core`, `experiment4j-jpa` and `experiment4j-jdbc` modules.
- [x] Persistence interfaces in core for experiment definitions and rollout state (with an audit trail of transitions and optimistic locking), with in-memory defaults and reusable contract tests.
- [ ] Persistence interfaces for run results and per-minute metrics.
- [x] Plain-JDBC persistence module (`experiment4j-jdbc`) with an SQL schema script.
- [ ] JPA (Hibernate) persistence module (`experiment4j-jpa`).
- [ ] Write run results asynchronously and in batches, off the request thread.
- [ ] Release v0.2.0.

### Phase 3: Safe Rollouts

- [ ] Candidate timeouts and bounded, shared executors.
- [ ] Side-effect safety for candidates (read-only / dry-run modes).
- [ ] Wire ramp-up strategies (canary, linear, exponential, metric-gated) into trials.
- [ ] Automatic rollback and a kill switch when mismatches exceed a threshold.
- [ ] Experiment lifecycle states (shadow → ramping → candidate-primary → completed / rolled back) with an audit trail of changes.
- [ ] Sticky cohorts and audience targeting (e.g., by user or tenant ID).
- [ ] Release v0.3.0.

### Phase 4: Advanced Features & Community Building

- [ ] Implement context propagation for richer data analysis.
- [ ] Readable mismatch diffs and payload normalization (ignored fields, tolerances).
- [ ] Redact sensitive data (PII) before results are stored or published.
- [ ] OpenTelemetry trace correlation.
- [ ] Develop tooling or integrations for visualizing experiment results.
- [ ] Optional Spring Boot starter (kept separate from core).
- [ ] Create detailed tutorials and example projects.
- [ ] Actively engage with the open-source community for feedback and contributions.

### Phase 5: Multi-Language Support & Long-Term Vision

- [ ] Begin porting the library to other popular languages like Python and C#.
- [ ] Investigate advanced features like automatic payload cleaning for comparison.
- [ ] Establish a long-term governance model for the project.
- [ ] Strive for v1.0.0 release in Java.

### Ongoing: Quality & Release Engineering

- [x] Continuous integration with Gradle on every pull request.
- [x] CodeQL security scanning on every pull request.
- [x] Regression tests accompany every bug fix.
- [x] Code coverage reporting (JaCoCo) with a minimum threshold enforced in CI (core: 98% lines, 95% branches).
- [x] Group ID `io.tumuhairwe` and package `io.experiment4j.exp`.
- [ ] Decide on the version scheme (the build is currently `2.0.0-SNAPSHOT`).
- [ ] Publish releases to Maven Central.

## How to Contribute

This is an open-source project, and we welcome contributions from the community. You can contribute by:

- **Providing Feedback**: Use the library and let us know what you think.
- **Reporting Bugs**: Create detailed issue reports.
- **Contributing Code**: Fork the repository and submit pull requests for new features or bug fixes.
- **Improving Documentation**: Help us make the documentation clearer and more comprehensive.

Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file for more details on how to get involved.
