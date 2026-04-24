# AlphaStick

AlphaStick is an Android security auditing and threat analysis application designed to evaluate installed applications using structured, explainable heuristics. It provides users and developers with transparent insights into potential security risks, misconfigurations, and privacy concerns within their application environment.

Unlike conventional permission viewers or opaque “risk score” tools, AlphaStick emphasizes **modularity, explainability, and reduced false positives**, making it suitable for both educational and practical auditing scenarios.

---

## Overview

AlphaStick performs on-device analysis of installed applications by combining static inspection with behavioral inference. It evaluates multiple security signals, aggregates them through a deterministic scoring model, and presents findings in a structured and explainable format.

The system is designed to:

* Identify insecure application configurations
* Highlight privacy-sensitive behaviors
* Reduce false positives through contextual validation
* Provide actionable mitigation guidance

---

## Core Features

### Modular Security Scanning Engine

* Decoupled scanning architecture using a unified `AppScanner` interface
* Independent scanners for:

  * Permissions
  * Manifest flags
  * Usage behavior (telemetry)
  * Installer source
  * Cryptographic signatures
* Centralized orchestration via a `ScanOrchestrator`

### Explainable Risk Scoring

* Deterministic scoring model with clearly defined contributing factors
* Each result includes:

  * Total risk score (0–100)
  * Individual risk factors with score impact
  * Severity classification
* Eliminates black-box behavior by exposing reasoning behind every score

### Structured Security Findings

* Unified data model (`SecurityFinding`) across all scanners
* Each finding includes:

  * Title and description
  * Severity level (INFO, LOW, MEDIUM, HIGH, CRITICAL)
  * Confidence level (LOW, MEDIUM, HIGH)
  * Root cause explanation
  * Mitigation guidance

### Zombie Tracker Detection (Behavioral Analysis)

* Identifies applications that:

  * Hold sensitive permissions
  * Remain unused over extended periods
* Flags potential background data collection risks
* Incorporates contextual filtering to reduce false positives

### Manifest and Configuration Analysis

* Detects critical application-level vulnerabilities:

  * Debuggable builds in production
  * Backup exposure risks
  * Cleartext network traffic usage
  * Outdated target SDK versions

### Installer Source Intelligence

* Classifies application origin using installer package analysis
* Distinguishes between:

  * Trusted sources (e.g., Play Store, OEM stores)
  * Unknown or sideloaded installations
* Includes OEM trust whitelist to prevent misclassification

### Cryptographic Signature Auditing

* Extracts and processes application signing certificates
* Generates SHA-256 signature hashes for verification
* Enables identification of repackaged or tampered applications

### False Positive Mitigation

* Context-aware validation system
* Adjusts severity and confidence based on:

  * System application status
  * Trusted installer sources
* Prevents misleading alerts for legitimate applications

### JSON Audit Export

* Exports full scan results in structured JSON format
* Enables sharing with security teams or further analysis
* Includes all findings, scores, and metadata

---

## Architecture

AlphaStick follows a strict Clean Architecture approach with clear separation of concerns:

* **Presentation Layer**

  * Jetpack Compose (Material 3)
  * State-driven UI using StateFlow
  * Lifecycle-aware updates

* **Domain Layer**

  * Risk scoring engine
  * Security models and business logic
  * Scanner orchestration

* **Data Layer**

  * Android system interfaces (PackageManager, UsageStatsManager)
  * Data extraction and transformation

### Key Design Principles

* Modularity and extensibility
* Deterministic and explainable logic
* Minimal external dependencies
* Clear separation between data, domain, and UI layers

---

## Technology Stack

* Kotlin
* Jetpack Compose (Material 3)
* Clean Architecture
* MVVM (Model-View-ViewModel)
* Dagger Hilt (Dependency Injection)
* Kotlin Coroutines and StateFlow

---

## Use Cases

* Auditing installed applications for security misconfigurations
* Identifying privacy risks in dormant or background-active apps
* Educational exploration of Android application security concepts
* Demonstrating modular security analysis architecture

---

## Limitations

AlphaStick is a **static and heuristic-based auditing tool**. It does not:

* Perform real-time network traffic inspection
* Execute dynamic malware analysis
* Replace antivirus or endpoint protection solutions

The results should be interpreted as **risk indicators**, not definitive malware detection.

---

## Project Status

This project is currently under active development. Features and detection logic are continuously being refined to improve accuracy and reduce false positives.

---

## License

This project is licensed under the MIT License.
