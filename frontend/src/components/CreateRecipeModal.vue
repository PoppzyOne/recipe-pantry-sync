<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { CreateRecipeDto, CreateRecipeIngredientDto, IngredientCategory } from '@/types/recipe'
import { MEASUREMENT_UNITS, CATEGORY_LABELS, INGREDIENT_CATEGORIES } from '@/types/recipe'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', dto: CreateRecipeDto): void
}>()

defineProps<{
  submitting?: boolean
}>()

const form = reactive<{
  title: string
  description: string
  instructions: string
  servings: number
  prepTimeMinutes: number
  cookTimeMinutes: number
  ingredients: CreateRecipeIngredientDto[]
}>({
  title: '',
  description: '',
  instructions: '',
  servings: 4,
  prepTimeMinutes: 15,
  cookTimeMinutes: 20,
  ingredients: [],
})

// Temp state for adding a new ingredient row
const newIng = reactive<CreateRecipeIngredientDto>({
  name: '',
  category: 'PANTRY',
  amount: undefined,
  unit: 'g',
  notes: '',
})

const titleError = ref<string | null>(null)
const ingredientError = ref<string | null>(null)

function handleAddIngredient() {
  if (!newIng.name.trim()) {
    ingredientError.value = 'Ange ingrediensens namn.'
    return
  }
  ingredientError.value = null

  form.ingredients.push({
    name: newIng.name.trim(),
    category: newIng.category || 'PANTRY',
    amount: newIng.amount ? Number(newIng.amount) : null,
    unit: newIng.unit?.trim() || null,
    notes: newIng.notes?.trim() || null,
  })

  // Reset temp input
  newIng.name = ''
  newIng.amount = undefined
  newIng.notes = ''
}

function removeIngredient(index: number) {
  form.ingredients.splice(index, 1)
}

function handleSubmit() {
  if (!form.title.trim()) {
    titleError.value = 'Receptet måste ha en titel.'
    return
  }
  titleError.value = null

  // If user typed something in newIng but didn't press add, auto-add if valid
  if (newIng.name.trim()) {
    handleAddIngredient()
  }

  emit('save', {
    title: form.title.trim(),
    description: form.description?.trim() || null,
    instructions: form.instructions?.trim() || null,
    servings: form.servings ? Number(form.servings) : null,
    prepTimeMinutes: form.prepTimeMinutes ? Number(form.prepTimeMinutes) : null,
    cookTimeMinutes: form.cookTimeMinutes ? Number(form.cookTimeMinutes) : null,
    ingredients: form.ingredients.length > 0 ? form.ingredients : null,
  })
}
</script>

<template>
  <div class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden border border-gray-100 flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-150">
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between shrink-0">
        <div>
          <h2 class="text-xl font-bold text-gray-900">Skapa nytt recept</h2>
          <p class="text-xs text-gray-500">Fyll i receptdetaljer och ingredienser för skafferisynk</p>
        </div>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
          @click="emit('close')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Scrollable Form Body -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-6 overflow-y-auto flex-1">
        <!-- Title & Description -->
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">
              Recepttitel <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.title"
              type="text"
              required
              placeholder="t.ex. Hemlagad Lasagne al Forno"
              class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
            />
            <p v-if="titleError" class="text-xs text-red-500 mt-1">{{ titleError }}</p>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">Kort beskrivning</label>
            <input
              v-model="form.description"
              type="text"
              placeholder="Krämig klassiker med köttfärssås och bechamel..."
              class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
            />
          </div>
        </div>

        <!-- Times & Portions -->
        <div class="grid grid-cols-3 gap-4 bg-gray-50/70 p-3.5 rounded-xl border border-gray-100">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Portioner</label>
            <input
              v-model="form.servings"
              type="number"
              min="1"
              class="w-full px-3 py-1.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 bg-white text-gray-900 text-sm"
            />
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Förb. (min)</label>
            <input
              v-model="form.prepTimeMinutes"
              type="number"
              min="0"
              class="w-full px-3 py-1.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 bg-white text-gray-900 text-sm"
            />
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Tillagn. (min)</label>
            <input
              v-model="form.cookTimeMinutes"
              type="number"
              min="0"
              class="w-full px-3 py-1.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 bg-white text-gray-900 text-sm"
            />
          </div>
        </div>

        <!-- Ingredients Section -->
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div>
              <label class="block text-sm font-bold text-gray-900">
                Ingredienser
                <span class="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full ml-1.5">
                  {{ form.ingredients.length }} st
                </span>
              </label>
              <p class="text-xs text-gray-500">Dessa kopplas automatiskt till ditt skafferi och din inköpslista</p>
            </div>
          </div>

          <!-- Added Ingredients List -->
          <div v-if="form.ingredients.length > 0" class="border border-gray-200 rounded-xl divide-y divide-gray-100 overflow-hidden bg-white shadow-2xs">
            <div
              v-for="(ing, idx) in form.ingredients"
              :key="idx"
              class="px-3.5 py-2.5 flex items-center justify-between gap-3 text-sm hover:bg-gray-50/50"
            >
              <div class="flex items-center gap-2 min-w-0">
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 shrink-0"></span>
                <span class="font-semibold text-gray-900 truncate">{{ ing.name }}</span>
                <span v-if="ing.notes" class="text-xs text-gray-500 truncate">({{ ing.notes }})</span>
                <span
                  class="text-[10px] font-semibold px-2 py-0.5 rounded-full border shrink-0"
                  :class="CATEGORY_LABELS[ing.category || 'OTHER']?.color || 'bg-gray-100 text-gray-600 border-gray-200'"
                >
                  {{ CATEGORY_LABELS[ing.category || 'OTHER']?.label || ing.category }}
                </span>
              </div>

              <div class="flex items-center gap-3 shrink-0">
                <span class="text-xs font-semibold text-gray-700 bg-gray-50 px-2.5 py-1 rounded-md border border-gray-200">
                  {{ ing.amount ? `${ing.amount} ` : '' }}{{ ing.unit || '' }}
                </span>
                <button
                  type="button"
                  class="text-gray-300 hover:text-red-500 p-1 transition-colors"
                  title="Ta bort ingrediens"
                  @click="removeIngredient(idx)"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Add Ingredient Form Row -->
          <div class="bg-gray-50 p-3.5 rounded-xl border border-dashed border-gray-300 space-y-2.5">
            <div class="text-xs font-semibold text-gray-700">Lägg till ingrediens:</div>
            <div class="grid grid-cols-1 sm:grid-cols-12 gap-2">
              <input
                v-model="newIng.name"
                type="text"
                placeholder="Råvara (t.ex. Krossade tomater)"
                class="sm:col-span-4 px-3 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
                @keydown.enter.prevent="handleAddIngredient"
              />

              <input
                v-model="newIng.amount"
                type="number"
                step="any"
                min="0"
                placeholder="Mängd"
                class="sm:col-span-2 px-3 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
                @keydown.enter.prevent="handleAddIngredient"
              />

              <select
                v-model="newIng.unit"
                class="sm:col-span-2 px-2 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              >
                <option v-for="u in MEASUREMENT_UNITS" :key="u.value" :value="u.value">
                  {{ u.label }}
                </option>
              </select>

              <select
                v-model="newIng.category"
                class="sm:col-span-3 px-2 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              >
                <option v-for="cat in INGREDIENT_CATEGORIES" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </option>
              </select>

              <button
                type="button"
                class="sm:col-span-1 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-lg flex items-center justify-center py-1.5 shadow-2xs transition-colors cursor-pointer"
                title="Lägg till ingrediens"
                @click="handleAddIngredient"
              >
                +
              </button>
            </div>

            <!-- Optional notes input -->
            <div class="flex items-center gap-2">
              <input
                v-model="newIng.notes"
                type="text"
                placeholder="Valfri anteckning (t.ex. finhackad, rumstempererad)"
                class="flex-1 px-3 py-1 text-xs rounded-lg border border-gray-200 bg-white focus:outline-hidden focus:ring-1 focus:ring-emerald-500 text-gray-600"
                @keydown.enter.prevent="handleAddIngredient"
              />
              <span v-if="ingredientError" class="text-xs text-red-500 font-medium">{{ ingredientError }}</span>
            </div>
          </div>
        </div>

        <!-- Instructions -->
        <div>
          <label class="block text-sm font-semibold text-gray-700 mb-1">
            Instruktioner / Tillagningssteg
          </label>
          <textarea
            v-model="form.instructions"
            rows="5"
            placeholder="1. Koka pastan al dente...&#10;2. Stek löken mjuk...&#10;3. Blanda samman allt..."
            class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm resize-none"
          ></textarea>
        </div>

        <!-- Modal Footer Actions -->
        <div class="pt-4 border-t border-gray-100 flex items-center justify-end gap-3 sticky bottom-0 bg-white py-2">
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
            <span v-if="submitting">Sparar recept...</span>
            <span v-else>Spara recept</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
