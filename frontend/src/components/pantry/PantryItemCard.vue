<script setup lang="ts">
import { computed } from 'vue'
import type { PantryItem } from '@/types/pantry'
import { QUANTITY_LEVELS } from '@/types/pantry'
import type { IngredientCategory } from '@/types/recipe'
import { CATEGORY_LABELS } from '@/types/recipe'

const props = defineProps<{
  item: PantryItem
}>()

const emit = defineEmits<{
  (e: 'toggle', id: number): void
  (e: 'edit', item: PantryItem): void
  (e: 'delete', id: number): void
  (e: 'addToShoppingList', item: PantryItem): void
}>()

const levelInfo = computed(() => {
  if (!props.item.quantityLevel) return null
  return QUANTITY_LEVELS.find((l) => l.value === props.item.quantityLevel) || null
})
</script>

<template>
  <div
    class="bg-white rounded-xl border transition-all p-4 flex flex-col justify-between"
    :class="item.inStock ? 'border-gray-200 shadow-2xs hover:shadow-xs' : 'border-amber-200 bg-amber-50/20 opacity-85'"
  >
    <div>
      <div class="flex items-start justify-between gap-2 mb-2">
        <div>
          <span
            class="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-full border mb-1.5"
            :class="CATEGORY_LABELS[item.category]?.color || 'bg-gray-50 text-gray-700 border-gray-200'"
          >
            {{ CATEGORY_LABELS[item.category]?.label || item.category }}
          </span>
          <h4 class="text-base font-bold text-gray-900 leading-snug">
            {{ item.ingredientName }}
          </h4>
        </div>

        <div class="flex items-center gap-1">
          <button
            type="button"
            class="text-gray-400 hover:text-emerald-600 p-1 rounded-md hover:bg-gray-100 transition-colors cursor-pointer"
            title="Ändra vara"
            @click="emit('edit', item)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </button>
          <button
            type="button"
            class="text-gray-400 hover:text-red-500 p-1 rounded-md hover:bg-gray-100 transition-colors cursor-pointer"
            title="Ta bort från skafferi"
            @click="emit('delete', item.id)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>
      </div>

      <div class="flex items-center flex-wrap gap-2 mb-3">
        <div class="text-xs text-gray-500 font-medium">
          Mängd:
          <span class="text-gray-800 font-semibold">
            {{ item.quantity ? `${item.quantity} ${item.unit || ''}` : 'Ej angiven' }}
          </span>
        </div>
        <span
          v-if="levelInfo"
          class="text-[10px] font-bold px-2 py-0.5 rounded-full border tracking-wide uppercase"
          :class="levelInfo.badgeColor"
        >
          {{ levelInfo.label }}
        </span>
      </div>
    </div>

    <div class="pt-3 border-t border-gray-100 flex items-center justify-between gap-2">
      <!-- Status toggle button -->
      <button
        type="button"
        class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors cursor-pointer"
        :class="
          item.inStock
            ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200'
            : 'bg-amber-100 text-amber-800 hover:bg-amber-200'
        "
        @click="emit('toggle', item.id)"
      >
        <span class="w-2 h-2 rounded-full" :class="item.inStock ? 'bg-emerald-600' : 'bg-amber-500'"></span>
        {{ item.inStock ? 'Finns hemma' : 'Slut' }}
      </button>

      <!-- Quick add to shopping list button -->
      <button
        v-if="!item.inStock"
        type="button"
        class="inline-flex items-center gap-1 text-xs text-emerald-700 hover:text-emerald-800 font-semibold hover:underline cursor-pointer"
        @click="emit('addToShoppingList', item)"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
        </svg>
        Till inköpslistan
      </button>
    </div>
  </div>
</template>
