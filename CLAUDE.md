# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands
- Build project: `xcodebuild -project NovaVesOdpad.xcodeproj -scheme NovaVesOdpad build`
- Run app: Open in Xcode and press Cmd+R
- Run tests: Open in Xcode and press Cmd+U

## Code Structure & Architecture
This project implements a waste collection schedule app using a MVVM-like architecture with these key components:

- **Coordinator Pattern**: Navigation is managed through a coordinator hierarchy
  - `GlobalCoordinator`: Manages the main navigation stack
  - Feature-specific coordinators (HomeCoordinator, TrashInfoCoordinator)
- **Model-View Pattern**: Each feature has:
  - Protocol-based Models (`HomeModel`, `TrashInfoModel`)
  - Implementation classes (`HomeModelImpl`, `TrashInfoModelImpl`)
  - SwiftUI Views (`HomeView`, `TrashInfoView`)

## Dependency Injection
- Uses the Factory library for dependency injection
- Dependencies are registered as singletons in Container extensions
- Dependencies are injected using `@Injected` property wrapper

## Concurrency Handling
- Uses structured concurrency with Swift's async/await
- `TasksManager` handles task creation, management, and cancellation
- Most model classes are marked with `@MainActor` to ensure UI updates happen on the main thread
- Uses `@Observable` macro for SwiftUI state management

## Navigation System
- Navigation uses SwiftUI's NavigationStack
- Navigation destinations defined in `AppDestination` enum
- Coordinator pattern for managing navigation flow

## Code Style Guidelines
- **Models**: 
  - Define protocol for each model
  - Implementation class with "Impl" suffix
  - Use `@MainActor` and `@Observable` attributes
- **Dependency Providers**:
  - Define protocol for each service
  - Implementation with same name + "Impl"
  - Register in Container extension
- **SwiftUI Views**:
  - Accept model as constructor parameter
  - Observe model changes through SwiftUI observation system

## Project Organization
- `/Sources`: All Swift code
  - `/Features`: Feature modules (Home, Settings, TrashInfo)
  - `/Dependencies`: Shared services (Logger, TasksManager)
  - `/Coordinator`: Navigation management
  - `/UIComponents`: Reusable UI components
- `/Resources`: Assets, data files, etc.