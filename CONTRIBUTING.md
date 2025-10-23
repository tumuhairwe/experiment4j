##Contributing to the Experiment4J

First off, thank you for considering contributing! We're excited to have you join our community. This project thrives on the contributions of developers like you, and we appreciate you taking the time to help us build a great tool for the software engineering community. 

This document provides guidelines for contributing to the project.

##Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Your First Code Contribution](#your-first-code-contribution)
  - [Pull Request](#pull-requests)
- [Style Guides](#style-guides)
  - [Git Commit Messages](#git-commit-messages)
  - [Java Style Guide](#java-style-guide)

<h3>Code of Conduct</h3>

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior.

<h3>How Can I Contribute?</h3>

<h4>Reporting Bugs</h4>

If you find a bug, please ensure the bug was not already reported by searching on GitHub under Issues.

If you're unable to find an open issue addressing the problem, open a new one. Be sure to include a title and clear description, as much relevant information as possible, and a code sample or an executable test case demonstrating the expected behavior that is not occurring.

<h4>Suggesting Enhancements</h4>

If you have an idea for an enhancement, please search the Issues to see if it has been discussed before. If not, open a new issue to start a discussion. This allows us to coordinate our efforts and prevent duplication of work.

<h4>Your First Code Contribution</h4>

Unsure where to begin contributing? You can start by looking through good first issue and help wanted issues:

Good first issue - issues which should only require a few lines of code, and a test or two.

Help wanted - issues which should be a bit more involved than good first issue issues.

<h4>Pull Requests</h4>

We welcome pull requests! To submit a change, please follow these steps:
<ol>
<li>Fork the repository on GitHub.</li>
<li>Create a new branch from main for your changes. Please give it a descriptive name (e.g., feature/add-prometheus-publisher, fix/result-comparison-bug).</li>
<li>Make your changes in your branch. Add or update tests as appropriate.</li>
<li>Ensure the test suite passes locally (`mvn clean verify`) and your code adheres to the style guides outlined below.</li>
<li>Commit your changes with a clear and descriptive commit message (see our Git Commit Messages style guide).</li>
<li>Push your changes to your forked repository.</li>
<li>Create a pull request from your branch to the main repository's main branch. Include a detailed description of your changes and reference any relevant issues.</li>
<li>Link the PR to any relevant issues.</li>
</ol>

A maintainer will review your PR. We may suggest some changes or improvements. Once your PR is approved, it will be merged.

<h2>Style Guides</h2>

<h3>Git Commit Messages</h3>
<ol>
<li>Use the present tense ("Add feature" not "Added feature").</li>
<li>Use the imperative mood ("Move cursor to..." not "Moves cursor to...").</li>
<li>Separate subject from body with a blank line.</li>
<li>Wrap the body at 72 characters or less.</li>
<li>Use the body to explain what and why vs. how.</li>
<li>Reference issues and pull requests liberally after the first line.</li>
</ol>

<h3>Java Style Guide</h3>
<ol>
<li>We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)</li>
<li>Use meaningful variable and method names.</li>
<li>Keep methods short and focused on a single task.</li>
<li>Please ensure your code is well-commented, especially for complex logic.</li>
<li>All new features and bug fixes must be accompanied by unit test.</li>
</ol>

Thank you again for your contribution!