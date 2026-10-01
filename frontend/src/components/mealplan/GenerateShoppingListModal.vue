<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { MealPlanShoppingItem } from '@/types/mealPlan'
import { CATEGORY_LABELS } from '@/types/recipe'
import { useShoppingListStore } from '@/stores/shoppingListStore'

const props = defineProps<{
  isOpen: boolean
  weekLabel: string
  fetchItems: () => Promise<MealPlanShoppingItem[]>
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success', count: number): void
}>()

const shoppingStore = useShoppingListStore()

const loading = ref(true)
const items = ref<MealPlanShoppingItem[]>([])
const selectedIngredientIds = ref<Set<number>>(new Set())
const submitting = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const list = await props.fetchItems()
    items.value = list
    // Pre-select items that are missing
    const selected = new Set<number>()
    for (const item of list) {
      if (item.missingAmount > 0) {
        selected.add(item.ingredientId)
      }
    }
    selectedIngredientIds.value = selected
  } catch (e) {
    console.error('Failed to calculate shopping list', e)
  } finally {
    loading.value = false
  }
})

const missingItems = computed(() => items.value.filter((i) => i.missingAmount > 0))
const inStockItems = computed(() => items.value.filter((i) => i.missingAmount === 0))

function toggleSelection(id: number) {
  const next = new Set(selectedIngredientIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  selectedIngredientIds.value = next
}

function selectAllMissing() {
  const selected = new Set<number>()
  for (const item of items.value) {
    if (item.missingAmount > 0) {
      selected.add(item.ingredientId)
    }
  }
  selectedIngredientIds.value = selected
}

function deselectAll() {
  selectedIngredientIds.value = new Set()
}

function handleAddSelected() {
  submitting.value = true
  let addedCount = 0

  for (const item of items.value) {
    if (selectedIngredientIds.value.has(item.ingredientId)) {
      shoppingStore.addItem({
        ingredientId: item.ingredientId,
        name: item.name,
        category: item.category,
        amount: item.missingAmount > 0 ? item.missingAmount : item.neededAmount,
        unit: item.unit,
        recipeTitle: item.recipeTitles.length > 0 ? item.recipeTitles.join(', ') : props.weekLabel,
      })
      addedCount++
    }
  }

  submitting.value = false
  emit('success', addedCount)
  emit('close')
}
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4"
    @click.self="emit('close')"
  >
    <div
      class="bg-white rounded-2xl shadow-xl w-full max-w-xl max-h-[85vh] flex flex-col overflow-hidden border border-gray-100 animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <div>
          <h2 class="text-base font-bold text-gray-900 flex items-center gap-2">
            <span>🛒 Skapa inköpslista</span>
            <span class="text-xs bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded-full font-semibold border border-emerald-200">
              {{ weekLabel }}
            </span>
          </h2>
          <p class="text-xs text-gray-500 mt-0.5">
            Jämför veckans recept mot skafferiet och lägg till det som saknas.
          </p>
        </div>
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

      <!-- Content -->
      <div class="p-6 overflow-y-auto flex-1 space-y-4">
        <!-- Loading State -->
        <div v-if="loading" class="py-12 text-center text-gray-500 text-sm">
          <div class="inline-block animate-spin text-2xl mb-2">⏳</div>
          <p class="font-medium">Beräknar ingredienser och stämmer av mot skafferiet...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="items.length === 0" class="py-10 text-center text-gray-500">
          <div class="text-3xl mb-2">🥗</div>
          <p class="text-sm font-semibold text-gray-700">Inga ingredienser att handla</p>
          <p class="text-xs text-gray-500 mt-1">
            Du har inga recept med ingredienser inlagda i veckomenyn än.
          </p>
        </div>

        <!-- Items List -->
        <div v-else class="space-y-4">
          <!-- Selection Toolbar -->
          <div class="flex items-center justify-between text-xs pb-1 border-b border-gray-100">
            <span class="text-gray-600 font-medium">
              Valt <strong>{{ selectedIngredientIds.size }}</strong> av {{ items.length }} varor
            </span>
            <div class="flex items-center gap-2">
              <button
                type="button"
                @click="selectAllMissing"
                class="text-emerald-700 hover:text-emerald-800 font-semibold cursor-pointer"
              >
                Välj alla saknade
              </button>
              <span class="text-gray-300">•</span>
              <button
                type="button"
                @click="deselectAll"
                class="text-gray-500 hover:text-gray-700 font-semibold cursor-pointer"
              >
                Avmarkera alla
              </button>
            </div>
          </div>

          <!-- Missing Items Section -->
          <div v-if="missingItems.length > 0" class="space-y-2">
            <h3 class="text-xs font-bold text-amber-800 uppercase tracking-wider flex items-center gap-1.5">
              <span>⚠️</span> Behöver köpas ({{ missingItems.length }})
            </h3>
            <div class="space-y-1.5">
              <div
                v-for="item in missingItems"
                :key="item.ingredientId"
                @click="toggleSelection(item.ingredientId)"
                class="flex items-center justify-between p-3 rounded-xl border transition-all cursor-pointer select-none"
                :class="
                  selectedIngredientIds.has(item.ingredientId)
                    ? 'border-emerald-500 bg-emerald-50/50'
                    : 'border-gray-200 hover:border-gray-300 bg-white'
                "
              >
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    :checked="selectedIngredientIds.has(item.ingredientId)"
                    class="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 border-gray-300 cursor-pointer pointer-events-none"
                  />
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-semibold text-gray-900">{{ item.name }}</span>
                      <span
                        class="text-[10px] px-1.5 py-0.5 rounded-full border font-medium"
                        :class="CATEGORY_LABELS[item.category]?.color || 'bg-gray-50 text-gray-700 border-gray-200'"
                      >
                        {{ CATEGORY_LABELS[item.category]?.label || item.category }}
                      </span>
                    </div>
                    <div class="text-[11px] text-gray-500 mt-0.5 flex items-center gap-2">
                      <span>Från: {{ item.recipeTitles.join(', ') }}</span>
                      <span v-if="item.pantryAmount > 0" class="text-amber-700">
                        (Finns {{ item.pantryAmount }} {{ item.unit }} i skafferi)
                      </span>
                    </div>
                  </div>
                </div>

                <div class="text-right">
                  <div class="text-xs font-bold text-gray-900">
                    {{ item.missingAmount }} {{ item.unit || 'st' }}
                  </div>
                  <span class="text-[10px] text-amber-600 font-semibold bg-amber-50 px-1.5 py-0.5 rounded-sm">
                    Saknas
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- In-Stock Items Section (Already in pantry) -->
          <div v-if="inStockItems.length > 0" class="space-y-2 pt-2">
            <h3 class="text-xs font-bold text-emerald-800 uppercase tracking-wider flex items-center gap-1.5">
              <span>✅</span> Finns redan i skafferiet ({{ inStockItems.length }})
            </h3>
            <div class="space-y-1.5 opacity-75">
              <div
                v-for="item in inStockItems"
                :key="item.ingredientId"
                @click="toggleSelection(item.ingredientId)"
                class="flex items-center justify-between p-2.5 rounded-xl border border-gray-200 bg-gray-50 hover:bg-white transition-all cursor-pointer select-none"
              >
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    :checked="selectedIngredientIds.has(item.ingredientId)"
                    class="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 border-gray-300 cursor-pointer pointer-events-none"
                  />
                  <div>
                    <span class="text-xs font-medium text-gray-700">{{ item.name }}</span>
                    <span class="text-[10px] text-gray-400 ml-2">({{ item.recipeTitles.join(', ') }})</span>
                  </div>
                </div>

                <div class="text-right flex items-center gap-2">
                  <span class="text-[11px] text-gray-500">{{ item.neededAmount }} {{ item.unit || 'st' }}</span>
                  <span class="text-[10px] text-emerald-700 font-semibold bg-emerald-50 border border-emerald-200 px-1.5 py-0.5 rounded-sm">
                    I skafferiet
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer Actions -->
      <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between bg-gray-50">
        <button
          type="button"
          class="px-4 py-2 rounded-xl text-xs font-semibold text-gray-600 hover:text-gray-900 transition-colors cursor-pointer"
          @click="emit('close')"
        >
          Stäng
        </button>
        <button
          type="button"
          :disabled="selectedIngredientIds.size === 0 || submitting"
          @click="handleAddSelected"
          class="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 shadow-xs transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          <span>🛒</span>
          <span>Lägg till {{ selectedIngredientIds.size }} varor i inköpslistan</span>
        </button>
      </div>
    </div>
  </div>
</template>
