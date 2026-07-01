<template>
  <div class="page-container">
    <div class="card">
      <h2>决策分析</h2>
      <p class="desc">输入决策背景和可选方案，AI辅助多维度分析</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="决策背景">
          <el-input v-model="form.context" type="textarea" :rows="3" placeholder="请描述决策背景..." />
        </el-form-item>
        <el-form-item label="可选方案">
          <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
            <el-input v-model="form.options[idx]" :placeholder="`方案 ${idx + 1}`" />
            <el-button v-if="form.options.length > 1" type="danger" link @click="form.options.splice(idx, 1)">删除</el-button>
          </div>
          <el-button type="primary" link @click="form.options.push('')">+ 添加方案</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="analyze">开始分析</el-button>
        </el-form-item>
      </el-form>
      <div v-if="result" class="result-box">
        <h4>分析结果：</h4>
        <el-alert :title="`推荐方案：${result.recommendation}`" type="success" show-icon />
        <div style="margin-top: 12px">
          <pre>{{ JSON.stringify(result.analysis, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { analyzeDecision } from '@/api/python'

const form = reactive({ context: '', options: ['', ''] })
const loading = ref(false)
const result = ref(null)

async function analyze() {
  if (!form.context || form.options.filter(o => o).length < 2) return
  loading.value = true
  try {
    const res = await analyzeDecision(form)
    result.value = res.data
  } catch (e) {
    result.value = { recommendation: '分析失败', analysis: {} }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.desc { color: #909399; margin-bottom: 20px; }
.option-row { display: flex; gap: 8px; margin-bottom: 8px; }
.result-box {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  pre { white-space: pre-wrap; word-break: break-all; }
}
</style>
