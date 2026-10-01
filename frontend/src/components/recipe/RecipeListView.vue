<script setup lang="ts">
import { useRecipeStore, type RecipeSortOption } from '@/stores/recipeStore'
import type { Recipe } from '@/types/recipe'
import RecipeCard from '../RecipeCard.vue'

const recipeStore = useRecipeStore()

const emit = defineEmits<{
  (e: 'select', recipe: Recipe): void
  (e: 'edit', recipe: Recipe): void
  (e: 'delete', id: number): void
  (e: 'create'): void
  (e: 'import'): void
}>()

const timeFilterOptions: { label: string; value: number | 'ALL' }[] = [
  { label: 'Alla tider', value: 'ALL' },
  { label: '⚡ Snabbast (≤ 30 min)', value: 30 },
  { label: '⏱️ ≤ 45 min', value: 45 },
  { label: '🍲 ≤ 60 min', value: 60 },
]

const sortOptions: { label: string; value: RecipeSortOption }[] = [
  { label: 'Nyast först', value: 'NEWEST' },
  { label: 'Titel (A–Ö)', value: 'TITLE' },
  { label: 'Kortast tillagningstid', value: 'TIME' },
]
</script>

<template>
  <div class="space-y-6">
    <!-- Header Actions & Search Bar -->
    <div class="bg-white p-4 sm:p-5 rounded-2xl border border-gray-200 shadow-2xs space-y-4">
      <div class="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-4">
        <!-- Search input -->
        <div class="relative flex-1">
          <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-gray-400">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input
            v-model="recipeStore.searchQuery"
            type="text"
            placeholder="Sök recept eller råvara (t.ex. Kyckling, Pasta, Köttbullar)..."
            class="w-full pl-10 pr-9 py-2 text-sm rounded-xl border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all bg-gray-50/50 focus:bg-white"
          />
          <button
            v-if="recipeStore.searchQuery"
            type="button"
            @click="recipeStore.searchQuery = ''"
            class="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 cursor-pointer"
            title="Rensa sökning"
          >
            ✕
          </button>
        </div>

        <!-- Sort selector -->
        <div class="flex items-center gap-2 shrink-0">
          <label class="text-xs font-semibold text-gray-500 whitespace-nowrap">Sortera:</label>
          <select
            v-model="recipeStore.sortBy"
            class="px-3 py-2 text-xs font-semibold rounded-xl border border-gray-200 bg-white text-gray-700 focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 cursor-pointer"
          >
            <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- Time filter chips -->
      <div class="flex flex-wrap items-center gap-2 pt-2 border-t border-gray-100">
        <span class="text-xs font-medium text-gray-400 mr-1">Tillagningstid:</span>
        <button
          v-for="timeOpt in timeFilterOptions"
          :key="String(timeOpt.value)"
          type="button"
          @click="recipeStore.maxTimeFilter = timeOpt.value"
          class="px-3 py-1 text-xs rounded-full font-medium transition-all cursor-pointer border"
          :class="
            recipeStore.maxTimeFilter === timeOpt.value
              ? 'bg-emerald-600 text-white border-emerald-600 shadow-2xs'
              : 'bg-gray-50 text-gray-600 border-gray-200 hover:bg-gray-100'
          "
        >
          {{ timeOpt.label }}
        </button>

        <button
          v-if="recipeStore.hasActiveFilters"
          type="button"
          @click="recipeStore.resetFilters"
          class="ml-auto text-xs font-semibold text-emerald-600 hover:text-emerald-700 underline underline-offset-2 cursor-pointer"
        >
          Återställ filter
        </button>
      </div>
    </div>

    <!-- Active Count & Results Header -->
    <div class="flex items-center justify-between px-1">
      <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
        <span>Sparade recept</span>
        <span
          v-if="recipeStore.hasActiveFilters"
          class="text-xs font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200"
        >
          Visar {{ recipeStore.filteredRecipes.length }} av {{ recipeStore.totalRecipesCount }}
        </span>
        <span
          v-else
          class="text-xs font-semibold px-2 py-0.5 rounded-full bg-gray-100 text-gray-600"
        >
          {{ recipeStore.totalRecipesCount }}
        </span>
      </h2>
    </div>

    <!-- Loading State -->
    <div
      v-if="recipeStore.loading && recipeStore.recipes.length === 0"
      class="text-center py-16 text-gray-500"
    >
      <div class="inline-block animate-spin text-3xl mb-3">🍳</div>
      <p class="text-sm font-medium">Hämtar dina recept...</p>
    </div>

    <!-- Empty Bank State (No recipes in entire database) -->
    <div
      v-else-if="recipeStore.recipes.length === 0 && !recipeStore.loading"
      class="bg-white rounded-2xl border border-dashed border-gray-300 p-12 text-center max-w-md mx-auto my-8"
    >
      <div class="w-14 h-14 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center mx-auto mb-4 text-2xl">
        📖
      </div>
      <h3 class="text-lg font-bold text-gray-900 mb-1">Inga recept ännu</h3>
      <p class="text-sm text-gray-500 mb-6">
        Börja med att skapa ditt första recept för att bygga upp din receptbank.
      </p>
      <div class="flex items-center justify-center gap-3">
        <button
          type="button"
          class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold px-4 py-2.5 rounded-xl shadow-xs transition-colors cursor-pointer"
          @click="emit('create')"
        >
          <span>+</span> Skapa recept
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 bg-white hover:bg-gray-50 text-gray-700 text-sm font-semibold px-4 py-2.5 rounded-xl border border-gray-200 transition-colors cursor-pointer"
          @click="emit('import')"
        >
          <span>📥</span> Importera recept
        </button>
      </div>
    </div>

    <!-- Empty Search Results State (Recipes exist but none match filters) -->
    <div
      v-else-if="recipeStore.filteredRecipes.length === 0"
      class="bg-white rounded-2xl border border-dashed border-gray-200 p-10 text-center max-w-md mx-auto my-8 space-y-3"
    >
      <div class="text-3xl">🔍</div>
      <h3 class="text-base font-bold text-gray-900">Inga recept matchar din sökning</h3>
      <p class="text-xs text-gray-500">
        Prova att söka på en annan råvara, eller justera dina tidsfilter.
      </p>
      <button
        type="button"
        @click="recipeStore.resetFilters"
        class="px-4 py-2 rounded-xl text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 transition-colors cursor-pointer"
      >
        Återställ alla filter
      </button>
    </div>

    <!-- Recipe Card Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <RecipeCard
        v-for="recipe in recipeStore.filteredRecipes"
        :key="recipe.id"
        :recipe="recipe"
        @select="emit('select', recipe)"
        @edit="emit('edit', recipe)"
        @delete="emit('delete', recipe.id)"
      />
    </div>
  </div>
</template>
