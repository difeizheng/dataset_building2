<template>
  <div class="page-container">
    <div class="card">
      <h2>图像理解</h2>
      <p class="desc">支持OCR文字识别、图像分类、目标检测、图像描述</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="图像URL">
          <el-input v-model="form.imageUrl" placeholder="请输入图像URL" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="form.taskType">
            <el-option label="OCR文字识别" value="ocr" />
            <el-option label="图像分类" value="classify" />
            <el-option label="目标检测" value="detect" />
            <el-option label="图像描述" value="describe" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="process">开始分析</el-button>
        </el-form-item>
      </el-form>
      <div v-if="result" class="result-box">
        <h4>分析结果：</h4>
        <pre>{{ JSON.stringify(result, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { understandImage } from '@/api/python'

const form = reactive({ imageUrl: '', taskType: 'ocr', confidenceThreshold: 0.5 })
const loading = ref(false)
const result = ref(null)

async function process() {
  if (!form.imageUrl) return
  loading.value = true
  try {
    const res = await understandImage(form)
    result.value = res.data
  } catch (e) {
    result.value = { error: '分析失败' }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.desc { color: #909399; margin-bottom: 20px; }
.result-box {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  pre { white-space: pre-wrap; word-break: break-all; }
}
</style>
