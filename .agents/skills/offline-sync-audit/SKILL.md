---
name: offline-sync-audit
description: >-
  Use this skill when modifying or debugging the offline-first shopping list,
  localStorage synchronization, optimistic UI updates, or Swedish measurement
  unit conversions between recipes, meal plans, pantry stock, and the shopping list.
---

# Offline-First Sync & Unit Conversion Guide

Use these domain rules and verification steps when working on the shopping list store, pantry synchronization, or ingredient unit math.

## 1. Offline-First State Rules (`frontend/src/stores/shoppingListStore.ts`)
1. **Zero-Latency Optimistic Updates:**
   - User actions (`addItem`, `toggleItem`, `removeItem`, `clearCompleted`) must mutate the Pinia reactive state and persist to `localStorage` **synchronously** before awaiting any network call.
2. **Offline Fallback & `pendingSync`:**
   - When `navigator.onLine` is `false` or a network request fails, keep the optimistic item in state with `pendingSync: true` (using negative temporary IDs for newly created offline items).
3. **Auto-Reconnect Reconciliation:**
   - The `online` event listener (`window.addEventListener('online', ...)`) must flush all `pendingSync` items via the batch endpoint (`/api/shopping-list/batch`) and reconcile server-assigned IDs with local state without duplicating items.

## 2. Swedish Measurement Unit Conversion (`backend/src/main/java/com/recipesync/service/`)
When aggregating ingredients or deducting pantry stock (`PantryService`, `MealPlanService`, `ShoppingListService`):
1. **Volume Family (base unit: `ml`):**
   - `1 krm` = `1 ml`
   - `1 tsk` = `5 ml`
   - `1 msk` = `15 ml`
   - `1 cl` = `10 ml`
   - `1 dl` = `100 ml`
   - `1 l` = `1000 ml`
2. **Weight Family (base unit: `g`):**
   - `1 g` = `1 g`
   - `1 hg` = `100 g`
   - `1 kg` = `1000 g`
3. **Discrete / Qualitative Handling:**
   - Count units (`st`, `port`, `förp`, `klyfta`, `burk`) only merge or deduct when units match.
   - Qualitative `QuantityLevel` (`FULL`, `HALF`, `LOW`, `EMPTY`):
     - `EMPTY` implies `inStock = false` (do not deduct from required shopping quantities).
     - When `inStock = true` without an incompatible numeric unit, respect qualitative stock presence according to service rules.

