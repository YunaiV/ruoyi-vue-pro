export type DeptVO = {
  id: number
  name: string
  parentId: number
  status: number
  sort: number
  leaderUserId: number
  phone: string
  email: string
}

export type DeptListReqVO = {
  name: string
  status: number
}
