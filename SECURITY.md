# Security Policy

## Supported Versions

We support security updates for the following versions of `group-theory-lib`:

| Version | Supported          | JDK Target |
| :------ | :----------------- | :--------- |
| `3.x`   | :white_check_mark: | Java 25+   |
| `2.x`   | :x:                | Java 25    |
| `< 2.0` | :x:                | Java 8     |

---

## Reporting a Vulnerability

We take the security of `group-theory-lib` seriously—especially around off-heap Foreign Function & Memory (`java.lang.foreign`) operations and algorithmic denial-of-service vectors. If you discover a security vulnerability, please report it responsibly:

1. **Do not** report security vulnerabilities through public GitHub issues or discussions.
2. Please use [GitHub Private Vulnerability Reporting](https://github.com/lock14/group-theory-lib/security/advisories/new) on this repository.
3. If private vulnerability reporting is unavailable, contact the maintainers directly.

### What to Include in Your Report

To help us triage and resolve the issue quickly, please include:
* A detailed description of the vulnerability.
* Steps to reproduce the issue or a minimal Java 25 reproducer.
* Impact assessment (e.g., native memory safety in `Float64Matrix`, unbounded allocation / algorithmic complexity DoS).
* Any suggested fixes or mitigations.

### Response Timeline

* We will acknowledge receipt of your vulnerability report within **48 hours**.
* We will provide a status update and estimated timeline for a fix within **7 days**.
* Once a fix is verified, a patch release and security advisory will be published.
