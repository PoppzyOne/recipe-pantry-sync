<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMealPlanStore, type WeekDay } from '@/stores/mealPlanStore'
import { useRecipeStore } from '@/stores/recipeStore'
import type { MealPlanItem, MealType } from '@/types/mealPlan'
import { MEAL_TYPE_LABELS } from '@/types/mealPlan'
import MealModal from './MealModal.vue'
import GenerateShoppingListModal from './GenerateShoppingListModal.vue'

const mealPlanStore = useMealPlanStore()
const recipeStore = useRecipeStore()

const isMealModalOpen = ref(false)
const isShoppingModalOpen = ref(false)
const selectedDate = ref('')
const selectedMealType = ref<MealType>('DINNER')
const editingMeal = ref<MealPlanItem | null>(null)
const isSubmitting = ref(false)
const notificationMessage = ref<string | null>(null)

onMounted(() => {
  mealPlanStore.fetchWeekMealPlans()
  if (recipeStore.recipes.length === 0) {
    recipeStore.fetchRecipes()
  }
})

function showNotification(msg: string) {
  notificationMessage.value = msg
  setTimeout(() => {
    notificationMessage.value = null
  }, 4000)
}

function openAddMeal(day?: WeekDay, mealType: MealType = 'DINNER') {
  editingMeal.value = null
  selectedDate.value = day ? day.date : mealPlanStore.weekStartDate
  selectedMealType.value = mealType
  isMealModalOpen.value = true
}

function openEditMeal(meal: MealPlanItem) {
  editingMeal.value = meal
  selectedDate.value = meal.planDate
  selectedMealType.value = meal.mealType
  isMealModalOpen.value = true
}

async function handleSaveMeal(data: {
  planDate: string
  mealType: MealType
  recipeId: number | null
  customTitle: string | null
  servings: number
  notes: string | null
}) {
  isSubmitting.value = true
  if (editingMeal.value) {
    const updated = await mealPlanStore.updateMealPlanItem(editingMeal.value.id, data)
    if (updated) {
      showNotification('Måltiden har uppdaterats!')
      isMealModalOpen.value = false
    }
  } else {
    const created = await mealPlanStore.addMealPlanItem(data)
    if (created) {
      showNotification('Måltid har lagts till i veckoplanen!')
      isMealModalOpen.value = false
    }
  }
  isSubmitting.value = false
}

async function handleDeleteMeal(id: number) {
  const confirmed = window.confirm('Vill du ta bort denna måltid från veckoplanen?')
  if (!confirmed) return

  isSubmitting.value = true
  const ok = await mealPlanStore.removeMealPlanItem(id)
  isSubmitting.value = false
  if (ok) {
    showNotification('Måltiden har tagits bort.')
    isMealModalOpen.value = false
  }
}

function handleShoppingSuccess(count: number) {
  showNotification(`Lade till ${count} varor i inköpslistan!`)
}
</script>

<template>
  <div class="space-y-6">
    <!-- Notification Toast -->
    <div
      v-if="notificationMessage"
      class="fixed bottom-6 right-6 z-50 bg-gray-900 text-white px-4 py-3 rounded-2xl shadow-xl border border-gray-800 flex items-center gap-3 text-xs font-semibold animate-in slide-in-from-bottom-5 duration-200"
    >
      <span class="text-base">✨</span>
      <span>{{ notificationMessage }}</span>
      <button
        type="button"
        @click="notificationMessage = null"
        class="text-gray-400 hover:text-white p-0.5 rounded cursor-pointer"
      >
        ✕
      </button>
    </div>

    <!-- Top Toolbar: Week Navigator & Actions -->
    <div class="bg-white p-4 sm:p-5 rounded-2xl border border-gray-200 shadow-2xs flex flex-col md:flex-row items-center justify-between gap-4">
      <!-- Week Navigation -->
      <div class="flex items-center gap-2 sm:gap-3 w-full md:w-auto justify-between md:justify-start">
        <button
          type="button"
          @click="mealPlanStore.goToPreviousWeek"
          class="w-9 h-9 rounded-xl border border-gray-200 hover:bg-gray-50 flex items-center justify-center text-gray-600 transition-colors cursor-pointer"
          title="Föregående vecka"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <div class="text-center">
          <div class="flex items-center justify-center gap-2">
            <span class="text-base font-extrabold text-gray-900 tracking-tight">
              {{ mealPlanStore.weekLabel }}
            </span>
            <button
              type="button"
              @click="mealPlanStore.goToCurrentWeek"
              class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200 transition-colors cursor-pointer"
            >
              Idag
            </button>
          </div>
          <span class="text-xs text-gray-500 font-medium">
            {{ mealPlanStore.weekStartDate }} – {{ mealPlanStore.weekEndDate }}
          </span>
        </div>

        <button
          type="button"
          @click="mealPlanStore.goToNextWeek"
          class="w-9 h-9 rounded-xl border border-gray-200 hover:bg-gray-50 flex items-center justify-center text-gray-600 transition-colors cursor-pointer"
          title="Nästa vecka"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>

      <!-- Action Buttons -->
      <div class="flex items-center gap-2.5 w-full md:w-auto justify-end">
        <button
          type="button"
          @click="isShoppingModalOpen = true"
          :disabled="mealPlanStore.mealPlans.length === 0"
          class="flex-1 md:flex-initial px-4 py-2.5 rounded-xl text-xs font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 shadow-2xs transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <span>🛒</span>
          <span>Skapa inköpslista</span>
        </button>

        <button
          type="button"
          @click="openAddMeal(undefined, 'DINNER')"
          class="flex-1 md:flex-initial px-4 py-2.5 rounded-xl text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 shadow-xs transition-colors cursor-pointer flex items-center justify-center gap-2"
        >
          <span>➕</span>
          <span>Planera måltid</span>
        </button>
      </div>
    </div>

    <!-- Loading indicator -->
    <div v-if="mealPlanStore.loading && mealPlanStore.mealPlans.length === 0" class="py-12 text-center text-gray-400">
      <div class="inline-block animate-spin text-2xl mb-2">⏳</div>
      <p class="text-sm font-medium">Laddar veckomeny...</p>
    </div>

    <!-- Error notice -->
    <div v-else-if="mealPlanStore.error" class="p-4 bg-red-50 text-red-700 rounded-2xl border border-red-200 text-xs font-medium">
      {{ mealPlanStore.error }}
    </div>

    <!-- 7-Day Weekly Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-7 gap-3">
      <div
        v-for="day in mealPlanStore.weekDays"
        :key="day.date"
        class="bg-white rounded-2xl border transition-all flex flex-col min-h-[300px]"
        :class="
          day.isToday
            ? 'border-emerald-400 ring-2 ring-emerald-500/20 shadow-xs'
            : 'border-gray-200 shadow-2xs hover:border-gray-300'
        "
      >
        <!-- Day Card Header -->
        <div
          class="px-3.5 py-2.5 border-b flex items-center justify-between rounded-t-2xl"
          :class="day.isToday ? 'bg-emerald-50/70 border-emerald-200' : 'bg-gray-50/70 border-gray-100'"
        >
          <div>
            <span
              class="text-xs font-extrabold uppercase tracking-wider"
              :class="day.isToday ? 'text-emerald-800' : 'text-gray-700'"
            >
              {{ day.dayName }}
            </span>
            <span class="text-[11px] text-gray-500 block font-medium">
              {{ day.formattedDate }}
            </span>
          </div>
          <span
            v-if="day.isToday"
            class="text-[10px] font-bold bg-emerald-600 text-white px-1.5 py-0.5 rounded-full"
          >
            IDAG
          </span>
        </div>

        <!-- Meals Container for Day -->
        <div class="p-2.5 flex-1 flex flex-col gap-3">
          <!-- Lunch Slot -->
          <div class="space-y-1.5">
            <div class="flex items-center justify-between text-[11px] font-bold text-gray-500 px-1">
              <span class="flex items-center gap-1">
                <span>{{ MEAL_TYPE_LABELS.LUNCH.icon }}</span>
                <span>{{ MEAL_TYPE_LABELS.LUNCH.label }}</span>
              </span>
              <button
                type="button"
                @click="openAddMeal(day, 'LUNCH')"
                class="text-gray-400 hover:text-emerald-600 transition-colors p-0.5 rounded cursor-pointer"
                title="Lägg till lunch"
              >
                ➕
              </button>
            </div>

            <!-- Lunch Items -->
            <div
              v-for="item in mealPlanStore.getItemsForDayAndMeal(day.date, 'LUNCH')"
              :key="item.id"
              @click="openEditMeal(item)"
              class="group p-2.5 rounded-xl border border-gray-200 bg-white hover:border-emerald-400 hover:shadow-2xs transition-all cursor-pointer space-y-1"
            >
              <div class="flex items-start justify-between gap-1">
                <span class="text-xs font-bold text-gray-900 group-hover:text-emerald-700 transition-colors leading-snug">
                  {{ item.recipeTitle || item.customTitle }}
                </span>
                <span class="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded-md font-medium shrink-0">
                  {{ item.servings }}p
                </span>
              </div>
              <p v-if="item.notes" class="text-[10px] text-gray-500 italic line-clamp-1">
                💬 {{ item.notes }}
              </p>
            </div>

            <!-- Empty Lunch State placeholder -->
            <button
              v-if="mealPlanStore.getItemsForDayAndMeal(day.date, 'LUNCH').length === 0"
              type="button"
              @click="openAddMeal(day, 'LUNCH')"
              class="w-full py-2 border border-dashed border-gray-200 rounded-xl text-[11px] font-medium text-gray-400 hover:text-emerald-600 hover:border-emerald-300 hover:bg-emerald-50/30 transition-all cursor-pointer flex items-center justify-center gap-1"
            >
              <span>+</span>
              <span>Lägg till</span>
            </button>
          </div>

          <!-- Divider -->
          <div class="border-t border-gray-100"></div>

          <!-- Dinner Slot -->
          <div class="space-y-1.5 flex-1">
            <div class="flex items-center justify-between text-[11px] font-bold text-gray-500 px-1">
              <span class="flex items-center gap-1">
                <span>{{ MEAL_TYPE_LABELS.DINNER.icon }}</span>
                <span>{{ MEAL_TYPE_LABELS.DINNER.label }}</span>
              </span>
              <button
                type="button"
                @click="openAddMeal(day, 'DINNER')"
                class="text-gray-400 hover:text-emerald-600 transition-colors p-0.5 rounded cursor-pointer"
                title="Lägg till middag"
              >
                ➕
              </button>
            </div>

            <!-- Dinner Items -->
            <div
              v-for="item in mealPlanStore.getItemsForDayAndMeal(day.date, 'DINNER')"
              :key="item.id"
              @click="openEditMeal(item)"
              class="group p-2.5 rounded-xl border border-gray-200 bg-white hover:border-emerald-400 hover:shadow-2xs transition-all cursor-pointer space-y-1"
            >
              <div class="flex items-start justify-between gap-1">
                <span class="text-xs font-bold text-gray-900 group-hover:text-emerald-700 transition-colors leading-snug">
                  {{ item.recipeTitle || item.customTitle }}
                </span>
                <span class="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded-md font-medium shrink-0">
                  {{ item.servings }}p
                </span>
              </div>
              <p v-if="item.notes" class="text-[10px] text-gray-500 italic line-clamp-1">
                💬 {{ item.notes }}
              </p>
            </div>

            <!-- Empty Dinner State placeholder -->
            <button
              v-if="mealPlanStore.getItemsForDayAndMeal(day.date, 'DINNER').length === 0"
              type="button"
              @click="openAddMeal(day, 'DINNER')"
              class="w-full py-2.5 border border-dashed border-gray-200 rounded-xl text-[11px] font-medium text-gray-400 hover:text-emerald-600 hover:border-emerald-300 hover:bg-emerald-50/30 transition-all cursor-pointer flex items-center justify-center gap-1"
            >
              <span>+</span>
              <span>Lägg till middag</span>
            </button>
          </div>

          <!-- Other Meals (Breakfast / Snack if any exist) -->
          <div
            v-if="
              mealPlanStore.getItemsForDayAndMeal(day.date, 'BREAKFAST').length > 0 ||
              mealPlanStore.getItemsForDayAndMeal(day.date, 'SNACK').length > 0
            "
            class="space-y-1 pt-1 border-t border-gray-100"
          >
            <div
              v-for="item in [
                ...mealPlanStore.getItemsForDayAndMeal(day.date, 'BREAKFAST'),
                ...mealPlanStore.getItemsForDayAndMeal(day.date, 'SNACK'),
              ]"
              :key="item.id"
              @click="openEditMeal(item)"
              class="p-1.5 rounded-lg border border-gray-100 bg-gray-50 text-[11px] font-medium text-gray-700 hover:border-gray-300 cursor-pointer flex items-center justify-between"
            >
              <span>{{ MEAL_TYPE_LABELS[item.mealType]?.icon }} {{ item.recipeTitle || item.customTitle }}</span>
              <span class="text-[10px] text-gray-400">{{ item.servings }}p</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Meal Modal (Add / Edit) -->
    <MealModal
      :is-open="isMealModalOpen"
      :initial-date="selectedDate"
      :initial-meal-type="selectedMealType"
      :meal-to-edit="editingMeal"
      :recipes="recipeStore.recipes"
      :submitting="isSubmitting"
      @close="isMealModalOpen = false"
      @save="handleSaveMeal"
      @delete="handleDeleteMeal"
    />

    <!-- Generate Shopping List Modal -->
    <GenerateShoppingListModal
      v-if="isShoppingModalOpen"
      :is-open="isShoppingModalOpen"
      :week-label="mealPlanStore.weekLabel"
      :fetch-items="() => mealPlanStore.calculateShoppingList()"
      @close="isShoppingModalOpen = false"
      @success="handleShoppingSuccess"
    />
  </div>
</template>
