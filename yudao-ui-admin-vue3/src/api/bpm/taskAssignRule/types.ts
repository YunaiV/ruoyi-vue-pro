export type TaskAssignVO = {
  id: number
  modelId: string
  processDefinitionId: string
  taskDefinitionKey: string
  taskDefinitionName: string
  options: string[]
  type: number
}
