export type DeptVO = {
  id: number
  name: string
  status: number
  parentId: number
  createTime: string
}

export type DeptListReqVO = {
  name: string
  status: number
}
