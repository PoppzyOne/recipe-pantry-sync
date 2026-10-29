<script setup lang="ts">
import { useThemeStore } from '@/stores/themeStore'
import { computed } from 'vue'

const themeStore = useThemeStore()

const toggle = () => {
  themeStore.toggleTheme()
}

const icon = computed(() => (themeStore.isDarkMode ? '🌙' : '☀️'))
</script>

<template>
  <button
    type="button"
    class="p-2 rounded-lg bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-100 hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
    @click="toggle"
    aria-label="Växla tema"
    :title="themeStore.isDarkMode ? 'Byt till ljust läge' : 'Byt till mörkt läge'"
  >
    {{ icon }}
  </button>
</template>
