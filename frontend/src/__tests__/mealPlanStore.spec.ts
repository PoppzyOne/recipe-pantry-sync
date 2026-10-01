import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMealPlanStore } from '../stores/mealPlanStore'

describe('mealPlanStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => [],
    } as Response)
  })

  it('initializes with 7 weekdays starting from Monday', () => {
    const store = useMealPlanStore()
    expect(store.weekDays).toHaveLength(7)
    expect(store.weekDays[0]?.dayName).toBe('Måndag')
    expect(store.weekDays[6]?.dayName).toBe('Söndag')
  })

  it('navigates weeks correctly', () => {
    const store = useMealPlanStore()
    const initialStart = store.weekStartDate

    store.goToNextWeek()
    const nextStart = store.weekStartDate
    expect(nextStart).not.toBe(initialStart)

    store.goToPreviousWeek()
    expect(store.weekStartDate).toBe(initialStart)
  })

  it('fetches meal plans for the week', async () => {
    const mockPlans = [
      {
        id: 1,
        planDate: '2026-10-05',
        mealType: 'DINNER',
        recipeId: 1,
        recipeTitle: 'Pasta Carbonara',
        servings: 4,
        notes: null,
      },
    ]

    global.fetch = vi.fn<typeof fetch>().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockPlans,
    } as Response)

    const store = useMealPlanStore()
    await store.fetchWeekMealPlans()

    expect(store.mealPlans).toHaveLength(1)
    const dinnerItems = store.getItemsForDayAndMeal('2026-10-05', 'DINNER')
    expect(dinnerItems).toHaveLength(1)
    expect(dinnerItems[0]?.recipeTitle).toBe('Pasta Carbonara')
  })
})
