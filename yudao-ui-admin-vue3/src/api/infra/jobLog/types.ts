export type JobLogVO = {
  id: number
  jobId: number
  handlerName: string
  handlerParam: string
  cronExpression: string
  executeIndex: string
  beginTime: Date
  endTime: Date
  duration: string
  status: number
  createTime: string
}
