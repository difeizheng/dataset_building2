<template>
  <div class="page-container">
    <div class="card">
      <h2>招投标智能辅助</h2>
      <p class="desc">智能分析招标文件，提供投标建议和风险评估</p>
      <el-form :model="form" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="form.projectName" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="招标文件">
          <el-input v-model="form.bidDocument" placeholder="可选：招标文件URL" />
        </el-form-item>
        <el-form-item label="需求列表">
          <div v-for="(req, idx) in form.requirements" :key="idx" class="req-row">
            <el-input v-model="form.requirements[idx]" :placeholder="`需求 ${idx + 1}`" />
            <el-button v-if="form.requirements.length > 1" type="danger" link @click="form.requirements.splice(idx, 1)">删除</el-button>
          </div>
          <el-button type="primary" link @click="form.requirements.push('')">+ 添加需求</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="assist">开始分析</el-button>
        </el-form-item>
      </el-form>
      <div v-if="result" class="result-box">
        <h4>分析结果：</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="建议">
            <ul><li v-for="s in result.suggestions" :key="s">{{ s }}</li></ul>
          </el-descriptions-item>
          <el-descriptions-item label="风险评估">
            <ul><li v-for="r in result.riskAssessment" :key="r">{{ r }}</li></ul>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { assistBid } from '@/api/python'

const form = reactive({ projectName: '', bidDocument: '', requirements: [''] })
const loading = ref(false)
const result = ref(null)

async function assist() {
  if (!form.projectName) return
  loading.value = true
  try {
    const res = await assistBid(form)
    result.value = res.data
  } catch (e) {
    result.value = { suggestions: ['分析失败'], riskAssessment: ['请稍后重试'] }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.desc { color: #909399; margin-bottom: 20px; }
.req-row { display: flex; gap: 8px; margin-bottom: 8px; }
.result-box {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  ul { margin: 0; padding-left: 20px; }
}
</style>
