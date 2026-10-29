import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  const isDarkMode = ref<boolean>(false)

  // Initialize from localStorage or system preference
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme) {
    isDarkMode.value = savedTheme === 'dark'
  } else {
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  // Apply theme to document
  const applyTheme = (dark: boolean) => {
    if (typeof document !== 'undefined') {
      if (dark) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }

  // Initial application
  applyTheme(isDarkMode.value)

  // Watch for changes to persist and apply
  watch(isDarkMode, (newVal) => {
    applyTheme(newVal)
    localStorage.setItem('theme', newVal ? 'dark' : 'light')
  }, { flush: 'sync' })

  function toggleTheme() {
    isDarkMode.value = !isDarkMode.value
    // Explicitly call applyTheme to ensure DOM is updated synchronously for tests
    applyTheme(isDarkMode.value)
  }

  return {
    isDarkMode,
    toggleTheme,
  }
})
