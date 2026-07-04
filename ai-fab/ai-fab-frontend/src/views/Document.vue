<template>
  <div class="page-container">
    <div class="card">
      <h2>文档智能处理</h2>
      <p class="desc">支持文档提取、摘要生成、智能翻译</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="文档URL">
          <el-input v-model="form.fileUrl" placeholder="请输入文档URL" />
        </el-form-item>
        <el-form-item label="处理类型">
          <el-select v-model="form.processType">
            <el-option label="文本提取" value="extract" />
            <el-option label="文档摘要" value="summarize" />
            <el-option label="文档翻译" value="translate" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标语言" v-if="form.processType === 'translate'">
          <el-select v-model="form.language">
            <el-option label="中文" value="zh" />
            <el-option label="英文" value="en" />
            <el-option label="日文" value="ja" />
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
import { processDocument } from '@/api/python'

const form = reactive({ fileUrl: '', processType: 'extract', language: 'zh' })
const loading = ref(false)
const result = ref(null)

async function process() {
  if (!form.fileUrl) return
  loading.value = true
  try {
    const res = await processDocument(form)
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
