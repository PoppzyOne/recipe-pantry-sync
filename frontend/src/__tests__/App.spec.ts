import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import App from '../App.vue'

describe('App', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Mock global fetch to return an empty recipe list by default
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => [],
    } as Response)
  })

  it('renders the header title and new recipe button', () => {
    const wrapper = mount(App)
    expect(wrapper.text()).toContain('Recipe & Pantry Sync')
    expect(wrapper.text()).toContain('Nytt recept')
  })
})
