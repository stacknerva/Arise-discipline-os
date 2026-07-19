# ARISE

> **A Discipline Operating System for Android**

ARISE is built for one purpose: helping you execute your daily routine with consistency.

This is not a motivation app.
This is not a habit tracker filled with badges and feel-good quotes.

ARISE holds you accountable. Every day is either completed or wasted.

---

# Features

## Daily Discipline System

- Build and manage your daily routine
- Time-based task progression
- Automatic current task tracking
- Daily completion tracking
- Skip Today mode with full-day behavior
- Calendar history

## Smart Notifications

- Exact alarm reminders
- Custom notification sounds
- System ringtone picker
- Silent mode support
- Reliable reminder scheduling

## Reports & Statistics

- Daily reports
- Weekly reports
- Completion history
- Skipped day tracking
- Productivity insights

## Cloud Sync

- Google Sign-In
- Firebase Authentication
- Cloud Firestore synchronization
- Automatic backup
- Restore data after reinstalling

## Modern Interface

- Material Design 3
- Dark Mode
- Smooth animations
- Responsive layouts

---

# Philosophy

Discipline isn't built by motivation.

It is built by showing up every day.

ARISE treats every day as a commitment.

A completed day moves you forward.

A skipped day is recorded as a day that can never be recovered.

No fake rewards.

No meaningless streaks.

No shortcuts.

Just accountability.

---

# Technology Stack

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database
- Firebase Authentication
- Cloud Firestore
- DataStore
- Coroutines
- Flow
- AlarmManager
- WorkManager
- Material Design 3

---

# Installation

## Option 1 — APK (Recommended)

If you simply want to use ARISE, download the latest APK from the **Releases** section on GitHub.

1. Open **Releases**.
2. Download the latest APK.
3. Install it on your Android device.
4. Sign in and start building your routine.

---

## Option 2 — Build from Source

Clone the repository:

```bash
git clone https://github.com/stacknerva/Arise.git
```

Open the project in Android Studio or your preferred IDE.

Configure Firebase:

- Enable Authentication
- Enable Cloud Firestore
- Add SHA-1 and SHA-256
- Place `google-services.json` inside the `app/` directory

Build the project:

```bash
./gradlew assembleDebug
```

or

```bash
./gradlew assembleRelease
```

---

# Requirements

- Android 8.0+
- Android Studio (for source builds)
- JDK 17+
- Firebase project (for source builds)

---

# Permissions

- Notifications
- Exact Alarms
- Internet
- Network State
- Boot Completed

---

# Project Status

**Version 1.0.0**

Core systems are complete:

- Daily Routine Engine
- Notification System
- Google Authentication
- Cloud Synchronization
- Calendar
- Reports
- Settings
- Local Database

Active development continues with new features and improvements.

---

# Roadmap

- Achievement System
- Discipline Levels
- Advanced Statistics
- Widgets
- Data Export
- Multiple Themes
- Focus Mode
- AI-powered Discipline Insights
- Wear OS Support

---

# Contributing

Contributions, bug reports, feature requests, and pull requests are welcome.

---

# License

Licensed under the MIT License.

---

# Author

**Gulshan Attri**

---

# Vision

The goal of ARISE is simple:

Create the operating system for discipline.

Every reminder exists for a reason.

Every completed task matters.

Every skipped day has a cost.

Because the life you want is built one day at a time.
