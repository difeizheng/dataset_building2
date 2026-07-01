<template>
  <div class="page-container">
    <div class="card">
      <h2>音视频理解</h2>
      <p class="desc">支持语音转写、音视频摘要、内容分析</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="媒体URL">
          <el-input v-model="form.mediaUrl" placeholder="请输入音视频URL" />
        </el-form-item>
        <el-form-item label="媒体类型">
          <el-radio-group v-model="form.mediaType">
            <el-radio label="audio">音频</el-radio>
            <el-radio label="video">视频</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="form.taskType">
            <el-option label="语音转写" value="transcribe" />
            <el-option label="内容摘要" value="summarize" />
            <el-option label="内容分析" value="analyze" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="process">开始处理</el-button>
        </el-form-item>
      </el-form>
      <div v-if="result" class="result-box">
        <h4>处理结果：</h4>
        <pre>{{ JSON.stringify(result, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { processMedia } from '@/api/python'

const form = reactive({ mediaUrl: '', mediaType: 'audio', taskType: 'transcribe', language: 'zh' })
const loading = ref(false)
const result = ref(null)

async function process() {
  if (!form.mediaUrl) return
  loading.value = true
  try {
    const res = await processMedia(form)
    result.value = res.data
  } catch (e) {
    result.value = { error: '处理失败' }
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
