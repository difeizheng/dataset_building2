import request from './request'

const BASE = '/python-api'

export function processDocument(data) {
  return request.post(`${BASE}/document/process`, data)
}

export function understandImage(data) {
  return request.post(`${BASE}/image/understand`, data)
}

export function processMedia(data) {
  return request.post(`${BASE}/media/process`, data)
}

export function diagnose(data) {
  return request.post(`${BASE}/diagnosis/analyze`, data)
}

export function analyzeDecision(data) {
  return request.post(`${BASE}/decision/analyze`, data)
}

export function assistBid(data) {
  return request.post(`${BASE}/bid/assist`, data)
}
