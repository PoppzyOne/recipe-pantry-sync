<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { Recipe, UpdateRecipeDto, CreateRecipeIngredientDto, IngredientCategory } from '@/types/recipe'
import { MEASUREMENT_UNITS, CATEGORY_LABELS, INGREDIENT_CATEGORIES } from '@/types/recipe'

const props = defineProps<{
  recipe: Recipe
  submitting?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', dto: UpdateRecipeDto): void
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
  title: props.recipe.title,
  description: props.recipe.description || '',
  instructions: props.recipe.instructions || '',
  servings: props.recipe.servings || 4,
  prepTimeMinutes: props.recipe.prepTimeMinutes || 15,
  cookTimeMinutes: props.recipe.cookTimeMinutes || 20,
  ingredients: (props.recipe.ingredients || []).map((i) => ({
    name: i.name,
    category: i.category,
    amount: i.amount,
    unit: i.unit,
    notes: i.notes,
  })),
})

watch(
  () => props.recipe,
  (newRecipe) => {
    form.title = newRecipe.title
    form.description = newRecipe.description || ''
    form.instructions = newRecipe.instructions || ''
    form.servings = newRecipe.servings || 4
    form.prepTimeMinutes = newRecipe.prepTimeMinutes || 15
    form.cookTimeMinutes = newRecipe.cookTimeMinutes || 20
    form.ingredients = (newRecipe.ingredients || []).map((i) => ({
      name: i.name,
      category: i.category,
      amount: i.amount,
      unit: i.unit,
      notes: i.notes,
    }))
  },
  { deep: true }
)

// Temp state for adding new ingredient
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
    ingredientError.value = 'Ange namn på ingrediensen'
    return
  }

  ingredientError.value = null
  form.ingredients.push({
    name: newIng.name.trim(),
    category: newIng.category,
    amount: newIng.amount ? Number(newIng.amount) : null,
    unit: newIng.unit || null,
    notes: newIng.notes?.trim() || null,
  })

  // Reset input fields
  newIng.name = ''
  newIng.amount = undefined
  newIng.notes = ''
}

function removeIngredient(index: number) {
  form.ingredients.splice(index, 1)
}

function handleSubmit() {
  if (!form.title.trim()) {
    titleError.value = 'Receptets titel är obligatorisk.'
    return
  }
  titleError.value = null

  emit('save', {
    title: form.title.trim(),
    description: form.description.trim() || null,
    instructions: form.instructions.trim() || null,
    servings: form.servings ? Number(form.servings) : null,
    prepTimeMinutes: form.prepTimeMinutes ? Number(form.prepTimeMinutes) : null,
    cookTimeMinutes: form.cookTimeMinutes ? Number(form.cookTimeMinutes) : null,
    ingredients: form.ingredients,
  })
}
</script>

<template>
  <div class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
    <div
      class="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between shrink-0">
        <div>
          <h2 class="text-xl font-bold text-gray-900">Redigera recept</h2>
          <p class="text-xs text-gray-500 mt-0.5">Uppdatera information, ingredienser och tillagning</p>
        </div>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors"
          @click="emit('close')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Scrollable Form Body -->
      <form @submit.prevent="handleSubmit" class="flex-1 overflow-y-auto p-6 space-y-6">
        <!-- Basic info -->
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">
              Titel <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.title"
              type="text"
              required
              class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
            />
            <p v-if="titleError" class="text-xs text-red-500 mt-1">{{ titleError }}</p>
          </div>

          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-1">Beskrivning</label>
            <textarea
              v-model="form.description"
              rows="2"
              class="w-full px-3.5 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
            ></textarea>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Portioner</label>
              <input
                v-model="form.servings"
                type="number"
                min="1"
                class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Förberedelsetid (min)</label>
              <input
                v-model="form.prepTimeMinutes"
                type="number"
                min="0"
                class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-gray-700 mb-1">Tillagningstid (min)</label>
              <input
                v-model="form.cookTimeMinutes"
                type="number"
                min="0"
                class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm"
              />
            </div>
          </div>
        </div>

        <!-- Ingredients Section -->
        <div class="pt-4 border-t border-gray-100">
          <div class="flex items-center justify-between mb-2">
            <div>
              <label class="block text-sm font-bold text-gray-900">
                Ingredienser
                <span class="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full ml-1.5">
                  {{ form.ingredients.length }} st
                </span>
              </label>
              <p class="text-xs text-gray-500">Kopplas automatiskt till skafferi och inköpslista</p>
            </div>
          </div>

          <!-- Existing / Current Ingredients List -->
          <div
            v-if="form.ingredients.length > 0"
            class="border border-gray-200 rounded-xl divide-y divide-gray-100 overflow-hidden bg-white shadow-2xs mb-4"
          >
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
                class="sm:col-span-2 px-2 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
              >
                <option v-for="cat in INGREDIENT_CATEGORIES" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </option>
              </select>

              <button
                type="button"
                class="sm:col-span-2 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition-colors flex items-center justify-center gap-1 shadow-2xs"
                @click="handleAddIngredient"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Lägg till
              </button>
            </div>

            <div class="flex items-center gap-2">
              <input
                v-model="newIng.notes"
                type="text"
                placeholder="Valfri notering (t.ex. finhackad, rumsvarm)"
                class="w-full px-3 py-1.5 text-xs rounded-lg border border-gray-300 bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
                @keydown.enter.prevent="handleAddIngredient"
              />
            </div>
            <p v-if="ingredientError" class="text-xs text-red-500">{{ ingredientError }}</p>
          </div>
        </div>

        <!-- Instructions -->
        <div class="pt-4 border-t border-gray-100">
          <label class="block text-sm font-semibold text-gray-700 mb-1">
            Instruktioner / Tillagning
          </label>
          <textarea
            v-model="form.instructions"
            rows="5"
            placeholder="Steg-för-steg beskrivning..."
            class="w-full px-3.5 py-2.5 rounded-lg border border-gray-300 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-gray-900 text-sm whitespace-pre-line"
          ></textarea>
        </div>
      </form>

      <!-- Footer Buttons -->
      <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-end gap-3 bg-gray-50/50 shrink-0">
        <button
          type="button"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
          @click="emit('close')"
        >
          Avbryt
        </button>
        <button
          type="button"
          :disabled="submitting"
          class="px-5 py-2 text-sm font-medium text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 rounded-lg shadow-xs transition-colors flex items-center gap-2 cursor-pointer"
          @click="handleSubmit"
        >
          <span v-if="submitting">Sparar...</span>
          <span v-else>Spara ändringar</span>
        </button>
      </div>
    </div>
  </div>
</template>
