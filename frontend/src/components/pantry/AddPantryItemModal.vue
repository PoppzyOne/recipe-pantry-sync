<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { CreatePantryItemDto, QuantityLevel } from '@/types/pantry'
import { QUANTITY_LEVELS } from '@/types/pantry'
import type { IngredientCategory } from '@/types/recipe'
import { MEASUREMENT_UNITS } from '@/types/recipe'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', dto: CreatePantryItemDto): void
}>()

defineProps<{
  submitting?: boolean
}>()

const form = reactive<CreatePantryItemDto>({
  ingredientName: '',
  category: 'PANTRY',
  quantity: 1,
  unit: 'st',
  quantityLevel: 'FULL',
  inStock: true,
})

const categories: { value: IngredientCategory; label: string }[] = [
  { value: 'PANTRY', label: 'Skafferi & Torrvaror' },
  { value: 'PRODUCE', label: 'Frukt & Grönt' },
  { value: 'DAIRY', label: 'Mejeri & Ost' },
  { value: 'MEAT', label: 'Kött, Fågel & Fisk' },
  { value: 'SPICES', label: 'Kryddor & Smaksättare' },
  { value: 'BAKERY', label: 'Bröd & Bakning' },
  { value: 'FROZEN', label: 'Frysvaror' },
  { value: 'OTHER', label: 'Övrigt' },
]

const nameError = ref<string | null>(null)

function selectQuantityLevel(level: QuantityLevel) {
  form.quantityLevel = level
  if (level === 'EMPTY') {
    form.inStock = false
  } else {
    form.inStock = true
  }
}

function handleSubmit() {
  if (!form.ingredientName.trim()) {
    nameError.value = 'Ange varans namn.'
    return
  }
  nameError.value = null

  emit('save', {
    ingredientName: form.ingredientName.trim(),
    category: form.category,
    quantity: form.quantity ? Number(form.quantity) : null,
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
        <h2 class="text-xl font-bold text-gray-900">Lägg till i skafferiet</h2>
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
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1">
            Vara / Ingrediens <span class="text-red-500">*</span>
          </label>
          <input
            v-model="form.ingredientName"
            type="text"
            required
            placeholder="t.ex. Havregryn, Ägg, Mjölk"
            class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
          />
          <p v-if="nameError" class="text-xs text-red-500 mt-1">{{ nameError }}</p>
        </div>

        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1">Kategori</label>
          <select
            v-model="form.category"
            class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm bg-white"
          >
            <option v-for="cat in categories" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </option>
          </select>
        </div>

        <!-- Mängdnivå (Quick enum buttons) -->
        <div>
          <label class="block text-xs font-semibold text-gray-700 mb-1.5">Lagerstatus / Mängdnivå</label>
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
              @click="selectQuantityLevel(lvl.value)"
            >
              {{ lvl.label }}
            </button>
          </div>
        </div>

        <!-- Numeric quantity and Unit enum select -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Exakt mängd (valfritt)</label>
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

        <div class="pt-2">
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              v-model="form.inStock"
              type="checkbox"
              class="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 border-gray-300"
            />
            <span class="text-sm font-medium text-gray-800">Finns hemma just nu</span>
          </label>
        </div>

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
            <span v-else>Spara i skafferiet</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
