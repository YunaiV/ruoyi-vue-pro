import { useAxios } from '@/hooks/web/useAxios'

const request = useAxios()

// 导出Html
export const exportHtmlApi = () => {
  return request.get({ url: '/infra/db-doc/export-html', responseType: 'blob' })
}

// 导出Word
export const exportWordApi = () => {
  return request.get({ url: '/infra/db-doc/export-word', responseType: 'blob' })
}

// 导出Markdown
export const exportMarkdownApi = () => {
  return request.get({ url: '/infra/db-doc/export-markdown', responseType: 'blob' })
}
