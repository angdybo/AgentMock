import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue')
  },
  {
    path: '/interface/list',
    name: 'InterfaceList',
    component: () => import('@/views/interface/list.vue')
  },
  {
    path: '/my_interface/list',
    name: 'MyInterface',
    component: () => import('@/views/interface/my-list.vue')
  },
  {
    path: '/interface/create/:id',
    name: 'InterfaceCreate',
    component: () => import('@/views/interface/edit.vue')
  },
  {
    path: '/interface/update/:id',
    name: 'InterfaceUpdate',
    component: () => import('@/views/interface/edit.vue')
  },
  {
    path: '/space/edit',
    name: 'SpaceEdit',
    component: () => import('@/views/space/edit.vue')
  },
  {
    path: '/doc',
    name: 'Doc',
    component: () => import('@/views/doc/index.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
