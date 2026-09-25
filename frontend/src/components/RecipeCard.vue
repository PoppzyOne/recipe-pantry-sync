<script setup lang="ts">
import type { Recipe } from '@/types/recipe'

defineProps<{
  recipe: Recipe
}>()

const emit = defineEmits<{
  (e: 'select', recipe: Recipe): void
  (e: 'edit', recipe: Recipe): void
  (e: 'delete', id: number): void
}>()
</script>

<template>
  <div
    class="bg-white rounded-xl border border-gray-200 shadow-xs hover:shadow-md transition-shadow p-5 flex flex-col justify-between cursor-pointer group"
    @click="emit('select', recipe)"
  >
    <div>
      <div class="flex items-start justify-between gap-3 mb-2">
        <h3 class="text-lg font-semibold text-gray-900 group-hover:text-emerald-600 transition-colors">
          {{ recipe.title }}
        </h3>
        <div class="flex items-center gap-1">
          <button
            type="button"
            class="text-gray-400 hover:text-emerald-600 transition-colors p-1 rounded-md hover:bg-emerald-50"
            title="Redigera recept"
            @click.stop="emit('edit', recipe)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            type="button"
            class="text-gray-400 hover:text-red-500 transition-colors p-1 rounded-md hover:bg-red-50"
            title="Radera recept"
            @click.stop="emit('delete', recipe.id)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      <p class="text-sm text-gray-600 line-clamp-2 mb-4">
        {{ recipe.description || 'Ingen beskrivning angiven.' }}
      </p>
    </div>

    <div class="pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500 font-medium">
      <div class="flex items-center gap-3">
        <span v-if="recipe.servings" class="flex items-center gap-1">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          {{ recipe.servings }} port
        </span>

        <span v-if="recipe.prepTimeMinutes || recipe.cookTimeMinutes" class="flex items-center gap-1">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ (recipe.prepTimeMinutes || 0) + (recipe.cookTimeMinutes || 0) }} min
        </span>
      </div>

      <span class="text-emerald-600 font-semibold text-xs flex items-center gap-0.5 group-hover:translate-x-0.5 transition-transform">
        Visa recept &rarr;
      </span>
    </div>
  </div>
</template>
