<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { PantryItem, UpdatePantryItemDto, QuantityLevel } from '@/types/pantry'
import { QUANTITY_LEVELS } from '@/types/pantry'
import type { IngredientCategory } from '@/types/recipe'
import { MEASUREMENT_UNITS, INGREDIENT_CATEGORIES } from '@/types/recipe'

const props = defineProps<{
  item: PantryItem
  submitting?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', dto: UpdatePantryItemDto): void
}>()

const form = reactive<{
  category: IngredientCategory
  quantity: number | null | undefined
  unit: string | null
  quantityLevel: QuantityLevel | null
  inStock: boolean
}>({
  category: props.item.category,
  quantity: props.item.quantity,
  unit: props.item.unit || 'st',
  quantityLevel: props.item.quantityLevel || null,
  inStock: props.item.inStock,
})

watch(
  () => props.item,
  (newItem) => {
    form.category = newItem.category
    form.quantity = newItem.quantity
    form.unit = newItem.unit || 'st'
    form.quantityLevel = newItem.quantityLevel || null
    form.inStock = newItem.inStock
  },
  { deep: true }
)

function selectQuantityLevel(level: QuantityLevel) {
  // If clicking the active level, toggle it off (optional level)
  if (form.quantityLevel === level) {
    form.quantityLevel = null
  } else {
    form.quantityLevel = level
    if (level === 'EMPTY') {
      form.inStock = false
    } else {
      form.inStock = true
    }
  }
}

function clearQuantityLevel() {
  form.quantityLevel = null
}

function handleSubmit() {
  emit('save', {
    category: form.category,
    quantity: form.quantity !== null && form.quantity !== undefined && form.quantity !== ('' as unknown)
      ? Number(form.quantity)
      : null,
    unit: form.unit?.trim() || null,
    quantityLevel: form.quantityLevel || null,
    inStock: form.inStock,
  })
}
</script>

<template>
  <div class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <div>
          <h2 class="text-xl font-bold text-gray-900">Ändra i skafferiet</h2>
          <p class="text-xs text-gray-500 font-medium mt-0.5">{{ item.ingredientName }}</p>
        </div>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
          @click="emit('close')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <!-- Vara / Namn info -->
        <div class="bg-gray-50 p-3.5 rounded-xl border border-gray-200/60 flex items-center justify-between">
          <div>
            <span class="text-[11px] font-semibold text-gray-400 uppercase tracking-wider block">Vara</span>
            <span class="text-base font-bold text-gray-900">{{ item.ingredientName }}</span>
          </div>
          <span class="text-xs font-semibold px-2.5 py-1 rounded-full bg-white border border-gray-200 text-gray-700">
            {{ INGREDIENT_CATEGORIES.find(c => c.value === form.category)?.label || form.category }}
          </span>
        </div>

        <!-- Kategori (redigerbar i svensk text) -->
        <div>
          <label class="block text-xs font-semibold text-gray-700 mb-1">Kategori</label>
          <select
            v-model="form.category"
            class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm bg-white"
          >
            <option v-for="cat in INGREDIENT_CATEGORIES" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </option>
          </select>
        </div>

        <!-- Mängdnivå (Valfri enum) -->
        <div>
          <div class="flex items-center justify-between mb-1.5">
            <label class="block text-xs font-semibold text-gray-700">
              Lagerstatus / Mängdnivå
              <span class="text-xs font-normal text-gray-500">(valfritt)</span>
            </label>
            <button
              v-if="form.quantityLevel"
              type="button"
              class="text-[11px] text-gray-400 hover:text-gray-600 underline cursor-pointer"
              @click="clearQuantityLevel"
            >
              Rensa nivå
            </button>
          </div>
          <div class="grid grid-cols-4 gap-1.5">
            <button
              v-for="lvl in QUANTITY_LEVELS"
              :key="lvl.value"
              type="button"
              class="py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer text-center"
              :class="
                form.quantityLevel === lvl.value
                  ? 'border-emerald-600 bg-emerald-50 text-emerald-800 ring-2 ring-emerald-500/20 shadow-2xs font-bold'
                  : 'border-gray-200 bg-white text-gray-600 hover:bg-gray-50'
              "
              :title="form.quantityLevel === lvl.value ? 'Klicka för att avmarkera' : lvl.label"
              @click="selectQuantityLevel(lvl.value)"
            >
              {{ lvl.label }}
            </button>
          </div>
          <p class="text-[11px] text-gray-400 mt-1">Klicka på en vald nivå igen för att avmarkera.</p>
        </div>

        <!-- Numeric quantity and Unit enum select -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Mängd</label>
            <input
              v-model="form.quantity"
              type="number"
              step="any"
              min="0"
              placeholder="t.ex. 500"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
            />
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Måttenhet</label>
            <select
              v-model="form.unit"
              class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm bg-white"
            >
              <option v-for="u in MEASUREMENT_UNITS" :key="u.value" :value="u.value">
                {{ u.label }}
              </option>
            </select>
          </div>
        </div>

        <!-- In stock checkbox -->
        <div class="pt-1">
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              v-model="form.inStock"
              type="checkbox"
              class="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 border-gray-300"
            />
            <span class="text-sm font-medium text-gray-800">Finns hemma</span>
          </label>
        </div>

        <!-- Buttons -->
        <div class="pt-4 border-t border-gray-100 flex items-center justify-end gap-3">
          <button
            type="button"
            class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer"
            @click="emit('close')"
          >
            Avbryt
          </button>
          <button
            type="submit"
            :disabled="submitting"
            class="px-5 py-2 text-sm font-medium text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 rounded-lg shadow-xs transition-colors flex items-center gap-2 cursor-pointer"
          >
            <span v-if="submitting">Sparar...</span>
            <span v-else>Spara ändringar</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
