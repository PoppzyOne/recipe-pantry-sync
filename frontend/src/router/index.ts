import { createRouter, createWebHistory } from 'vue-router'
import RecipeListView from '@/components/recipe/RecipeListView.vue'
import MealPlannerView from '@/components/mealplan/MealPlannerView.vue'
import PantryView from '@/components/pantry/PantryView.vue'
import ShoppingListView from '@/components/shopping/ShoppingListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/recipes',
    },
    {
      path: '/recipes',
      name: 'recipes',
      component: RecipeListView,
    },
    {
      path: '/planner',
      name: 'planner',
      component: MealPlannerView,
    },
    {
      path: '/pantry',
      name: 'pantry',
      component: PantryView,
    },
    {
      path: '/shopping',
      name: 'shopping',
      component: ShoppingListView,
    },
  ],
})

export default router
