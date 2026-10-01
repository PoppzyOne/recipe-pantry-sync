import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { mealPlanApi } from '@/api/mealPlanApi'
import type {
  MealPlanItem,
  CreateMealPlanItemDto,
  UpdateMealPlanItemDto,
  MealPlanShoppingItem,
  MealType,
} from '@/types/mealPlan'

const SWEDISH_DAYS = ['Måndag', 'Tisdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lördag', 'Söndag']
const SWEDISH_MONTHS = [
  'jan', 'feb', 'mar', 'apr', 'maj', 'jun',
  'jul', 'aug', 'sep', 'okt', 'nov', 'dec'
]

function getMonday(d: Date): Date {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  date.setHours(0, 0, 0, 0)
  return date
}

function formatDateIso(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getWeekNumber(d: Date): number {
  const target = new Date(d.valueOf())
  const dayNr = (d.getDay() + 6) % 7
  target.setDate(target.getDate() - dayNr + 3)
  const firstThursday = target.valueOf()
  target.setMonth(0, 1)
  if (target.getDay() !== 4) {
    target.setMonth(0, 1 + ((4 - target.getDay() + 7) % 7))
  }
  return 1 + Math.ceil((firstThursday - target.valueOf()) / 604800000)
}

export interface WeekDay {
  date: string
  dayName: string
  shortLabel: string
  formattedDate: string
  isToday: boolean
}

export const useMealPlanStore = defineStore('mealPlan', () => {
  const currentDate = ref<Date>(new Date())
  const mealPlans = ref<MealPlanItem[]>([])
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  const monday = computed(() => getMonday(currentDate.value))

  const sunday = computed(() => {
    const sun = new Date(monday.value)
    sun.setDate(sun.getDate() + 6)
    return sun
  })

  const weekStartDate = computed(() => formatDateIso(monday.value))
  const weekEndDate = computed(() => formatDateIso(sunday.value))

  const weekNumber = computed(() => getWeekNumber(monday.value))
  const year = computed(() => monday.value.getFullYear())

  const weekLabel = computed(() => `Vecka ${weekNumber.value}, ${year.value}`)

  const weekDays = computed<WeekDay[]>(() => {
    const todayIso = formatDateIso(new Date())
    const days: WeekDay[] = []

    for (let i = 0; i < 7; i++) {
      const d = new Date(monday.value)
      d.setDate(d.getDate() + i)
      const dateStr = formatDateIso(d)
      const dayName = SWEDISH_DAYS[i] || 'Måndag'
      const monthName = SWEDISH_MONTHS[d.getMonth()] || ''
      days.push({
        date: dateStr,
        dayName,
        shortLabel: dayName.substring(0, 3),
        formattedDate: `${d.getDate()} ${monthName}`,
        isToday: dateStr === todayIso,
      })
    }
    return days
  })

  const itemsByDate = computed(() => {
    const map = new Map<string, MealPlanItem[]>()
    for (const item of mealPlans.value) {
      if (!map.has(item.planDate)) {
        map.set(item.planDate, [])
      }
      map.get(item.planDate)!.push(item)
    }
    return map
  })

  function getItemsForDayAndMeal(dateStr: string, mealType: MealType): MealPlanItem[] {
    const forDay = itemsByDate.value.get(dateStr) || []
    return forDay.filter((i) => i.mealType === mealType)
  }

  async function fetchWeekMealPlans(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      mealPlans.value = await mealPlanApi.getMealPlans(weekStartDate.value, weekEndDate.value)
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte hämta veckans måltider'
    } finally {
      loading.value = false
    }
  }

  function goToPreviousWeek(): void {
    const newDate = new Date(currentDate.value)
    newDate.setDate(newDate.getDate() - 7)
    currentDate.value = newDate
    fetchWeekMealPlans()
  }

  function goToNextWeek(): void {
    const newDate = new Date(currentDate.value)
    newDate.setDate(newDate.getDate() + 7)
    currentDate.value = newDate
    fetchWeekMealPlans()
  }

  function goToCurrentWeek(): void {
    currentDate.value = new Date()
    fetchWeekMealPlans()
  }

  async function addMealPlanItem(dto: CreateMealPlanItemDto): Promise<MealPlanItem | null> {
    loading.value = true
    error.value = null
    try {
      const created = await mealPlanApi.create(dto)
      mealPlans.value.push(created)
      return created
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte lägga till måltid'
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateMealPlanItem(
    id: number,
    dto: UpdateMealPlanItemDto
  ): Promise<MealPlanItem | null> {
    loading.value = true
    error.value = null
    try {
      const updated = await mealPlanApi.update(id, dto)
      const index = mealPlans.value.findIndex((i) => i.id === id)
      if (index !== -1) {
        mealPlans.value[index] = updated
      }
      return updated
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte uppdatera måltid'
      return null
    } finally {
      loading.value = false
    }
  }

  async function removeMealPlanItem(id: number): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await mealPlanApi.delete(id)
      mealPlans.value = mealPlans.value.filter((i) => i.id !== id)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Kunde inte ta bort måltid'
      return false
    } finally {
      loading.value = false
    }
  }

  async function calculateShoppingList(): Promise<MealPlanShoppingItem[]> {
    return mealPlanApi.calculateShoppingList(weekStartDate.value, weekEndDate.value)
  }

  return {
    currentDate,
    mealPlans,
    loading,
    error,
    monday,
    sunday,
    weekStartDate,
    weekEndDate,
    weekNumber,
    year,
    weekLabel,
    weekDays,
    itemsByDate,
    getItemsForDayAndMeal,
    fetchWeekMealPlans,
    goToPreviousWeek,
    goToNextWeek,
    goToCurrentWeek,
    addMealPlanItem,
    updateMealPlanItem,
    removeMealPlanItem,
    calculateShoppingList,
  }
})
