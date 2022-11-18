import request from '@/config/axios'

// 导出Html
export const exportHtmlApi = () => {
  return request.download({ url: '/infra/db-doc/export-html' })
}

// 导出Word
export const exportWordApi = () => {
  return request.download({ url: '/infra/db-doc/export-word' })
}

// 导出Markdown
export const exportMarkdownApi = () => {
  return request.download({ url: '/infra/db-doc/export-markdown' })
}
