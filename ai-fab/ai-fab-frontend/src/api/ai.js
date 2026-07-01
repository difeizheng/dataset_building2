import request from './request'

export function chat(data) {
  return request.post('/v1/ai/chat', data)
}
