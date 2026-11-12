# Contributing to Experiment4J

First off, thank you for considering contributing! We're excited to have you join our community. This project thrives on the contributions of developers like you, and we appreciate you taking the time to help us build a great tool for the software engineering community.

This document provides guidelines for contributing to the project. To see what we are working on and what is planned, read the [Roadmap](ROADMAP.md).

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Your First Code Contribution](#your-first-code-contribution)
  - [Development Setup](#development-setup)
  - [Pull Requests](#pull-requests)
- [Style Guides](#style-guides)
  - [Branch Names](#branch-names)
  - [Pull Request Titles](#pull-request-titles)
  - [Git Commit Messages](#git-commit-messages)
  - [Java Style Guide](#java-style-guide)

## Code of Conduct

This project and everyone participating in it is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behaviour to the project maintainer, [@tumuhairwe](https://github.com/tumuhairwe).

## How Can I Contribute?

### Reporting Bugs

If you find a bug, please ensure the bug was not already reported by searching on GitHub under [Issues](https://github.com/tumuhairwe/experiment4j/issues).

If you're unable to find an open issue addressing the problem, open a new one. Be sure to include a title and clear description, as much relevant information as possible, and a code sample or an executable test case demonstrating the expected behavior that is not occurring.

### Suggesting Enhancements

If you have an idea for an enhancement, please search the [Issues](https://github.com/tumuhairwe/experiment4j/issues) to see if it has been discussed before, and check the [Roadmap](ROADMAP.md). If not, open a new issue to start a discussion. This allows us to coordinate our efforts and prevent duplication of work.

### Your First Code Contribution

Unsure where to begin contributing? You can start by looking through these issues:

- [Good first issues](https://github.com/tumuhairwe/experiment4j/labels/good%20first%20issue): issues which should only require a few lines of code, and a test or two.
- [Help wanted](https://github.com/tumuhairwe/experiment4j/labels/help%20wanted): issues which should be a bit more involved than good first issues.

### Development Setup

- **JDK 21**. The build uses a Gradle toolchain for Java 21.
- **Gradle**: use the wrapper included in the repository (`./gradlew`); no separate installation is needed.
- **Lombok**: the code uses [Lombok](https://projectlombok.org/). Enable annotation processing in your IDE (IntelliJ IDEA: *Settings → Build, Execution, Deployment → Compiler → Annotation Processors*).

Build the project and run the tests with:

```sh
./gradlew build
```

### Pull Requests

We welcome pull requests! To submit a change, please follow these steps:

1. Fork the repository on GitHub.
2. Create a new branch from `master` for your changes, named as described in [Branch Names](#branch-names).
3. Make your changes in your branch. Add or update tests as appropriate. Every bug fix must include a regression test that fails without the fix.
4. Ensure the build passes locally (`./gradlew build`) and your code follows the [style guides](#style-guides) below.
5. Commit your changes with a clear and descriptive commit message (see [Git Commit Messages](#git-commit-messages)).
6. Push your changes to your forked repository.
7. Create a pull request from your branch to the `master` branch, with a title as described in [Pull Request Titles](#pull-request-titles). Include a detailed description of your changes and how you tested them.
8. Link the pull request to any relevant issues.

Before a pull request can be merged, the **Gradle build** and **CodeQL** security scan must pass. A maintainer will review your pull request and may suggest some changes or improvements. Once it is approved, it will be merged.

Please keep each pull request focused on a single change. Unrelated fixes or refactoring belong in a separate pull request.

## Style Guides

### Branch Names

Prefix the branch name with the type of change, followed by a short, descriptive name in kebab-case:

| Type | Prefix | Example |
| :--- | :----- | :------ |
| New feature | `feature/` | `feature/add-prometheus-publisher` |
| Bug fix | `bug/` | `bug/publisher-invoked-twice` |
| Other tasks (build, docs, CI, refactoring) | `chore/` | `chore/gradle-only` |

Name a bug branch after the problem, not the fix (e.g. `bug/publisher-invoked-twice`, not `bug/remove-accept-call`).

### Pull Request Titles

Start the title with the type of change, followed by a summary in the imperative mood:

- `Bug: Make ExactCountGuard(N) allow at most N runs`
- `Feat: Add Prometheus publisher`
- `Chore: Remove Maven build in favour of Gradle`
- `Other: ...` for anything that doesn't fit the types above

### Git Commit Messages

1. Use the present tense ("Add feature" not "Added feature").
2. Use the imperative mood ("Move cursor to..." not "Moves cursor to...").
3. Separate subject from body with a blank line.
4. Wrap the body at 72 characters or less.
5. Use the body to explain what and why vs. how.
6. Reference issues and pull requests liberally after the first line.

### Java Style Guide

1. We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) (2-space indentation). The rules are in [`checkstyle.xml`](checkstyle.xml) at the root of the repository.
2. Use meaningful variable and method names.
3. Keep methods short and focused on a single task.
4. Please ensure your code is well-commented, especially for complex logic.
5. All new features and bug fixes must be accompanied by unit tests.
6. Keep `experiment4j-core` free of framework and persistence dependencies; integrations (persistence, metrics, Spring, …) belong in their own modules, such as `experiment4j-jpa` and `experiment4j-jdbc`.

Thank you again for your contribution!
