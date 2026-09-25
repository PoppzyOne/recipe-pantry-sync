<script setup lang="ts">
import { ref } from 'vue'
import { useShoppingListStore } from '@/stores/shoppingListStore'
import type { IngredientCategory } from '@/types/recipe'

const shoppingStore = useShoppingListStore()

const newItemName = ref('')
const selectedCategory = ref<IngredientCategory>('PRODUCE')
const newItemAmount = ref<number | undefined>(undefined)
const newItemUnit = ref<string>('')
const toastMessage = ref<string | null>(null)
const isSyncing = ref(false)

const categoryLabels: Record<IngredientCategory, { label: string; icon: string }> = {
  PRODUCE: { label: 'Frukt & Grönt', icon: '🥬' },
  DAIRY: { label: 'Mejeri & Kyl', icon: '🧀' },
  MEAT: { label: 'Kött, Fågel & Fisk', icon: '🥩' },
  PANTRY: { label: 'Skafferi & Torrvaror', icon: '🌾' },
  SPICES: { label: 'Kryddor & Smak', icon: '🧂' },
  BAKERY: { label: 'Bröd & Bak', icon: '🍞' },
  FROZEN: { label: 'Frysvaror', icon: '🧊' },
  OTHER: { label: 'Övrigt', icon: '📦' },
}

function handleAddItem() {
  if (!newItemName.value.trim()) return

  shoppingStore.addItem({
    name: newItemName.value.trim(),
    category: selectedCategory.value,
    amount: newItemAmount.value,
    unit: newItemUnit.value?.trim() || null,
  })

  newItemName.value = ''
  newItemAmount.value = undefined
  newItemUnit.value = ''
}

async function handleSyncToPantry() {
  isSyncing.value = true
  const count = await shoppingStore.syncCheckedToPantry()
  isSyncing.value = false
  showToast(`${count} köpta varor uppdaterade i skafferiet!`)
}

function showToast(msg: string) {
  toastMessage.value = msg
  setTimeout(() => {
    toastMessage.value = null
  }, 2500)
}
</script>

<template>
  <div class="space-y-6 max-w-4xl mx-auto">
    <!-- Header with offline indicator -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
      <div>
        <div class="flex items-center gap-2">
          <h2 class="text-2xl font-bold text-gray-900">Inköpslista</h2>
          <!-- Offline badge -->
          <span
            v-if="shoppingStore.isOffline"
            class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800"
          >
            <span class="w-1.5 h-1.5 rounded-full bg-amber-600"></span>
            Offline-läge
          </span>
          <span
            v-else
            class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700"
          >
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
            Synkad lokalt
          </span>
        </div>
        <p class="text-sm text-gray-500">
          Sparas automatiskt lokalt – fungerar perfekt i butiken även utan täckning
        </p>
      </div>

      <div class="flex items-center gap-2 self-stretch sm:self-auto">
        <button
          v-if="shoppingStore.checkedItems.length > 0"
          type="button"
          :disabled="isSyncing"
          class="flex-1 sm:flex-none inline-flex items-center justify-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white text-xs font-semibold px-3.5 py-2 rounded-xl shadow-xs transition-colors cursor-pointer"
          @click="handleSyncToPantry"
        >
          <span>✓</span>
          Flytta köpta till skafferi ({{ shoppingStore.checkedItems.length }})
        </button>

        <button
          v-if="shoppingStore.checkedItems.length > 0"
          type="button"
          class="text-xs text-gray-500 hover:text-gray-700 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
          @click="shoppingStore.clearChecked"
        >
          Rensa avbockade
        </button>
      </div>
    </div>

    <!-- Quick Add Input -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-2xs">
      <form @submit.prevent="handleAddItem" class="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
        <input
          v-model="newItemName"
          type="text"
          placeholder="Lägg till vara (t.ex. Smör, Kaffe, Morötter)..."
          class="flex-1 px-3.5 py-2 text-sm rounded-lg border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
        />

        <div class="flex items-center gap-2">
          <input
            v-model="newItemAmount"
            type="number"
            step="any"
            placeholder="Mängd"
            class="w-20 px-2.5 py-2 text-sm rounded-lg border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
          />

          <input
            v-model="newItemUnit"
            type="text"
            placeholder="Enhet"
            class="w-20 px-2.5 py-2 text-sm rounded-lg border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
          />

          <select
            v-model="selectedCategory"
            class="px-2.5 py-2 text-sm rounded-lg border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500 bg-white"
          >
            <option
              v-for="(info, key) in categoryLabels"
              :key="key"
              :value="key"
            >
              {{ info.icon }} {{ info.label }}
            </option>
          </select>

          <button
            type="submit"
            class="bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white px-4 py-2 text-sm font-semibold rounded-lg shadow-xs transition-colors shrink-0 cursor-pointer"
          >
            Lägg till
          </button>
        </div>
      </form>
    </div>

    <!-- Empty state -->
    <div
      v-if="shoppingStore.totalCount === 0"
      class="bg-white rounded-2xl border border-dashed border-gray-300 p-12 text-center max-w-md mx-auto my-8"
    >
      <div class="w-14 h-14 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center mx-auto mb-3 text-2xl">
        🛒
      </div>
      <h3 class="text-base font-bold text-gray-900 mb-1">Inköpslistan är tom</h3>
      <p class="text-xs text-gray-500 mb-6">
        Lägg till varor direkt här, eller gå till dina recept och klicka "Lägg till saknade ingredienser".
      </p>
    </div>

    <!-- Shopping List Grouped by Category -->
    <div v-else class="space-y-5">
      <div
        v-for="[categoryKey, categoryItems] in shoppingStore.itemsByCategory"
        :key="categoryKey"
        class="bg-white rounded-xl border border-gray-200 shadow-2xs overflow-hidden"
      >
        <!-- Aisle / Category header -->
        <div class="bg-gray-50/80 px-4 py-2.5 border-b border-gray-100 flex items-center justify-between text-xs font-bold text-gray-700">
          <div class="flex items-center gap-2">
            <span>{{ categoryLabels[categoryKey as IngredientCategory]?.icon || '📦' }}</span>
            <span>{{ categoryLabels[categoryKey as IngredientCategory]?.label || categoryKey }}</span>
            <span class="text-gray-400 font-normal">({{ categoryItems.length }})</span>
          </div>
        </div>

        <!-- Items list in this category -->
        <ul class="divide-y divide-gray-100">
          <li
            v-for="item in categoryItems"
            :key="item.id"
            class="px-4 py-3 flex items-center justify-between gap-3 hover:bg-gray-50/50 transition-colors group cursor-pointer"
            @click="shoppingStore.toggleItem(item.id)"
          >
            <div class="flex items-center gap-3 min-w-0">
              <input
                type="checkbox"
                :checked="item.checked"
                class="w-5 h-5 rounded-md text-emerald-600 focus:ring-emerald-500 border-gray-300 cursor-pointer shrink-0"
                @click.stop="shoppingStore.toggleItem(item.id)"
              />
              <div class="min-w-0">
                <span
                  class="text-sm font-medium transition-all"
                  :class="item.checked ? 'line-through text-gray-400' : 'text-gray-900'"
                >
                  {{ item.name }}
                </span>
                <span
                  v-if="item.amount"
                  class="ml-2 text-xs font-semibold px-2 py-0.5 rounded-md bg-gray-100 text-gray-600"
                >
                  {{ item.amount }} {{ item.unit || '' }}
                </span>
                <span
                  v-if="item.recipeTitle"
                  class="block text-[11px] text-emerald-600/80 mt-0.5"
                >
                  Från: {{ item.recipeTitle }}
                </span>
              </div>
            </div>

            <button
              type="button"
              class="text-gray-300 hover:text-red-500 p-1 rounded-md opacity-0 group-hover:opacity-100 transition-opacity"
              title="Ta bort"
              @click.stop="shoppingStore.removeItem(item.id)"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </li>
        </ul>
      </div>
    </div>

    <!-- Toast Notification -->
    <div
      v-if="toastMessage"
      class="fixed bottom-6 right-6 z-50 bg-gray-900 text-white text-sm px-4 py-2.5 rounded-xl shadow-lg animate-in fade-in slide-in-from-bottom-2 duration-150 flex items-center gap-2"
    >
      <span>✓</span>
      {{ toastMessage }}
    </div>
  </div>
</template>
