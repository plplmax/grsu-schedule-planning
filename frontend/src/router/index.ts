import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/timeslots',
      name: 'timeslots',
      component: () => import('../views/TimeslotsView.vue')
    },
    {
      path: '/subjects',
      name: 'subjects',
      component: () => import('../views/SubjectsView.vue')
    },
    {
      path: '/subjects/paired',
      name: 'paired-subjects',
      component: () => import('../views/PairedSubjectsView.vue')
    },
    {
      path: '/teachers',
      name: 'teachers',
      component: () => import('../views/TeachersView.vue')
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('../views/RoomsView.vue')
    },
    {
      path: '/groups',
      name: 'groups',
      component: () => import('../views/GroupsView.vue')
    },
    {
      path: '/timetable',
      name: 'timetable',
      component: () => import('../views/TimetableView.vue')
    },
    {
      path: '/divisions',
      name: 'divisions',
      component: () => import('../views/DivisionsView.vue')
    }
  ]
})

export default router
