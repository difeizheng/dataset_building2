<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>AI能力封装平台</h1>
        <p>三峡集团人工智能统一工作台</p>
      </div>

      <!-- Step 1: Username and Password -->
      <el-form v-if="!mfaRequired" ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- Step 2: MFA Verification -->
      <el-form v-else ref="mfaFormRef" :model="mfaForm" :rules="mfaRules" @submit.prevent="handleMfaVerify">
        <el-alert type="info" :closable="false" style="margin-bottom: 20px">
          <p>请输入身份验证器应用中的6位验证码</p>
        </el-alert>
        <el-form-item prop="code">
          <el-input
            v-model="mfaForm.code"
            placeholder="6位验证码"
            prefix-icon="Key"
            size="large"
            maxlength="6"
            @input="mfaForm.code = mfaForm.code.replace(/\D/g, '')"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="mfaLoading" style="width: 100%" @click="handleMfaVerify">
            验 证
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="text" size="small" @click="resetToStep1">
            返回上一步
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { verifyMfa } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const mfaFormRef = ref()
const loading = ref(false)
const mfaLoading = ref(false)
const mfaRequired = ref(false)
const tempToken = ref('')

const form = reactive({
  username: '',
  password: ''
})

const mfaForm = reactive({
  code: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const mfaRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.login(form.username, form.password)
    if (res.success) {
      // Check if MFA is required
      if (res.data.mfaRequired) {
        mfaRequired.value = true
        tempToken.value = res.data.tempToken
        ElMessage.info('请输入双因子验证码')
      } else {
        ElMessage.success('登录成功')
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      }
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleMfaVerify() {
  const valid = await mfaFormRef.value.validate().catch(() => false)
  if (!valid) return

  mfaLoading.value = true
  try {
    const res = await verifyMfa(tempToken.value, mfaForm.code)
    if (res.success) {
      // Store the token and user info
      await userStore.setToken(res.data.token)
      await userStore.fetchUserInfo()
      ElMessage.success('验证成功')
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    mfaLoading.value = false
  }
}

function resetToStep1() {
  mfaRequired.value = false
  tempToken.value = ''
  mfaForm.code = ''
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}
</style>
