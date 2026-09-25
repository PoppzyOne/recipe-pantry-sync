<script setup lang="ts">
import { ref } from 'vue'
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
  } catch (e) {
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
  <div class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150">
      <div class="px-6 py-5 border-b border-gray-100 flex items-start justify-between gap-4">
        <div>
          <span class="inline-block px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 mb-2">
            Recept #{{ recipe.id }}
          </span>
          <h2 class="text-2xl font-bold text-gray-900 leading-tight">
            {{ recipe.title }}
          </h2>
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

      <div class="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
        <p v-if="recipe.description" class="whitespace-pre-line text-gray-600 text-sm leading-relaxed">
          {{ formatLineBreaks(recipe.description) }}
        </p>

        <!-- Stats grid -->
        <div class="grid grid-cols-3 gap-3 bg-gray-50 p-4 rounded-xl text-center">
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
        </div>

        <!-- Ingredients Section with Pantry status -->
        <div>
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-bold uppercase tracking-wider text-gray-900">
              Ingredienser ({{ recipe.ingredients?.length || 0 }})
            </h3>
            <button
              v-if="recipe.ingredients && recipe.ingredients.length > 0"
              type="button"
              :disabled="isAddingToShopping"
              class="text-xs font-semibold text-emerald-700 hover:text-emerald-800 bg-emerald-50 hover:bg-emerald-100 px-2.5 py-1.5 rounded-lg transition-colors flex items-center gap-1 cursor-pointer disabled:opacity-50"
              @click="handleAddMissingToShoppingList"
            >
              <span>🛒</span>
              Synka saknade till inköpslista
            </button>
          </div>

          <!-- Sync notification -->
          <div
            v-if="syncMessage"
            class="mb-3 p-2.5 rounded-lg bg-emerald-50 border border-emerald-200 text-xs font-medium text-emerald-800 animate-in fade-in"
          >
            {{ syncMessage }}
          </div>

          <div v-if="recipe.ingredients && recipe.ingredients.length > 0" class="divide-y divide-gray-100 border border-gray-100 rounded-xl overflow-hidden bg-gray-50/40">
            <div
              v-for="ing in recipe.ingredients"
              :key="ing.id"
              class="px-3.5 py-2.5 flex items-center justify-between gap-3 text-sm"
            >
              <div class="flex items-center gap-2">
                <span
                  class="w-2 h-2 rounded-full shrink-0"
                  :class="pantryStore.isIngredientInStock(ing.ingredientId) ? 'bg-emerald-500' : 'bg-amber-400'"
                  :title="pantryStore.isIngredientInStock(ing.ingredientId) ? 'Finns hemma' : 'Saknas i skafferi'"
                ></span>
                <span class="font-medium text-gray-900">{{ ing.name }}</span>
                <span v-if="ing.notes" class="text-xs text-gray-500">({{ ing.notes }})</span>
              </div>

              <div class="flex items-center gap-2">
                <span v-if="ing.amount" class="text-xs font-semibold text-gray-700 bg-white px-2 py-0.5 rounded-md border border-gray-200">
                  {{ ing.amount }} {{ ing.unit || '' }}
                </span>
                <span
                  class="text-[10px] font-semibold px-1.5 py-0.5 rounded-sm"
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
          <p v-else class="text-xs text-gray-400 italic">
            Inga separata ingredienser angivna för detta recept.
          </p>
        </div>

        <!-- Instructions -->
        <div>
          <h3 class="text-sm font-bold uppercase tracking-wider text-gray-900 mb-2">
            Instruktioner
          </h3>
          <div v-if="recipe.instructions" class="whitespace-pre-line text-sm text-gray-700 leading-relaxed bg-gray-50/60 p-4 rounded-xl border border-gray-100">
            {{ formatLineBreaks(recipe.instructions) }}
          </div>
          <p v-else class="text-sm text-gray-400 italic">
            Inga instruktioner tillagda ännu.
          </p>
        </div>

        <div class="text-xs text-gray-400">
          Skapat: {{ new Date(recipe.createdAt).toLocaleDateString('sv-SE') }}
        </div>
      </div>

      <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-between">
        <button
          type="button"
          class="text-xs font-semibold text-red-600 hover:text-red-700 hover:underline flex items-center gap-1"
          @click="emit('delete', recipe.id)"
        >
          Radera recept
        </button>

        <div class="flex items-center gap-2">
          <button
            type="button"
            class="px-3.5 py-2 text-sm font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 hover:bg-emerald-100 rounded-lg transition-colors flex items-center gap-1.5 cursor-pointer"
            @click="emit('edit', recipe)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            Redigera
          </button>

          <button
            type="button"
            class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-200 hover:bg-gray-100 rounded-lg shadow-2xs transition-colors cursor-pointer"
            @click="emit('close')"
          >
            Stäng
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
