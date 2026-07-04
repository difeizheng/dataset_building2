<template>
  <div class="page-container">
    <div class="card">
      <h2>故障诊断</h2>
      <p class="desc">输入故障症状，AI智能分析可能原因和解决方案</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="故障症状">
          <el-input v-model="form.symptom" type="textarea" :rows="3" placeholder="请描述故障症状..." />
        </el-form-item>
        <el-form-item label="系统类型">
          <el-input v-model="form.systemType" placeholder="可选：如数据库、网络、应用等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="diagnose">开始诊断</el-button>
        </el-form-item>
      </el-form>
      <div v-if="result" class="result-box">
        <h4>诊断结果：</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="可能原因">
            <ul><li v-for="c in result.possibleCauses" :key="c">{{ c }}</li></ul>
          </el-descriptions-item>
          <el-descriptions-item label="解决方案">
            <ul><li v-for="s in result.solutions" :key="s">{{ s }}</li></ul>
          </el-descriptions-item>
          <el-descriptions-item label="置信度">{{ (result.confidence * 100).toFixed(1) }}%</el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { diagnose as diagnoseApi } from '@/api/python'

const form = reactive({ symptom: '', systemType: '' })
const loading = ref(false)
const result = ref(null)

async function diagnose() {
  if (!form.symptom) return
  loading.value = true
  try {
    const res = await diagnoseApi(form)
    result.value = res.data
  } catch (e) {
    result.value = { possibleCauses: ['诊断失败'], solutions: ['请稍后重试'], confidence: 0 }
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
  ul { margin: 0; padding-left: 20px; }
}
</style>
