<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import { useRecipeStore } from '@/stores/recipeStore'
import { usePantryStore } from '@/stores/pantryStore'
import { useShoppingListStore } from '@/stores/shoppingListStore'
import type { Recipe, CreateRecipeDto, UpdateRecipeDto, ImportedRecipeDto } from '@/types/recipe'
import CreateRecipeModal from '@/components/CreateRecipeModal.vue'
import EditRecipeModal from '@/components/EditRecipeModal.vue'
import RecipeDetailsModal from '@/components/RecipeDetailsModal.vue'
import ImportRecipeModal from '@/components/recipe/ImportRecipeModal.vue'

const recipeStore = useRecipeStore()
const pantryStore = usePantryStore()
const shoppingStore = useShoppingListStore()

const isCreateModalOpen = ref(false)
const isImportModalOpen = ref(false)
const importedData = ref<ImportedRecipeDto | null>(null)
const selectedRecipe = ref<Recipe | null>(null)
const editingRecipe = ref<Recipe | null>(null)
const isSubmitting = ref(false)

onMounted(() => {
  recipeStore.fetchRecipes()
  pantryStore.fetchPantry()
})

function handleOpenCreateModal() {
  importedData.value = null
  isCreateModalOpen.value = true
}

function handleOpenImportModal() {
  isCreateModalOpen.value = false
  isImportModalOpen.value = true
}

function handleRecipeImported(recipe: ImportedRecipeDto) {
  isImportModalOpen.value = false
  importedData.value = recipe
  isCreateModalOpen.value = true
}

function handleCloseCreateModal() {
  isCreateModalOpen.value = false
  importedData.value = null
}

async function handleCreateRecipe(dto: CreateRecipeDto) {
  isSubmitting.value = true
  const created = await recipeStore.addRecipe(dto)
  isSubmitting.value = false
  if (created) {
    handleCloseCreateModal()
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

// Expose handlers to child components via provide/inject if needed, 
// or simply pass them as props if we rework views.
// For now, let's keep it simple by providing them.
import { provide } from 'vue'
provide('recipeActions', {
  handleOpenCreateModal,
  handleOpenImportModal,
  handleSelectRecipe,
  handleOpenEditRecipe,
  handleDeleteRecipe
})
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
            <h1 class="text-lg font-bold text-gray-900 leading-tight">Skafferiet</h1>
            <p class="text-xs text-gray-500 hidden sm:block">Recept, skafferi och inköpslista</p>
          </div>
        </div>

        <!-- Tab navigation -->
        <nav class="hidden sm:flex items-center gap-1 bg-gray-100 p-1 rounded-xl text-xs font-semibold">
          <RouterLink
            to="/recipes"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            active-class="bg-white shadow-2xs text-gray-900"
            inactive-class="text-gray-600 hover:text-gray-900"
          >
            <span>🍳</span> Recept
          </RouterLink>
          <RouterLink
            to="/planner"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            active-class="bg-white shadow-2xs text-gray-900"
            inactive-class="text-gray-600 hover:text-gray-900"
          >
            <span>📅</span> Veckoplan
          </RouterLink>
          <RouterLink
            to="/pantry"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
            active-class="bg-white shadow-2xs text-gray-900"
            inactive-class="text-gray-600 hover:text-gray-900"
          >
            <span>🥫</span> Skafferi
          </RouterLink>
          <RouterLink
            to="/shopping"
            class="px-3.5 py-1.5 rounded-lg transition-colors cursor-pointer flex items-center gap-1.5 relative"
            active-class="bg-white shadow-2xs text-gray-900"
            inactive-class="text-gray-600 hover:text-gray-900"
          >
            <span>🛒</span> Inköpslista
            <span
              v-if="shoppingStore.remainingCount > 0"
              class="w-4 h-4 rounded-full bg-emerald-600 text-white text-[10px] font-bold flex items-center justify-center"
            >
              {{ shoppingStore.remainingCount }}
            </span>
          </RouterLink>
        </nav>

        <div class="flex items-center gap-2">
          <button
            v-if="$route.name === 'recipes'"
            type="button"
            class="inline-flex items-center gap-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs sm:text-sm font-semibold px-2.5 sm:px-3 py-2 rounded-xl transition-colors cursor-pointer shrink-0"
            title="Importera recept"
            @click="handleOpenImportModal"
          >
            <span>📥</span>
            <span class="hidden sm:inline">Importera</span>
          </button>
          <button
            v-if="$route.name === 'recipes'"
            type="button"
            class="inline-flex items-center gap-1.5 sm:gap-2 bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white text-xs sm:text-sm font-semibold px-2.5 sm:px-4 py-2 rounded-xl shadow-xs transition-colors cursor-pointer shrink-0"
            title="Skapa nytt recept"
            @click="handleOpenCreateModal"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            <span class="hidden sm:inline">Nytt recept</span>
          </button>
        </div>
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

      <RouterView />
    </main>

    <!-- Modals -->
    <CreateRecipeModal
      v-if="isCreateModalOpen"
      :initial-data="importedData"
      :submitting="isSubmitting"
      @close="handleCloseCreateModal"
      @save="handleCreateRecipe"
      @open-import="handleOpenImportModal"
    />

    <ImportRecipeModal
      v-if="isImportModalOpen"
      @close="isImportModalOpen = false"
      @imported="handleRecipeImported"
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