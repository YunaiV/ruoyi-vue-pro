export type task = {
  id: string
  name: string
}
export type ProcessInstanceVO = {
  id: number
  name: string
  processDefinitionId: string
  category: string
  result: number
  tasks: task[]
  fields: string[]
  status: number
  remark: string
  businessKey: string
  createTime: string
  endTime: string
}
