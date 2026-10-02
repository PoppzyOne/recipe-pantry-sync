<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import type { Recipe } from '@/types/recipe'
import { usePantryStore } from '@/stores/pantryStore'
import { useShoppingListStore } from '@/stores/shoppingListStore'

const props = defineProps<{
  recipe: Recipe
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'edit', recipe: Recipe): void
  (e: 'delete', id: number): void
}>()

const pantryStore = usePantryStore()
const shoppingStore = useShoppingListStore()

const syncMessage = ref<string | null>(null)
const isAddingToShopping = ref(false)

function formatLineBreaks(text: string | null | undefined): string {
  if (!text) return ''
  return text.replace(/\\n/g, '\n')
}

const totalTimeMinutes = computed(() => {
  const prep = props.recipe.prepTimeMinutes || 0
  const cook = props.recipe.cookTimeMinutes || 0
  return prep + cook > 0 ? prep + cook : null
})

const instructionSteps = computed(() => {
  if (!props.recipe.instructions) return []
  const text = formatLineBreaks(props.recipe.instructions)
  return text
    .split(/\r?\n+/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
})

function getCleanStepText(step: string): string {
  return step.replace(/^(\d+[\.\)]|\b(steg|step)\s*\d+[:\.]?|[-*•])\s*/i, '').trim()
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    emit('close')
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

async function handleAddMissingToShoppingList() {
  isAddingToShopping.value = true
  try {
    const addedCount = await shoppingStore.addMissingIngredientsFromRecipe(
      props.recipe.id,
      props.recipe.title
    )
    if (addedCount > 0) {
      syncMessage.value = `${addedCount} saknade råvaror lades till i inköpslistan!`
    } else {
      syncMessage.value = 'Alla råvaror finns redan hemma i skafferiet!'
    }
  } catch {
    syncMessage.value = 'Kunde inte synka ingredienser just nu.'
  } finally {
    isAddingToShopping.value = false
    setTimeout(() => {
      syncMessage.value = null
    }, 3000)
  }
}
</script>

<template>
  <div
    class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-3 sm:p-4 md:p-6"
    @click.self="emit('close')"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl w-full max-w-lg md:max-w-3xl lg:max-w-5xl xl:max-w-6xl max-h-[92vh] flex flex-col overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Header -->
      <div class="px-5 sm:px-6 py-4 sm:py-5 border-b border-gray-100 flex items-start justify-between gap-4 shrink-0 bg-white">
        <div class="min-w-0">
          <div class="flex items-center gap-2 mb-1.5 flex-wrap">
            <span class="inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
              Recept #{{ recipe.id }}
            </span>
            <span
              v-if="recipe.ingredients?.length"
              class="inline-block px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600"
            >
              {{ recipe.ingredients.length }} ingredienser
            </span>
          </div>
          <h2 class="text-xl sm:text-2xl md:text-3xl font-bold text-gray-900 leading-tight truncate">
            {{ recipe.title }}
          </h2>
        </div>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 p-2 rounded-xl hover:bg-gray-100 transition-colors cursor-pointer shrink-0"
          title="Stäng (Esc)"
          @click="emit('close')"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Scrollable Content Body -->
      <div class="p-5 sm:p-6 md:p-8 space-y-6 md:space-y-8 overflow-y-auto flex-1">
        <!-- Description -->
        <p
          v-if="recipe.description"
          class="whitespace-pre-line text-gray-600 text-sm sm:text-base leading-relaxed bg-gray-50/50 p-4 rounded-xl border border-gray-100"
        >
          {{ formatLineBreaks(recipe.description) }}
        </p>

        <!-- Stats Grid -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 bg-gray-50/80 p-3.5 sm:p-4 rounded-xl text-center border border-gray-100">
          <div>
            <div class="text-xs text-gray-500 font-medium">Portioner</div>
            <div class="text-base font-bold text-gray-900 mt-0.5">
              {{ recipe.servings ? `${recipe.servings} st` : '-' }}
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 font-medium">Förberedelse</div>
            <div class="text-base font-bold text-gray-900 mt-0.5">
              {{ recipe.prepTimeMinutes ? `${recipe.prepTimeMinutes} min` : '-' }}
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 font-medium">Tillagning</div>
            <div class="text-base font-bold text-gray-900 mt-0.5">
              {{ recipe.cookTimeMinutes ? `${recipe.cookTimeMinutes} min` : '-' }}
            </div>
          </div>
          <div>
            <div class="text-xs text-gray-500 font-medium">Total tid</div>
            <div class="text-base font-bold text-emerald-700 mt-0.5">
              {{ totalTimeMinutes ? `${totalTimeMinutes} min` : '-' }}
            </div>
          </div>
        </div>

        <!-- Responsive Split Layout: Ingredients (Left) and Instructions (Right) -->
        <div class="grid grid-cols-1 md:grid-cols-12 gap-6 lg:gap-8 items-start">
          <!-- Left Column: Ingredients -->
          <div class="md:col-span-5 space-y-3">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <h3 class="text-sm font-bold uppercase tracking-wider text-gray-900 flex items-center gap-1.5">
                <span>🥕</span> Ingredienser
              </h3>
              <button
                v-if="recipe.ingredients && recipe.ingredients.length > 0"
                type="button"
                :disabled="isAddingToShopping"
                class="text-xs font-semibold text-emerald-700 hover:text-emerald-800 bg-emerald-50 hover:bg-emerald-100 px-2.5 py-1.5 rounded-lg transition-colors flex items-center gap-1.5 cursor-pointer disabled:opacity-50 self-start sm:self-auto border border-emerald-200/60"
                @click="handleAddMissingToShoppingList"
              >
                <span>🛒</span>
                <span>Synka saknade</span>
              </button>
            </div>

            <!-- Sync feedback message -->
            <div
              v-if="syncMessage"
              class="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-xs font-medium text-emerald-800 animate-in fade-in"
            >
              {{ syncMessage }}
            </div>

            <!-- Ingredients list -->
            <div
              v-if="recipe.ingredients && recipe.ingredients.length > 0"
              class="divide-y divide-gray-100 border border-gray-200/80 rounded-xl overflow-hidden bg-white shadow-2xs"
            >
              <div
                v-for="ing in recipe.ingredients"
                :key="ing.id"
                class="px-3.5 py-2.5 flex items-center justify-between gap-2.5 text-sm hover:bg-gray-50 transition-colors"
              >
                <div class="flex items-center gap-2 min-w-0">
                  <span
                    class="w-2.5 h-2.5 rounded-full shrink-0"
                    :class="pantryStore.isIngredientInStock(ing.ingredientId) ? 'bg-emerald-500' : 'bg-amber-400'"
                    :title="pantryStore.isIngredientInStock(ing.ingredientId) ? 'Finns hemma i skafferiet' : 'Saknas i skafferi'"
                  ></span>
                  <span class="font-medium text-gray-900 truncate">{{ ing.name }}</span>
                  <span v-if="ing.notes" class="text-xs text-gray-500 shrink-0">({{ ing.notes }})</span>
                </div>

                <div class="flex items-center gap-2 shrink-0">
                  <span
                    v-if="ing.amount"
                    class="text-xs font-semibold text-gray-700 bg-gray-50 px-2 py-0.5 rounded-md border border-gray-200"
                  >
                    {{ ing.amount }} {{ ing.unit || '' }}
                  </span>
                  <span
                    class="text-[10px] font-semibold px-1.5 py-0.5 rounded-md"
                    :class="
                      pantryStore.isIngredientInStock(ing.ingredientId)
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-amber-100 text-amber-800'
                    "
                  >
                    {{ pantryStore.isIngredientInStock(ing.ingredientId) ? 'Hemma' : 'Saknas' }}
                  </span>
                </div>
              </div>
            </div>
            <p v-else class="text-xs text-gray-400 italic bg-gray-50/60 p-4 rounded-xl border border-gray-100">
              Inga separata ingredienser angivna för detta recept.
            </p>
          </div>

          <!-- Right Column: Instructions -->
          <div class="md:col-span-7 space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="text-sm font-bold uppercase tracking-wider text-gray-900 flex items-center gap-1.5">
                <span>📝</span> Gör så här
              </h3>
              <span
                v-if="instructionSteps.length > 0"
                class="text-xs font-medium text-gray-500"
              >
                {{ instructionSteps.length }} steg
              </span>
            </div>

            <!-- Step by step instructions -->
            <div v-if="instructionSteps.length > 0" class="space-y-3">
              <div
                v-for="(step, index) in instructionSteps"
                :key="index"
                class="flex items-start gap-3.5 p-3.5 sm:p-4 rounded-xl bg-gray-50/70 border border-gray-100 hover:bg-gray-50 transition-colors"
              >
                <span
                  class="shrink-0 w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-emerald-600 text-white font-bold text-xs sm:text-sm flex items-center justify-center shadow-xs mt-0.5"
                >
                  {{ index + 1 }}
                </span>
                <p class="text-sm sm:text-base text-gray-800 leading-relaxed flex-1">
                  {{ getCleanStepText(step) }}
                </p>
              </div>
            </div>
            <p v-else class="text-sm text-gray-400 italic bg-gray-50/60 p-4 rounded-xl border border-gray-100">
              Inga instruktioner tillagda ännu.
            </p>
          </div>
        </div>

        <!-- Created Date info -->
        <div class="text-xs text-gray-400 pt-3 border-t border-gray-100 flex items-center justify-between">
          <span>Skapat: {{ new Date(recipe.createdAt).toLocaleDateString('sv-SE') }}</span>
          <span v-if="recipe.updatedAt">Uppdaterat: {{ new Date(recipe.updatedAt).toLocaleDateString('sv-SE') }}</span>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-5 sm:px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between shrink-0">
        <button
          type="button"
          class="text-xs sm:text-sm font-semibold text-red-600 hover:text-red-700 hover:underline flex items-center gap-1 cursor-pointer"
          @click="emit('delete', recipe.id)"
        >
          Radera recept
        </button>

        <div class="flex items-center gap-2.5">
          <button
            type="button"
            class="px-3.5 sm:px-4 py-2 text-xs sm:text-sm font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 hover:bg-emerald-100 rounded-xl transition-colors flex items-center gap-1.5 cursor-pointer"
            @click="emit('edit', recipe)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            Redigera
          </button>

          <button
            type="button"
            class="px-4 py-2 text-xs sm:text-sm font-semibold text-gray-700 bg-white border border-gray-200 hover:bg-gray-100 rounded-xl shadow-2xs transition-colors cursor-pointer"
            @click="emit('close')"
          >
            Stäng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
