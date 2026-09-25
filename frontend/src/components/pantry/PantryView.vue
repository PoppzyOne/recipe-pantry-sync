<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { usePantryStore } from '@/stores/pantryStore'
import { useShoppingListStore } from '@/stores/shoppingListStore'
import type { CreatePantryItemDto, UpdatePantryItemDto, PantryItem } from '@/types/pantry'
import type { IngredientCategory } from '@/types/recipe'
import PantryItemCard from './PantryItemCard.vue'
import AddPantryItemModal from './AddPantryItemModal.vue'
import EditPantryItemModal from './EditPantryItemModal.vue'

const pantryStore = usePantryStore()
const shoppingStore = useShoppingListStore()

const isAddModalOpen = ref(false)
const editingItem = ref<PantryItem | null>(null)
const isSubmitting = ref(false)
const toastMessage = ref<string | null>(null)

onMounted(() => {
  pantryStore.fetchPantry()
})

const categories: { value: IngredientCategory | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'Alla' },
  { value: 'PANTRY', label: 'Skafferi' },
  { value: 'DAIRY', label: 'Mejeri' },
  { value: 'PRODUCE', label: 'Frukt & Grönt' },
  { value: 'MEAT', label: 'Kött & Fisk' },
  { value: 'SPICES', label: 'Kryddor' },
  { value: 'BAKERY', label: 'Bageri' },
  { value: 'FROZEN', label: 'Frys' },
]

async function handleSaveItem(dto: CreatePantryItemDto) {
  isSubmitting.value = true
  const created = await pantryStore.addItem(dto)
  isSubmitting.value = false
  if (created) {
    isAddModalOpen.value = false
    showToast(`"${created.ingredientName}" lades till i skafferiet`)
  }
}

async function handleUpdateItem(dto: UpdatePantryItemDto) {
  if (!editingItem.value) return
  isSubmitting.value = true
  const updated = await pantryStore.updateItem(editingItem.value.id, dto)
  isSubmitting.value = false
  if (updated) {
    showToast(`"${updated.ingredientName}" uppdaterades`)
    editingItem.value = null
  }
}

function handleAddToShoppingList(item: PantryItem) {
  shoppingStore.addItem({
    ingredientId: item.ingredientId,
    name: item.ingredientName,
    category: item.category,
    amount: item.quantity,
    unit: item.unit,
  })
  showToast(`"${item.ingredientName}" lades till i inköpslistan`)
}

function showToast(msg: string) {
  toastMessage.value = msg
  setTimeout(() => {
    toastMessage.value = null
  }, 2500)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Action and summary bar -->
    <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-gray-900">Skafferiet</h2>
        <p class="text-sm text-gray-500">
          Håll koll på vad du har hemma för automatisk receptsynkning
        </p>
      </div>

      <button
        type="button"
        class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white text-sm font-semibold px-4 py-2 rounded-xl shadow-xs transition-colors cursor-pointer"
        @click="isAddModalOpen = true"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Lägg till vara
      </button>
    </div>

    <!-- Filters & Search -->
    <div class="bg-white p-4 rounded-xl border border-gray-200 shadow-2xs space-y-3">
      <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <!-- Search -->
        <div class="relative flex-1 max-w-md">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-4 h-4 text-gray-400 absolute left-3 top-3"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            v-model="pantryStore.searchQuery"
            type="text"
            placeholder="Sök i skafferiet..."
            class="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <!-- Stock state segmented control -->
        <div class="inline-flex rounded-lg bg-gray-100 p-1 text-xs font-semibold self-start sm:self-auto">
          <button
            type="button"
            class="px-3 py-1.5 rounded-md transition-colors cursor-pointer"
            :class="pantryStore.filterStock === 'ALL' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="pantryStore.filterStock = 'ALL'"
          >
            Alla ({{ pantryStore.items.length }})
          </button>
          <button
            type="button"
            class="px-3 py-1.5 rounded-md transition-colors cursor-pointer"
            :class="pantryStore.filterStock === 'IN_STOCK' ? 'bg-white shadow-2xs text-emerald-700' : 'text-gray-600 hover:text-gray-900'"
            @click="pantryStore.filterStock = 'IN_STOCK'"
          >
            Finns hemma ({{ pantryStore.inStockCount }})
          </button>
          <button
            type="button"
            class="px-3 py-1.5 rounded-md transition-colors cursor-pointer"
            :class="pantryStore.filterStock === 'OUT_OF_STOCK' ? 'bg-white shadow-2xs text-amber-700' : 'text-gray-600 hover:text-gray-900'"
            @click="pantryStore.filterStock = 'OUT_OF_STOCK'"
          >
            Slut ({{ pantryStore.outOfStockCount }})
          </button>
        </div>
      </div>

      <!-- Category chips -->
      <div class="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
        <button
          v-for="cat in categories"
          :key="cat.value"
          type="button"
          class="px-3 py-1 rounded-full font-medium transition-colors whitespace-nowrap cursor-pointer"
          :class="
            pantryStore.activeCategory === cat.value
              ? 'bg-emerald-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          "
          @click="pantryStore.activeCategory = cat.value"
        >
          {{ cat.label }}
        </button>
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

    <!-- Items Grid -->
    <div v-if="pantryStore.loading && pantryStore.items.length === 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="i in 4" :key="i" class="bg-white rounded-xl border border-gray-200 p-4 animate-pulse space-y-3">
        <div class="h-4 bg-gray-200 rounded-md w-1/3"></div>
        <div class="h-5 bg-gray-300 rounded-md w-3/4"></div>
        <div class="h-4 bg-gray-100 rounded-md w-1/2"></div>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-else-if="pantryStore.filteredItems.length === 0"
      class="bg-white rounded-2xl border border-dashed border-gray-300 p-10 text-center max-w-sm mx-auto my-8"
    >
      <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-xl flex items-center justify-center mx-auto mb-3 text-xl">
        🥫
      </div>
      <h3 class="text-base font-bold text-gray-900 mb-1">Inga varor matchar</h3>
      <p class="text-xs text-gray-500 mb-4">
        Prova att ändra filtrering eller lägg till varor i skafferiet.
      </p>
      <button
        type="button"
        class="inline-flex items-center gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold px-3.5 py-2 rounded-lg transition-colors cursor-pointer"
        @click="isAddModalOpen = true"
      >
        Lägg till en vara
      </button>
    </div>

    <!-- Cards Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <PantryItemCard
        v-for="item in pantryStore.filteredItems"
        :key="item.id"
        :item="item"
        @toggle="pantryStore.toggleStock"
        @edit="item => editingItem = item"
        @delete="pantryStore.removeItem"
        @add-to-shopping-list="handleAddToShoppingList"
      />
    </div>

    <!-- Add Item Modal -->
    <AddPantryItemModal
      v-if="isAddModalOpen"
      :submitting="isSubmitting"
      @close="isAddModalOpen = false"
      @save="handleSaveItem"
    />

    <!-- Edit Item Modal -->
    <EditPantryItemModal
      v-if="editingItem"
      :item="editingItem"
      :submitting="isSubmitting"
      @close="editingItem = null"
      @save="handleUpdateItem"
    />
  </div>
</template>
