import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Chat from '@/views/Chat.vue'

describe('Chat.vue - XSS Protection', () => {
  it('sanitizes XSS payload in markdown content - no executable img tag', async () => {
    const wrapper = mount(Chat)

    // Simulate a malicious message with XSS payload
    const xssPayload = '<img src=x onerror=alert("XSS")>'

    // Access the renderMarkdown function through the component
    const renderMarkdown = wrapper.vm.renderMarkdown
    const result = renderMarkdown(xssPayload)

    // CRITICAL: The dangerous img tag should NOT be rendered as actual HTML
    // DOMPurify escapes it to &lt;img... which is safe text, not executable HTML
    // Check that there's no actual <img tag (case insensitive, with word boundary)
    expect(result).not.toMatch(/<img\b/i)

    // If the string contains "onerror", it should be in escaped form (&lt;...onerror...&gt;)
    // which is safe - the browser will display it as text, not execute it
    if (result.includes('onerror')) {
      // Verify it's escaped (safe) not raw HTML (dangerous)
      expect(result).toMatch(/&lt;.*onerror.*&gt;/)
    }
  })

  it('allows safe markdown tags', () => {
    const wrapper = mount(Chat)
    const renderMarkdown = wrapper.vm.renderMarkdown

    const safeMarkdown = '**bold** and *italic* and `code`'
    const result = renderMarkdown(safeMarkdown)

    // Should preserve safe tags
    expect(result).toContain('<strong>')
    expect(result).toContain('<em>')
    expect(result).toContain('<code>')
  })

  it('sanitizes script tags - no executable script', () => {
    const wrapper = mount(Chat)
    const renderMarkdown = wrapper.vm.renderMarkdown

    const scriptPayload = '<script>alert("XSS")</script>'
    const result = renderMarkdown(scriptPayload)

    // No actual <script> tag should be rendered
    expect(result).not.toMatch(/<script[\s>]/i)
  })

  it('sanitizes event handlers in allowed tags', () => {
    const wrapper = mount(Chat)
    const renderMarkdown = wrapper.vm.renderMarkdown

    const payload = '<p onclick="alert(1)">Click me</p>'
    const result = renderMarkdown(payload)

    // No onclick attribute should be present as executable HTML
    expect(result).not.toMatch(/onclick\s*=/i)
  })

  it('preserves safe HTML while blocking dangerous content', () => {
    const wrapper = mount(Chat)
    const renderMarkdown = wrapper.vm.renderMarkdown

    // Mixed safe and dangerous content
    const mixedContent = '**Safe bold** <script>bad()</script> *safe italic*'
    const result = renderMarkdown(mixedContent)

    // Safe content should be preserved
    expect(result).toContain('<strong>')
    expect(result).toContain('<em>')
    // Dangerous content should be stripped
    expect(result).not.toMatch(/<script[\s>]/i)
    expect(result).not.toContain('bad()')
  })
})
