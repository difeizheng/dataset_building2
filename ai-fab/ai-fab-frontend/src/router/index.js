import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/portal',
    children: [
      {
        path: 'portal',
        name: 'Portal',
        component: () => import('@/views/Portal.vue'),
        meta: { title: 'AI工作台', icon: 'HomeFilled' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: { title: '智能问答', icon: 'ChatDotRound' }
      },
      {
        path: 'knowledge',
        name: 'Knowledge',
        component: () => import('@/views/Knowledge.vue'),
        meta: { title: '知识库问答', icon: 'Collection' }
      },
      {
        path: 'document',
        name: 'Document',
        component: () => import('@/views/Document.vue'),
        meta: { title: '文档智能处理', icon: 'Document' }
      },
      {
        path: 'image',
        name: 'Image',
        component: () => import('@/views/Image.vue'),
        meta: { title: '图像理解', icon: 'Picture' }
      },
      {
        path: 'media',
        name: 'Media',
        component: () => import('@/views/Media.vue'),
        meta: { title: '音视频理解', icon: 'VideoCamera' }
      },
      {
        path: 'diagnosis',
        name: 'Diagnosis',
        component: () => import('@/views/Diagnosis.vue'),
        meta: { title: '故障诊断', icon: 'Warning' }
      },
      {
        path: 'decision',
        name: 'Decision',
        component: () => import('@/views/Decision.vue'),
        meta: { title: '决策分析', icon: 'DataAnalysis' }
      },
      {
        path: 'bid',
        name: 'Bid',
        component: () => import('@/views/Bid.vue'),
        meta: { title: '招投标辅助', icon: 'Tickets' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    next()
    return
  }

  if (!userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router
