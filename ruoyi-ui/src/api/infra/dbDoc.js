// 导出参数
import request from "@/utils/request";

export function exportHtml() {
  return request({
    url: '/infra/db-doc/export-html',
    method: 'get',
    responseType: 'blob'
  })
}
