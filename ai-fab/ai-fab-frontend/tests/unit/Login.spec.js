import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'

describe('Login.vue', () => {
  it('renders login form', () => {
    const wrapper = mount(Login)
    expect(wrapper.find('h1').text()).toBe('AI能力封装平台')
  })

  it('has username and password inputs', () => {
    const wrapper = mount(Login)
    expect(wrapper.find('input[placeholder="用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="密码"]').exists()).toBe(true)
  })

  it('has login button', () => {
    const wrapper = mount(Login)
    expect(wrapper.find('button').text()).toContain('登 录')
  })
})
