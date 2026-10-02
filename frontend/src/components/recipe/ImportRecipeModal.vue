<script setup lang="ts">
import { ref } from 'vue'
import { useRecipeStore } from '@/stores/recipeStore'
import type { ImportedRecipeDto } from '@/types/recipe'

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'imported', recipe: ImportedRecipeDto): void
}>()

const recipeStore = useRecipeStore()

type ImportMode = 'URL' | 'TEXT'
const mode = ref<ImportMode>('URL')
const urlInput = ref('')
const textInput = ref('')
const errorMessage = ref<string | null>(null)
const isImporting = ref(false)

async function handleImportUrl() {
  if (!urlInput.value.trim()) {
    errorMessage.value = 'Vänligen ange en webbadress till receptet.'
    return
  }

  errorMessage.value = null
  isImporting.value = true

  const result = await recipeStore.importRecipe({ url: urlInput.value.trim() })
  isImporting.value = false

  if (result) {
    emit('imported', result)
  } else {
    errorMessage.value = recipeStore.error || 'Kunde inte importera receptet från länken.'
  }
}

async function handleImportText() {
  if (!textInput.value.trim()) {
    errorMessage.value = 'Klistra in recepttexten du vill tolka.'
    return
  }

  errorMessage.value = null
  isImporting.value = true

  const result = await recipeStore.importRecipe({ text: textInput.value.trim() })
  isImporting.value = false

  if (result) {
    emit('imported', result)
  } else {
    errorMessage.value = recipeStore.error || 'Kunde inte tolka recepttexten.'
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
    <div
      class="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-gray-100 flex flex-col animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Modal Header -->
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between shrink-0">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center text-lg">
            📥
          </div>
          <div>
            <h2 class="text-lg font-bold text-gray-900 leading-tight">Importera recept</h2>
            <p class="text-xs text-gray-500">Hämta från valfri receptsajt eller klistra in text</p>
          </div>
        </div>
        <button
          type="button"
          @click="emit('close')"
          class="text-gray-400 hover:text-gray-600 p-1 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
        >
          ✕
        </button>
      </div>

      <!-- Mode Selector Tabs -->
      <div class="px-6 pt-4">
        <div class="flex items-center gap-1 bg-gray-100 p-1 rounded-xl text-xs font-semibold">
          <button
            type="button"
            class="flex-1 py-1.5 rounded-lg transition-colors cursor-pointer text-center"
            :class="mode === 'URL' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="mode = 'URL'; errorMessage = null"
          >
            🔗 Från webbadress
          </button>
          <button
            type="button"
            class="flex-1 py-1.5 rounded-lg transition-colors cursor-pointer text-center"
            :class="mode === 'TEXT' ? 'bg-white shadow-2xs text-gray-900' : 'text-gray-600 hover:text-gray-900'"
            @click="mode = 'TEXT'; errorMessage = null"
          >
            📝 Klistra in text
          </button>
        </div>
      </div>

      <!-- Modal Body -->
      <div class="p-6 space-y-4">
        <!-- Error Banner -->
        <div
          v-if="errorMessage"
          class="p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-xl flex items-start gap-2"
        >
          <span class="text-base shrink-0">⚠️</span>
          <div class="flex-1">
            <p class="font-semibold">Import misslyckades</p>
            <p>{{ errorMessage }}</p>
          </div>
          <button
            type="button"
            @click="errorMessage = null"
            class="text-red-400 hover:text-red-600 cursor-pointer"
          >
            ✕
          </button>
        </div>

        <!-- Mode URL -->
        <div v-if="mode === 'URL'" class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">
              Receptlänk (URL)
            </label>
            <input
              v-model="urlInput"
              type="url"
              placeholder="https://www.ica.se/recept/..."
              :disabled="isImporting"
              @keydown.enter.prevent="handleImportUrl"
              class="w-full px-3.5 py-2.5 text-sm rounded-xl border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all bg-gray-50/50 focus:bg-white disabled:opacity-50"
            />
          </div>

          <div class="bg-emerald-50/60 border border-emerald-100/80 rounded-xl p-3 text-xs text-emerald-800 space-y-1">
            <p class="font-semibold flex items-center gap-1.5">
              <span>💡</span>
              Stöder de flesta receptsajter
            </p>
            <p class="text-emerald-700 leading-relaxed">
              Fungerar utmärkt med ICA, Coop, Arla, Köket.se, Tasteline, BBC Good Food m.fl. som använder standardiserad schema.org-data.
            </p>
          </div>
        </div>

        <!-- Mode TEXT -->
        <div v-else class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-gray-700 mb-1">
              Recepttext
            </label>
            <textarea
              v-model="textInput"
              rows="8"
              :disabled="isImporting"
              placeholder="Krämig Laxpasta&#10;4 portioner&#10;25 min&#10;&#10;Ingredienser:&#10;400 g laxfilé&#10;2 dl grädde&#10;1 st lök&#10;&#10;Gör så här:&#10;1. Stek löken och laxen.&#10;2. Häll på grädden..."
              class="w-full px-3.5 py-2.5 text-xs sm:text-sm font-mono rounded-xl border border-gray-200 focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all bg-gray-50/50 focus:bg-white resize-none disabled:opacity-50"
            ></textarea>
          </div>

          <p class="text-xs text-gray-500">
            Dela gärna upp texten med rubriker som <em>"Ingredienser:"</em> och <em>"Gör så här:"</em> för bäst resultat.
          </p>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end gap-3 shrink-0">
        <button
          type="button"
          @click="emit('close')"
          :disabled="isImporting"
          class="px-4 py-2 text-xs font-semibold text-gray-700 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50"
        >
          Avbryt
        </button>

        <button
          v-if="mode === 'URL'"
          type="button"
          @click="handleImportUrl"
          :disabled="isImporting || !urlInput.trim()"
          class="inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="isImporting" class="inline-block animate-spin">⏳</span>
          <span>{{ isImporting ? 'Hämtar och tolkar...' : 'Hämta recept' }}</span>
        </button>

        <button
          v-else
          type="button"
          @click="handleImportText"
          :disabled="isImporting || !textInput.trim()"
          class="inline-flex items-center gap-2 px-5 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="isImporting" class="inline-block animate-spin">⏳</span>
          <span>{{ isImporting ? 'Tolkar recept...' : 'Tolka och importera' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
