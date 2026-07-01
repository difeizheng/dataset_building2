<template>
  <div class="page-container">
    <div class="card">
      <h2>知识库问答</h2>
      <p class="desc">基于企业知识库的精准问答，支持上传文档作为知识源</p>
      <el-upload drag action="/api/v1/knowledge/upload" :headers="uploadHeaders" :on-success="onUploadSuccess">
        <el-icon :size="40"><Upload /></el-icon>
        <div>拖拽文件到此处或 <em>点击上传</em></div>
      </el-upload>
      <div class="qa-section">
        <el-input v-model="question" placeholder="请输入您的问题..." @keydown.enter="ask">
          <template #append>
            <el-button @click="ask" :loading="loading">提问</el-button>
          </template>
        </el-input>
        <div v-if="answer" class="answer-box">
          <h4>回答：</h4>
          <p>{{ answer }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { chat } from '@/api/ai'

const userStore = useUserStore()
const question = ref('')
const answer = ref('')
const loading = ref(false)

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

function onUploadSuccess(res) {
  console.log('Upload success:', res)
}

async function ask() {
  if (!question.value.trim()) return
  loading.value = true
  try {
    const res = await chat({ question: `[知识库] ${question.value}` })
    answer.value = res.data.answer
  } catch (e) {
    answer.value = '查询失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.desc {
  color: #909399;
  margin-bottom: 20px;
}

.qa-section {
  margin-top: 24px;
}

.answer-box {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;

  h4 {
    margin-bottom: 8px;
    color: #303133;
  }

  p {
    color: #606266;
    line-height: 1.6;
  }
}
</style>
