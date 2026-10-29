import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import App from '../App.vue'

describe('App', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Mock global fetch to return an empty recipe list by default
    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => [],
    } as Response)
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

  it('renders the header title, import button and new recipe button', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', redirect: '/recipes' },
        { path: '/recipes', name: 'recipes', component: { template: '<div>Recipes</div>' } },
      ],
    })
    router.push('/recipes')
    await router.isReady()
    
    const wrapper = mount(App, {
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.text()).toContain('Skafferiet')
    expect(wrapper.text()).toContain('Importera')
    expect(wrapper.text()).toContain('Nytt recept')
  })
})
