export type PostVO = {
  id: number
  name: string
  code: string
  sort: number
  status: number
  remark: string
  createTime: string
}

export type PostPageReqVO = {
  code: string
  name: string
  status: number
}

export type PostExportReqVO = {
  code: string
  name: string
  status: number
}
