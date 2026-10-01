<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Recipe } from '@/types/recipe'
import type { MealPlanItem, MealType } from '@/types/mealPlan'
import { MEAL_TYPES } from '@/types/mealPlan'

const props = defineProps<{
  isOpen: boolean
  initialDate: string
  initialMealType?: MealType
  mealToEdit?: MealPlanItem | null
  recipes: Recipe[]
  submitting?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (
    e: 'save',
    data: {
      planDate: string
      mealType: MealType
      recipeId: number | null
      customTitle: string | null
      servings: number
      notes: string | null
    }
  ): void
  (e: 'delete', id: number): void
}>()

const mode = ref<'recipe' | 'custom'>('recipe')
const selectedDate = ref(props.initialDate)
const selectedMealType = ref<MealType>(props.initialMealType || 'DINNER')
const selectedRecipeId = ref<number | null>(null)
const recipeSearch = ref('')
const customTitle = ref('')
const servings = ref(4)
const notes = ref('')
const titleError = ref<string | null>(null)

// Synchronize state when opening or switching mealToEdit
watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      recipeSearch.value = ''
      titleError.value = null
      if (props.mealToEdit) {
        selectedDate.value = props.mealToEdit.planDate
        selectedMealType.value = props.mealToEdit.mealType
        servings.value = props.mealToEdit.servings
        notes.value = props.mealToEdit.notes || ''
        if (props.mealToEdit.recipeId) {
          mode.value = 'recipe'
          selectedRecipeId.value = props.mealToEdit.recipeId
          customTitle.value = ''
        } else {
          mode.value = 'custom'
          selectedRecipeId.value = null
          customTitle.value = props.mealToEdit.customTitle || ''
        }
      } else {
        selectedDate.value = props.initialDate
        selectedMealType.value = props.initialMealType || 'DINNER'
        servings.value = 4
        notes.value = ''
        customTitle.value = ''
        const firstRecipe = props.recipes[0]
        if (firstRecipe) {
          mode.value = 'recipe'
          selectedRecipeId.value = firstRecipe.id
          if (firstRecipe.servings) {
            servings.value = firstRecipe.servings
          }
        } else {
          mode.value = 'custom'
          selectedRecipeId.value = null
        }
      }
    }
  },
  { immediate: true }
)

const filteredRecipes = computed(() => {
  if (!recipeSearch.value.trim()) return props.recipes
  const q = recipeSearch.value.toLowerCase().trim()
  return props.recipes.filter((r) => r.title.toLowerCase().includes(q))
})

function handleRecipeChange(id: number) {
  selectedRecipeId.value = id
  const rec = props.recipes.find((r) => r.id === id)
  if (rec && rec.servings) {
    servings.value = rec.servings
  }
}

function adjustServings(delta: number) {
  const next = servings.value + delta
  if (next >= 1 && next <= 50) {
    servings.value = next
  }
}

function handleSubmit() {
  titleError.value = null

  if (mode.value === 'recipe') {
    if (!selectedRecipeId.value) {
      titleError.value = 'Vänligen välj ett recept.'
      return
    }
  } else {
    if (!customTitle.value.trim()) {
      titleError.value = 'Ange vad ni ska äta.'
      return
    }
  }

  emit('save', {
    planDate: selectedDate.value,
    mealType: selectedMealType.value,
    recipeId: mode.value === 'recipe' ? selectedRecipeId.value : null,
    customTitle: mode.value === 'custom' ? customTitle.value.trim() : null,
    servings: Number(servings.value) || 4,
    notes: notes.value.trim() || null,
  })
}

function handleDelete() {
  if (props.mealToEdit) {
    emit('delete', props.mealToEdit.id)
  }
}
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4"
    @click.self="emit('close')"
  >
    <div
      class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <h2 class="text-base font-bold text-gray-900 flex items-center gap-2">
          <span>{{ mealToEdit ? '✏️ Redigera måltid' : '➕ Planera måltid' }}</span>
        </h2>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 transition-colors p-1 rounded-lg hover:bg-gray-100 cursor-pointer"
          @click="emit('close')"
        >
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Form Body -->
      <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
        <!-- Date and Meal Type -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Datum</label>
            <input
              type="date"
              v-model="selectedDate"
              required
              class="w-full text-xs rounded-xl border border-gray-200 px-3 py-2 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all font-medium"
            />
          </div>
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">Måltid</label>
            <select
              v-model="selectedMealType"
              class="w-full text-xs rounded-xl border border-gray-200 px-3 py-2 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all font-medium cursor-pointer"
            >
              <option v-for="type in MEAL_TYPES" :key="type.value" :value="type.value">
                {{ type.icon }} {{ type.label }}
              </option>
            </select>
          </div>
        </div>

        <!-- Mode Toggle: Recept vs Egen rätt -->
        <div>
          <label class="block text-xs font-semibold text-gray-700 mb-1.5">Typ av måltid</label>
          <div class="grid grid-cols-2 gap-1.5 p-1 bg-gray-100 rounded-xl text-xs font-medium">
            <button
              type="button"
              class="py-1.5 rounded-lg transition-all cursor-pointer flex items-center justify-center gap-1.5"
              :class="mode === 'recipe' ? 'bg-white shadow-2xs font-bold text-gray-900' : 'text-gray-600 hover:text-gray-900'"
              @click="mode = 'recipe'"
            >
              <span>📖</span> Välj recept
            </button>
            <button
              type="button"
              class="py-1.5 rounded-lg transition-all cursor-pointer flex items-center justify-center gap-1.5"
              :class="mode === 'custom' ? 'bg-white shadow-2xs font-bold text-gray-900' : 'text-gray-600 hover:text-gray-900'"
              @click="mode = 'custom'"
            >
              <span>✍️</span> Egen rätt / Rester
            </button>
          </div>
        </div>

        <!-- Recipe Selector Mode -->
        <div v-if="mode === 'recipe'" class="space-y-2">
          <div class="flex items-center justify-between">
            <label class="block text-xs font-semibold text-gray-700">Recept</label>
            <span v-if="recipes.length" class="text-xs text-gray-400 font-normal">
              {{ recipes.length }} recept tillgängliga
            </span>
          </div>

          <div v-if="recipes.length === 0" class="text-xs text-amber-700 bg-amber-50 p-3 rounded-xl border border-amber-200">
            Inga recept finns sparade än. Byt till "Egen rätt" eller skapa ett recept först!
          </div>

          <div v-else class="space-y-2">
            <!-- Search bar if many recipes -->
            <input
              v-if="recipes.length > 5"
              type="text"
              v-model="recipeSearch"
              placeholder="Sök recept..."
              class="w-full text-xs rounded-xl border border-gray-200 px-3 py-1.5 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
            />
            <select
              :value="selectedRecipeId"
              @change="handleRecipeChange(Number(($event.target as HTMLSelectElement).value))"
              class="w-full text-sm rounded-xl border border-gray-200 px-3 py-2.5 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all font-medium cursor-pointer"
            >
              <option v-for="r in filteredRecipes" :key="r.id" :value="r.id">
                {{ r.title }} ({{ r.servings || 4 }} port)
              </option>
            </select>
          </div>
          <p v-if="titleError" class="text-xs text-red-600 font-medium">{{ titleError }}</p>
        </div>

        <!-- Custom Dish Mode -->
        <div v-else class="space-y-1">
          <label class="block text-xs font-semibold text-gray-700">Vad ska ni äta?</label>
          <input
            type="text"
            v-model="customTitle"
            placeholder="t.ex. Rester från igår, Tacos, Pizza ute"
            class="w-full text-sm rounded-xl border border-gray-200 px-3 py-2 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
            :class="{ 'border-red-300 ring-2 ring-red-500/20': titleError }"
          />
          <p v-if="titleError" class="text-xs text-red-600 font-medium">{{ titleError }}</p>
        </div>

        <!-- Servings selector -->
        <div>
          <label class="block text-xs font-semibold text-gray-700 mb-1">Antal portioner</label>
          <div class="flex items-center gap-3">
            <div class="flex items-center border border-gray-200 rounded-xl bg-gray-50 p-1">
              <button
                type="button"
                @click="adjustServings(-1)"
                class="w-8 h-8 rounded-lg bg-white shadow-2xs hover:bg-gray-100 flex items-center justify-center font-bold text-gray-700 text-base cursor-pointer transition-colors"
              >
                -
              </button>
              <input
                type="number"
                v-model.number="servings"
                min="1"
                max="50"
                class="w-14 text-center font-bold text-sm bg-transparent border-0 focus:outline-hidden"
              />
              <button
                type="button"
                @click="adjustServings(1)"
                class="w-8 h-8 rounded-lg bg-white shadow-2xs hover:bg-gray-100 flex items-center justify-center font-bold text-gray-700 text-base cursor-pointer transition-colors"
              >
                +
              </button>
            </div>
            <span class="text-xs text-gray-500">portioner beräknas</span>
          </div>
        </div>

        <!-- Notes -->
        <div>
          <label class="block text-xs font-semibold text-gray-700 mb-1">Anteckningar (valfritt)</label>
          <input
            type="text"
            v-model="notes"
            placeholder="t.ex. Glöm inte ta fram från frysen"
            class="w-full text-xs rounded-xl border border-gray-200 px-3 py-2 bg-gray-50 focus:bg-white focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all"
          />
        </div>

        <!-- Actions -->
        <div class="pt-3 border-t border-gray-100 flex items-center justify-between gap-3">
          <div>
            <button
              v-if="mealToEdit"
              type="button"
              class="text-xs text-red-600 hover:text-red-700 font-semibold px-2 py-1.5 rounded-lg hover:bg-red-50 transition-colors cursor-pointer"
              @click="handleDelete"
            >
              Ta bort måltid
            </button>
          </div>
          <div class="flex items-center gap-2">
            <button
              type="button"
              class="px-4 py-2 rounded-xl text-xs font-semibold text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-colors cursor-pointer"
              @click="emit('close')"
            >
              Avbryt
            </button>
            <button
              type="submit"
              :disabled="submitting"
              class="px-4 py-2 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 shadow-xs transition-colors cursor-pointer disabled:opacity-50"
            >
              {{ submitting ? 'Sparar...' : mealToEdit ? 'Uppdatera' : 'Spara måltid' }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
