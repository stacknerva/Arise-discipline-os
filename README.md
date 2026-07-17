# ARISE – Discipline Operating System

ARISE is a modern Android productivity application designed to build consistency, discipline, and long-term habits. Instead of being just another to-do list, ARISE acts as a personal discipline operating system that helps users stay on track every day.

---

# Features

## Authentication
- Secure Google Sign-In
- Firebase Authentication
- Automatic account persistence
- Sign out support

## Cloud Sync
- Firebase Cloud Firestore
- Automatic cloud backup
- Restore data after reinstalling
- Manual sync option
- Offline-first architecture

## Discipline Routine
- Create a personalized daily routine
- Flexible task scheduling
- Daily progress tracking
- Skip Today feature
- Completion history

## Notifications
- Exact alarm scheduling
- Android notification channels
- User-selectable notification sound
- Silent mode support
- System default notification support
- Custom ringtone picker
- Notification persistence after reboot

## Calendar
- Daily completion tracking
- Habit history
- Progress visualization

## Reports
- Daily discipline report
- Weekly summary
- Productivity insights

## Modern UI
- Clean minimal interface
- Material Design 3
- Dark theme
- Smooth animations
- Responsive layouts

---

# Technology Stack

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database
- Firebase Authentication
- Cloud Firestore
- DataStore Preferences
- Android AlarmManager
- WorkManager
- Coroutines
- Flow
- Material 3

---

# Architecture

```
UI
│
├── Compose Screens
│
├── ViewModels
│
├── Repository
│
├── Room Database
│
├── Firebase Cloud Sync
│
└── Alarm & Notification System
```

---

# Current Modules

- Home
- Calendar
- Reports
- Settings
- Cloud Sync
- Notifications
- Authentication
- Routine Management

---

# Upcoming Features

- Achievement System
- Discipline Levels
- XP System
- Statistics Dashboard
- Advanced Calendar
- Widgets
- Data Export
- Multiple Themes
- Motivational Engine
- Smart Reminder Suggestions
- Focus Mode
- Lock Screen Widgets
- Wear OS Support

---

# Installation

There are two ways to get started with ARISE.

## Option 1 — GUI (Recommended)

1. Download the project as a ZIP from GitHub.
2. Extract the ZIP.
3. Open the project in Android Studio.
4. Add your own `google-services.json`.
5. Configure Firebase Authentication and Firestore.
6. Build and run the project.

---

## Option 2 — CLI

Clone the repository:

```bash
git clone https://github.com/stacknerva/Arise-discipline-os.git
```

Navigate into the project:

```bash
cd Arise-discipline-os
```

Build the project:

```bash
./gradlew assembleRelease
```

or

```bash
./gradlew assembleDebug
```

---

## Firebase Setup

Before running the app:

- Create a Firebase project.
- Register your Android application.
- Add SHA-1 and SHA-256 fingerprints.
- Enable Google Authentication.
- Enable Cloud Firestore.
- Download `google-services.json`.
- Place `google-services.json` in the `app/` module.

---

## Requirements

- Android Studio (GUI workflow)
- Git (CLI workflow)
- JDK 17+
- Android SDK
- Gradle
- Firebase project
  
---

# Permissions

- Notifications
- Exact Alarms
- Internet
- Network State
- Boot Completed

---

# Project Status

Current Version: **Beta**

Core systems completed:

- Authentication
- Cloud Sync
- Routine Engine
- Notification Engine
- Settings
- Reports
- Calendar
- Local Database

Actively under development.

---

# Contributing

Contributions, suggestions, and issue reports are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

---

# License

This project is licensed under the MIT License.

---

# Author

**Gulshan Attri**

Built with Kotlin, Firebase, and Jetpack Compose.

---

## Vision

ARISE is built around one principle:

> Small disciplined actions repeated every day create extraordinary results.

The goal is to become a complete Discipline Operating System that helps users build consistency, eliminate procrastination, and achieve long-term goals through structured daily execution.
