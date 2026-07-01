<template>
  <div class="page-container">
    <div class="portal-header">
      <h1>欢迎使用AI能力封装平台</h1>
      <p>三峡集团人工智能统一工作台 — 集成智能问答、文档处理、图像理解等多种AI能力</p>
    </div>
    <el-row :gutter="20" class="portal-cards">
      <el-col :span="8" v-for="item in capabilities" :key="item.title">
        <div class="capability-card card" @click="$router.push(item.path)">
          <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const capabilities = [
  { title: '智能问答', desc: '基于大模型的智能对话', icon: 'ChatDotRound', color: '#409eff', path: '/chat' },
  { title: '知识库问答', desc: '基于企业知识库的精准问答', icon: 'Collection', color: '#67c23a', path: '/knowledge' },
  { title: '文档智能处理', desc: '文档提取、摘要、翻译', icon: 'Document', color: '#e6a23c', path: '/document' },
  { title: '图像理解', desc: 'OCR、分类、目标检测', icon: 'Picture', color: '#f56c6c', path: '/image' },
  { title: '音视频理解', desc: '语音转写、视频分析', icon: 'VideoCamera', color: '#909399', path: '/media' },
  { title: '故障诊断', desc: '智能故障分析与诊断', icon: 'Warning', color: '#e6a23c', path: '/diagnosis' },
  { title: '决策分析', desc: '多维度决策辅助', icon: 'DataAnalysis', color: '#409eff', path: '/decision' },
  { title: '招投标辅助', desc: '智能招投标分析', icon: 'Tickets', color: '#67c23a', path: '/bid' }
]
</script>

<style scoped lang="scss">
.portal-header {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    font-size: 28px;
    color: #303133;
    margin-bottom: 12px;
  }

  p {
    font-size: 16px;
    color: #909399;
  }
}

.portal-cards {
  max-width: 1000px;
  margin: 0 auto;
}

.capability-card {
  text-align: center;
  padding: 32px 20px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  h3 {
    margin: 16px 0 8px;
    font-size: 18px;
    color: #303133;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}
</style>
