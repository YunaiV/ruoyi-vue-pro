// 导出参数
import request from "@/utils/request";

export function exportHtml() {
  return request({
    url: '/infra/db-doc/export-html',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportWord() {
  return request({
    url: '/infra/db-doc/export-word',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportMarkdown() {
  return request({
    url: '/infra/db-doc/export-markdown',
    method: 'get',
    responseType: 'blob'
  })
}
