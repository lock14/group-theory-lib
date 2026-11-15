# Security policy

## Supported versions

Security updates are provided for the following versions of `group-theory-lib`:

| Version | Supported | JDK target |
| :--- | :--- | :--- |
| `3.x` | Yes | Java 25+ |
| `2.x` | No | Java 25 |
| `< 2.0` | No | Java 8 |

## Reporting a vulnerability

If you discover a security vulnerability in `group-theory-lib`—such as native memory safety issues in Foreign Function and Memory (`java.lang.foreign`) operations or unbounded allocation and algorithmic denial-of-service vectors—report it privately using the following steps:

1.  Do not report security vulnerabilities through public GitHub issues or discussions.
2.  Submit a report using [GitHub Private Vulnerability Reporting](https://github.com/lock14/group-theory-lib/security/advisories/new) on this repository.
3.  If private vulnerability reporting is unavailable, contact the maintainer directly.

### What to include in your report

Include the following details to help triage and resolve the issue:

*   A clear description of the vulnerability and affected package or type.
*   Numbered steps or a minimal Java 25 snippet to reproduce the issue.
*   An impact assessment (such as native memory safety in `Float64Matrix` or unbounded allocation complexity).
*   Any suggested fixes or mitigations.

### Response timeline

*   Receipt of a vulnerability report is acknowledged within 48 hours.
*   A status update and estimated fix timeline are provided within 7 days.
*   Once a fix is verified, a patch release and security advisory are published.
