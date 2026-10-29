import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ThemeToggle from '../components/ThemeToggle.vue'
import { useThemeStore } from '../stores/themeStore'

describe('ThemeToggle', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.stubGlobal('matchMedia', vi.fn().mockImplementation((query) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })))
  })

  it('renders correctly and toggles theme on click', async () => {
    const wrapper = mount(ThemeToggle)
    const store = useThemeStore()

    // Should show light mode icon initially (isDarkMode is false by default in our mock)
    expect(wrapper.text()).toContain('☀️')

    await wrapper.trigger('click')
    
    expect(store.isDarkMode).toBe(true)
    expect(wrapper.text()).toContain('🌙')
  })
})
