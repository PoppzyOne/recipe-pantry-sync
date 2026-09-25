<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRecipeStore } from '@/stores/recipeStore'
import { usePantryStore } from '@/stores/pantryStore'
import { useShoppingListStore } from '@/stores/shoppingListStore'
import type { Recipe, CreateRecipeDto, UpdateRecipeDto } from '@/types/recipe'
import RecipeCard from '@/components/RecipeCard.vue'
import CreateRecipeModal from '@/components/CreateRecipeModal.vue'
import EditRecipeModal from '@/components/EditRecipeModal.vue'
import RecipeDetailsModal from '@/components/RecipeDetailsModal.vue'
import PantryView from '@/components/pantry/PantryView.vue'
import ShoppingListView from '@/components/shopping/ShoppingListView.vue'

type ActiveTab = 'recipes' | 'pantry' | 'shopping'

const activeTab = ref<ActiveTab>('recipes')

const recipeStore = useRecipeStore()
const pantryStore = usePantryStore()
const shoppingStore = useShoppingListStore()

const isCreateModalOpen = ref(false)
const selectedRecipe = ref<Recipe | null>(null)
const editingRecipe = ref<Recipe | null>(null)
const isSubmitting = ref(false)

onMounted(() => {
  recipeStore.fetchRecipes()
  pantryStore.fetchPantry()
})

async function handleCreateRecipe(dto: CreateRecipeDto) {
  isSubmitting.value = true
  const created = await recipeStore.addRecipe(dto)
  isSubmitting.value = false
  if (created) {
    isCreateModalOpen.value = false
  }
}

async function handleDeleteRecipe(id: number) {
  const confirmed = window.confirm('Är du säker på att du vill ta bort detta recept?')
  if (!confirmed) return

  const success = await recipeStore.removeRecipe(id)
  if (success && selectedRecipe.value?.id === id) {
    selectedRecipe.value = null
  }
}

function handleSelectRecipe(recipe: Recipe) {
  selectedRecipe.value = recipe
}

function handleOpenEditRecipe(recipe: Recipe) {
  selectedRecipe.value = null
  editingRecipe.value = recipe
}

async function handleSaveEditRecipe(dto: UpdateRecipeDto) {
  if (!editingRecipe.value) return
  isSubmitting.value = true
  const updated = await recipeStore.editRecipe(editingRecipe.value.id, dto)
  isSubmitting.value = false
  if (updated) {
    editingRecipe.value = null
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 text-gray-900 flex flex-col font-sans">
    <!-- Navigation header -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-30 shadow-2xs">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-bold text-lg shadow-xs">
            🍳
          </div>
          <div>
            <h1 class="text-lg font-bold text-gray-900 leading-tight">Recipe & Pantry Sync</h1>
            <p class="text-xs text-gray-500">Mina recept, skafferi och inköpslista</p>
          </div>
        </div>

        <!-- Tab navigation -->
        <nav class="hidden sm:flex items-center gap-1 bg-gray-100 p-1 rounded-xl text-xs font-semibold">
          <button
            type="button"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            :class="activeTab === 'recipes' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="activeTab = 'recipes'"
          >
            <span>🍳</span> Recept
          </button>
          <button
            type="button"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            :class="activeTab === 'pantry' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="activeTab = 'pantry'"
          >
            <span>🥫</span> Skafferi
          </button>
          <button
            type="button"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5 relative"
            :class="activeTab === 'shopping' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="activeTab = 'shopping'"
          >
            <span>🛒</span> Inköpslista
            <span
              v-if="shoppingStore.remainingCount > 0"
              class="w-4 h-4 rounded-full bg-emerald-600 text-white text-[10px] font-bold flex items-center justify-center"
            >
              {{ shoppingStore.remainingCount }}
            </span>
          </button>
        </nav>

        <button
          v-if="activeTab === 'recipes'"
          type="button"
          class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white text-sm font-semibold px-4 py-2 rounded-xl shadow-xs transition-colors cursor-pointer"
          @click="isCreateModalOpen = true"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nytt recept
        </button>
        <div v-else class="w-24 sm:w-auto"></div>
      </div>

      <!-- Mobile Tab Bar -->
      <div class="sm:hidden flex border-t border-gray-100 bg-gray-50/50 px-2 py-1 justify-around text-xs font-semibold">
        <button
          type="button"
          class="px-3 py-1.5 rounded-lg transition-colors"
          :class="activeTab === 'recipes' ? 'bg-white text-emerald-700 shadow-2xs' : 'text-gray-500'"
          @click="activeTab = 'recipes'"
        >
          🍳 Recept
        </button>
        <button
          type="button"
          class="px-3 py-1.5 rounded-lg transition-colors"
          :class="activeTab === 'pantry' ? 'bg-white text-emerald-700 shadow-2xs' : 'text-gray-500'"
          @click="activeTab = 'pantry'"
        >
          🥫 Skafferi
        </button>
        <button
          type="button"
          class="px-3 py-1.5 rounded-lg transition-colors relative"
          :class="activeTab === 'shopping' ? 'bg-white text-emerald-700 shadow-2xs' : 'text-gray-500'"
          @click="activeTab = 'shopping'"
        >
          🛒 Inköpslista ({{ shoppingStore.remainingCount }})
        </button>
      </div>
    </header>

    <!-- Main Content Area -->
    <main class="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Error notification banner -->
      <div
        v-if="recipeStore.error || pantryStore.error"
        class="mb-6 p-4 rounded-xl bg-red-50 border border-red-200 flex items-start justify-between gap-3 text-red-800 animate-in fade-in duration-150"
      >
        <div class="flex items-start gap-3">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-red-500 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <p class="text-sm font-semibold">Kommunikationsfel</p>
            <p class="text-xs text-red-700 mt-0.5">{{ recipeStore.error || pantryStore.error }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="text-xs font-semibold px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-800 rounded-lg transition-colors"
            @click="recipeStore.fetchRecipes(); pantryStore.fetchPantry()"
          >
            Försök igen
          </button>
          <button
            type="button"
            class="text-gray-400 hover:text-gray-600 p-1"
            @click="recipeStore.clearError(); pantryStore.clearError()"
          >
            &times;
          </button>
        </div>
      </div>

      <!-- Tab 1: Recept -->
      <section v-if="activeTab === 'recipes'">
        <!-- Loading skeleton -->
        <div v-if="recipeStore.loading && recipeStore.recipes.length === 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="i in 3"
            :key="i"
            class="bg-white rounded-xl border border-gray-200 p-5 animate-pulse space-y-4"
          >
            <div class="h-5 bg-gray-200 rounded-md w-3/4"></div>
            <div class="h-3 bg-gray-100 rounded-md w-full"></div>
            <div class="h-3 bg-gray-100 rounded-md w-2/3"></div>
            <div class="pt-4 border-t border-gray-100 flex justify-between">
              <div class="h-3 bg-gray-200 rounded-md w-1/4"></div>
              <div class="h-3 bg-gray-200 rounded-md w-1/4"></div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div
          v-else-if="recipeStore.recipes.length === 0 && !recipeStore.loading"
          class="bg-white rounded-2xl border border-dashed border-gray-300 p-12 text-center max-w-md mx-auto my-12"
        >
          <div class="w-14 h-14 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center mx-auto mb-4 text-2xl">
            📖
          </div>
          <h3 class="text-lg font-bold text-gray-900 mb-1">Inga recept ännu</h3>
          <p class="text-sm text-gray-500 mb-6">
            Börja med att skapa ditt första recept för att bygga upp din receptbank.
          </p>
          <button
            type="button"
            class="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-semibold px-4 py-2.5 rounded-xl shadow-xs transition-colors cursor-pointer"
            @click="isCreateModalOpen = true"
          >
            Skapa ditt första recept
          </button>
        </div>

        <!-- Recipe list grid -->
        <div v-else>
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-gray-900">
              Sparade recept
              <span class="ml-2 text-xs font-semibold px-2 py-0.5 rounded-full bg-gray-100 text-gray-600">
                {{ recipeStore.recipes.length }}
              </span>
            </h2>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <RecipeCard
              v-for="recipe in recipeStore.recipes"
              :key="recipe.id"
              :recipe="recipe"
              @select="handleSelectRecipe"
              @edit="handleOpenEditRecipe"
              @delete="handleDeleteRecipe"
            />
          </div>
        </div>
      </section>

      <!-- Tab 2: Skafferi -->
      <section v-else-if="activeTab === 'pantry'">
        <PantryView />
      </section>

      <!-- Tab 3: Inköpslista (Offline-first) -->
      <section v-else-if="activeTab === 'shopping'">
        <ShoppingListView />
      </section>
    </main>

    <!-- Modals -->
    <CreateRecipeModal
      v-if="isCreateModalOpen"
      :submitting="isSubmitting"
      @close="isCreateModalOpen = false"
      @save="handleCreateRecipe"
    />

    <EditRecipeModal
      v-if="editingRecipe"
      :recipe="editingRecipe"
      :submitting="isSubmitting"
      @close="editingRecipe = null"
      @save="handleSaveEditRecipe"
    />

    <RecipeDetailsModal
      v-if="selectedRecipe"
      :recipe="selectedRecipe"
      @close="selectedRecipe = null"
      @edit="handleOpenEditRecipe"
      @delete="handleDeleteRecipe"
    />
  </div>
</template>