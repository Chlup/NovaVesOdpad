# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

NovaVesOdpad is a cross-platform mobile application for tracking trash collection schedules in Nova Ves. The app displays upcoming trash collection days, allows users to configure notifications, and provides information about different types of waste.

The project consists of two platform-specific implementations:
- Android app (using Kotlin, Jetpack Compose, Koin)
- iOS app (using Swift, SwiftUI, Factory)

Both implementations share a similar architecture and functionality, with platform-specific implementations.

## Build Commands

### Android

```bash
# Navigate to the Android project directory
cd android

# Build the project
./gradlew build

# Install on a connected device or emulator
./gradlew installDebug

# Run tests
./gradlew test

# Check for lint issues
./gradlew lint
```

### iOS

```bash
# Navigate to the iOS project directory
cd ios

# Build and run using Xcode
xcodebuild -scheme NovaVesOdpad -configuration Debug build

# Or open the project in Xcode
open NovaVesOdpad.xcodeproj
```

## Architecture Overview

### Data Models

- `TrashDay`: Represents a specific day when trash is collected, with a date and list of bin types
- `Bin`: Enum representing different types of waste bins (mix, plastic, paper, bio)
- `NotificationHour`: Model for storing notification preferences
- `TrashInfoSection`: Model for trash information content

### Core Components

#### Android

- **Dependency Injection**: Uses Koin (see `appModule` in `AppModule.kt`)
- **Navigation**: Uses Jetpack Navigation Compose (see `AppDestination.kt`)
- **ViewModels**: State management via ViewModels (e.g., `HomeViewModel`, `SettingsViewModel`)
- **Notifications**: Implemented via `NotificationsBuilder` and `NotificationReceiver`

#### iOS

- **Dependency Injection**: Uses Factory for DI
- **Navigation**: Uses a coordinator pattern (see `GlobalCoordinator`)
- **Models**: Observable models (e.g., `HomeModel`, `SettingsModel`)
- **Notifications**: Implemented via `NotificationsBuilder` and the `UNUserNotificationCenter` delegate

### Key Features

1. **Home Screen**: Displays upcoming trash collection days with bin types
2. **Settings Screen**: Allows configuring notification preferences
3. **Trash Info Screen**: Provides information about different waste types
4. **Notifications**: Sends reminders before trash collection days

### Data Sources

- Trash collection schedule is stored in `calendar.json`
- Trash information resources are stored as PDFs in the assets directory

## Common Tasks

### Adding a New Trash Type

1. Add the new type to the `Bin` enum in both Android (`TrashDay.kt`) and iOS (`TashDay.swift`)
2. Update the associated properties (title, color)
3. Update the UI components that render bins in both platforms

### Modifying the Collection Schedule

Update the `calendar.json` file in both platforms:
- Android: `android/app/src/main/assets/calendar.json`
- iOS: `ios/Resources/calendar.json`

### Customizing Notifications

Modify the notification implementation in:
- Android: `NotificationsBuilder.kt` and `NotificationReceiver.kt`
- iOS: `NotificationsBuilder.swift`