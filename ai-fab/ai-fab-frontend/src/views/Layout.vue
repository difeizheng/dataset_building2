<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <h2>AI工作台</h2>
      </div>
      <el-menu :default-active="activeMenu" router class="aside-menu">
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="breadcrumb">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo.realName || userStore.userInfo.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/portal', title: 'AI工作台', icon: 'HomeFilled' },
  { path: '/chat', title: '智能问答', icon: 'ChatDotRound' },
  { path: '/knowledge', title: '知识库问答', icon: 'Collection' },
  { path: '/document', title: '文档智能处理', icon: 'Document' },
  { path: '/image', title: '图像理解', icon: 'Picture' },
  { path: '/media', title: '音视频理解', icon: 'VideoCamera' },
  { path: '/diagnosis', title: '故障诊断', icon: 'Warning' },
  { path: '/decision', title: '决策分析', icon: 'DataAnalysis' },
  { path: '/bid', title: '招投标辅助', icon: 'Tickets' }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => {
  const item = menuItems.find(m => m.path === route.path)
  return item ? item.title : ''
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #263445;

  h2 {
    color: #fff;
    font-size: 18px;
  }
}

.aside-menu {
  border-right: none;
  background: #304156;

  :deep(.el-menu-item) {
    color: #bfcbd9;

    &.is-active {
      background: #409eff;
      color: #fff;
    }

    &:hover {
      background: #263445;
    }
  }
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  .breadcrumb {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
}

.layout-main {
  background: #f5f7fa;
  overflow-y: auto;
}
</style>
