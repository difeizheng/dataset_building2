<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messagesRef">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-message', msg.role]">
        <div class="message-avatar">
          <el-icon v-if="msg.role === 'user'"><User /></el-icon>
          <el-icon v-else><Monitor /></el-icon>
        </div>
        <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
      </div>
      <div v-if="loading" class="chat-message assistant">
        <div class="message-avatar"><el-icon><Monitor /></el-icon></div>
        <div class="message-content"><el-icon class="is-loading"><Loading /></el-icon> 思考中...</div>
      </div>
    </div>
    <div class="chat-input">
      <el-input v-model="inputText" type="textarea" :rows="3" placeholder="请输入您的问题..." @keydown.enter.exact.prevent="sendMessage" />
      <el-button type="primary" :loading="loading" @click="sendMessage" :disabled="!inputText.trim()">
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { chat } from '@/api/ai'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

// Whitelist of allowed HTML tags for markdown rendering
const ALLOWED_TAGS = ['p', 'br', 'strong', 'em', 'code', 'pre', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'blockquote', 'a', 'span']

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const messagesRef = ref()

function renderMarkdown(text) {
  const rawHtml = marked.parse(text || '')
  return DOMPurify.sanitize(rawHtml, { ALLOWED_TAGS })
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  loading.value = true
  try {
    const res = await chat({ question: text })
    messages.value.push({ role: 'assistant', content: res.data.answer })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用，请稍后重试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #fff;
  border-radius: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  &.user {
    flex-direction: row-reverse;

    .message-content {
      background: #409eff;
      color: #fff;
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  max-width: 70%;
  line-height: 1.6;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}
</style>
