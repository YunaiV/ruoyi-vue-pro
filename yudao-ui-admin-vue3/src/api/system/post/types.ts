export type PostVO = {
  id?: number
  name: string
  code: string
  sort: number
  status: number
  remark: string
  createTime?: string
}

// TODO @星语：要不要搞个 Page 基类呀？和后端对应
export type PostPageReqVO = {
  code: string
  name: string
  status?: number
  pageSize?: number
  pageNo?: number
}

export type PostExportReqVO = {
  code: string
  name: string
  status?: number
}
