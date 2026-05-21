import { ElMessage } from 'element-plus'
import { fetchEventSource } from '@microsoft/fetch-event-source'

/** Throw on auth failure so the caller knows to redirect. */
class FatalAiError extends Error {}

function authHeaders() {
  const token = localStorage.getItem('student_token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function handleAuthFailure() {
  localStorage.removeItem('student_token')
  window.location.hash = '#/login'
  ElMessage.error('登录已过期，请重新登录')
}

async function getJson(url) {
  let resp
  try {
    resp = await fetch(url, { headers: { Accept: 'application/json', ...authHeaders() } })
  } catch {
    ElMessage.error('网络错误，AI 服务不可达')
    throw new Error('network')
  }
  if (resp.status === 401) {
    handleAuthFailure()
    throw new FatalAiError('auth')
  }
  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    ElMessage.error(`AI 服务错误 ${resp.status}: ${text || resp.statusText}`)
    throw new Error(`HTTP ${resp.status}`)
  }
  return resp.json()
}

async function postJson(url, body = null) {
  let resp
  try {
    resp = await fetch(url, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...authHeaders(),
      },
      body: body == null ? null : JSON.stringify(body),
    })
  } catch {
    ElMessage.error('网络错误，AI 服务不可达')
    throw new Error('network')
  }
  if (resp.status === 401) {
    handleAuthFailure()
    throw new FatalAiError('auth')
  }
  if (!resp.ok) {
    const text = await resp.text().catch(() => '')
    ElMessage.error(`AI 服务错误 ${resp.status}: ${text || resp.statusText}`)
    throw new Error(`HTTP ${resp.status}`)
  }
  return resp.json()
}

export function getConversations(query = {}) {
  const page = query.page || 1
  const pageSize = query.pageSize || 30
  return getJson(`/api/ai/conversations?page=${page}&page_size=${pageSize}`)
}

export function getMessages(conversationId, query = {}) {
  const limit = query.limit || 100
  const beforeId = query.beforeId != null ? `&before_id=${query.beforeId}` : ''
  return getJson(`/api/ai/conversations/${conversationId}/messages?limit=${limit}${beforeId}`)
}

export function cancelPendingAction(actionId, conversationId) {
  return postJson(`/api/ai/chat/actions/${actionId}/cancel?conversation_id=${conversationId}`)
}

/**
 * Stream a chat turn via SSE.
 *
 * Uses @microsoft/fetch-event-source because native fetch + ReadableStream is
 * known to buffer SSE in some browser/proxy combinations (Vite dev proxy
 * notably). fetch-event-source handles framing + chunk delivery reliably and
 * preserves POST + custom Authorization headers (which EventSource cannot do).
 *
 * Send either { message } (new user turn) or { confirmedActionId } (resume).
 * `onEvent({ type, ... })` receives each parsed SSE event:
 *   token | tool_call | tool_result | confirm_required | final | error
 *
 * Returns when the server closes the stream. Pass an AbortSignal to cancel.
 */
export async function chatStream({
  conversationId = null,
  message = null,
  confirmedActionId = null,
  onEvent,
  signal,
}) {
  const body = { conversation_id: conversationId }
  if (confirmedActionId != null) body.confirmed_action_id = confirmedActionId
  else body.message = message

  let fatal = null

  try {
    await fetchEventSource('/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        ...authHeaders(),
      },
      body: JSON.stringify(body),
      signal,
      // Keep the stream alive when the tab loses focus (default would close it).
      openWhenHidden: true,

      async onopen(resp) {
        if (resp.ok && resp.headers.get('content-type')?.includes('text/event-stream')) {
          return // good
        }
        if (resp.status === 401) {
          handleAuthFailure()
          throw new FatalAiError('auth')
        }
        if (resp.status === 429) {
          ElMessage.error('请求过于频繁，请稍后再试')
          throw new FatalAiError('rate-limit')
        }
        const text = await resp.text().catch(() => '')
        ElMessage.error(`AI 服务错误 ${resp.status}: ${text || resp.statusText}`)
        throw new FatalAiError(`HTTP ${resp.status}`)
      },

      onmessage(ev) {
        // Skip ping/comment frames (no data field).
        if (!ev.data) return
        try {
          onEvent(JSON.parse(ev.data))
        } catch {
          // ignore malformed frame
        }
      },

      onerror(err) {
        // Any throw here stops auto-retry. We never want retry for chat turns
        // — the user can resend manually. Surface error and rethrow.
        fatal = err
        throw err
      },
    })
  } catch (e) {
    if (e?.name === 'AbortError') return
    if (fatal || e instanceof FatalAiError) throw e
    // network drop / other transport error
    ElMessage.error('AI 流式响应中断，请重试')
    throw e
  }
}
