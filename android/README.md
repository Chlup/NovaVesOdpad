# NovaVesOdpad - Android

## Overview
This is the Android version of the NovaVesOdpad app, which provides information about waste collection schedules and sends local notifications to remind users about upcoming collection days.

## Features
- Displays upcoming waste collection days from a calendar file
- Shows which types of bins (mixed waste, plastic, paper, bio) will be collected on each date
- Sends customizable notifications before collection days
- Provides educational information about what waste goes in which bin

## Technical Details
- Built with Kotlin
- Uses Jetpack Compose for UI
- Uses Koin for dependency injection
- Local notifications implemented using AlarmManager
- Structured according to MVVM architecture
- Navigation handled with Jetpack Navigation Compose

## Project Structure
- `/model`: Data models
- `/ui`: UI components and screens organized by feature
  - `/home`: Home screen showing upcoming collection days
  - `/settings`: Settings screen for notification preferences
  - `/trashinfo`: Information screen about waste sorting
- `/service`: Background services like notification scheduling
- `/util`: Utility classes like Logger and TasksManager
- `/di`: Dependency injection modules
- `/navigation`: Navigation-related code

## Data Sources
- App uses a local JSON file for trash collection schedule
- PDF assets provide detailed information about waste sorting

## Getting Started
1. Clone the repository
2. Open the project in Android Studio
3. Build and run on an Android device or emulator

## Build and Run
- Compile and install: `./gradlew installDebug`
- Run tests: `./gradlew test`