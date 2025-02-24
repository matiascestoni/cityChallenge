# Project Overview

## Goal

The primary goal of this project was to implement a reactive search solution that efficiently handles data retrieval, error handling, and UI responsiveness. In doing so, the project demonstrates strong problem-solving skills, user experience judgment, and code quality practices. Key objectives included:

### Problem Solving Skills
- Implementing a reactive search from data to UI.
- Handling errors gracefully.
- Optimizing performance using efficient data structures and algorithms.
- Structuring the solution in layers: first the domain, then the data, and finally the presentation.

### UX Judgment
- Delivering a responsive and usable interface.
- Managing error states and feedback.
- Persisting the selected city through configuration changes.
- Implementing pagination to handle large data sets smoothly.

### Code Quality
- Ensuring thorough unit testing for use cases, ViewModels, and repositories.
- Conducting integration testing for the database, API service, repository, and UI.
- Following a production-level architecture with clean, readable, and maintainable code.
- Adopting Clean Architecture principles, MVVM in the presentation layer with MVI pattern for UI events
- Utilizing dependency injection (DI) and managing dependencies via Gradle with a clear Git history.

## Assumptions

### Initial Data Loading
- The loading time of the app is not critical. During the first launch, cities are fetched from an API and stored in the local database.
- Although this process can take several seconds, optimizations such as server-side compression or server-level pagination. I tried Retrofit streaming but found to compromise the user experience due to continuous UI updates during loading.

### Data Persistence
- The selected city and the favorites are stored and maintained across configuration changes, ensuring a seamless user experience.

### Pagination Strategy
- Pagination is implemented to manage large data sets. This approach helps avoid overwhelming the device’s memory and ensures a fluid scrolling experience.

## Decisions Taken Through Implementation

### Layered Architecture
- The project is structured in clear layers—Domain, Data, and Presentation.
- This separation is evident both in the codebase and the commit history, which reflects a step-by-step approach.

### Use of Room
- Room was chosen for its quick data responses, efficiency in managing local data persistence, and seamless integration with Flow.

### Pagination Approach
- Instead of custom pagination in the domain and data layers, the Paging 3 library was utilized.
- Paging 3 automates pagination handling, reducing boilerplate code and enhancing reliability.

### Search Implementation
- A Trie-based search solution was initially evaluated for its performance benefits.
- Testing with a large JSON file (approximately 20 MB) showed that building a Trie would consume 50–100 MB of memory, making it less scalable as data grows.
- A more memory-efficient approach was adopted, accepting slightly slower search times in favor of scalability.
- Implemented using Room DB LIKE queries with Paging 3 for real-time filtering
- Debounced (300ms) search queries to minimize DB thrashing
- Paging 3 with pageSize=50, prefetchDistance=20

### Error Handling
- Unified Response<T> wrapper for API/DB operations
- Retry mechanism for pagination failures

### Single ViewModel Strategy
- A single ViewModel is used for both the Home and Map screens because they share the same underlying data, ensuring consistency and simplifying state management.

### Map Implementation
- Although the Google Maps API was initially considered, account issues and potential costs led to the adoption of the free and open-source OpenStreetMap.
- Despite not providing the same level of polish as Google Maps, OpenStreetMap meets the project’s requirements.

### UI Rotation Handling
- The design prioritizes portrait mode with navigation components.
- In landscape mode, a split-screen approach is used where both screens communicate via reactive flows.

## Testing Strategy

### Unit Testing
- Domain logic, use cases, ViewModels, and repository components are thoroughly unit tested to ensure proper isolation and functionality.

### Integration Testing
- Integration tests cover the interactions between the database, API service, repository, and overall data flow.

### UI Testing
- Compose UI tests are implemented for both the Home and Map screens to validate user interactions, responsiveness, and error state handling.