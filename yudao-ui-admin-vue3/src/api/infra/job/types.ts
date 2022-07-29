export type JobVO = {
  id: number
  name: string
  status: number
  handlerName: string
  handlerParam: string
  cronExpression: string
  retryCount: number
  retryInterval: number
  monitorTimeout: number
}
