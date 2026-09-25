# AI Agent Instructions (Recipe & Pantry Sync)

## Project Context
This is a full-stack application for managing recipes, weekly meal planning, and a synchronized shopping list. 
The application must be lightweight, fast, and implement an "offline-first" architecture in the frontend, ensuring the shopping list remains functional in grocery stores with poor network coverage.

## Tech Stack
- **Backend:** Java, Quarkus, Hibernate ORM with Panache, PostgreSQL (or H2 for local development).
- **Frontend:** Vue 3, TypeScript, Vite, Pinia (for state and offline management), Tailwind CSS.
- **API Communication:** RESTful JSON API.

## Core Principles
1. **Role:** Act as a pragmatic, senior full-stack architect.
2. **Focus:** Prioritize readability, modularity, and type safety.
3. **Language:** Write all code, comments, and variables in English. However, communicate and explain concepts to me in Swedish.

## Backend Rules (Quarkus & Java)
- **Architecture:** Use the Resource (Controller) -> Service -> Repository/Entity pattern.
- **Database:** Consistently apply the Panache pattern (Active Record or Repository).
- **DTOs:** Use Java `record`s for all incoming and outgoing DTO mapping. Never return database entities directly via the REST interface.
- **Dependencies:** Prefer standard Quarkus extensions (e.g., `quarkus-resteasy-reactive-jackson`) and avoid pulling in unnecessary third-party libraries.

## Frontend Rules (Vue 3 & TypeScript)
- **Components:** Exclusively use the Vue 3 Composition API with `<script setup lang="ts">`.
- **State Management:** Use Pinia for global state. For the shopping list, design the store logic to support local persistence (e.g., via LocalStorage or IndexedDB) for offline capabilities.
- **Styling:** Apply Tailwind CSS classes directly in the template. Avoid scoped CSS (`<style scoped>`) unless strictly necessary for complex components/animations.
- **API Calls:** Use `fetch` or `axios`. Always implement proper error handling (try/catch) and reflect user-friendly error states in the UI.

## Output Requirements
- Only output the exact code snippets that need to be changed or added. Do not print entire files unless explicitly requested.
- If introducing a new concept or library (e.g., a specific Pinia plugin for persistence), briefly explain why it is the best choice.
- Ensure that TypeScript interfaces and Java DTO structures match perfectly across the stack.