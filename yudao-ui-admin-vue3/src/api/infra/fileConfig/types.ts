export type ConfigType = {
  basePath: string
  host: string
  port: string
  username: string
  password: string
  mode: string
  endpoint: string
  bucket: string
  accessKey: string
  accessSecret: string
  domain: string
}
export type FileConfigVO = {
  id: number
  name: string
  storage: string
  primary: number
  visible: boolean
  config: ConfigType
  remark: string
  createTime: string
}
