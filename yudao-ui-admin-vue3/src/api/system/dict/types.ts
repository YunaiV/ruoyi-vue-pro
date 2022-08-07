export type DictTypeVO = {
  id: number
  name: string
  type: string
  status: number
  remark: string
  createTime: string
}

export type DictTypePageReqVO = {
  name: string
  type: string
  status: number
  createTime: []
}

export type DictTypeExportReqVO = {
  name: string
  type: string
  status: number
  createTime: []
}

export type DictDataVO = {
  id: number
  sort: number
  label: string
  value: string
  dictType: string
  status: number
  colorType: string
  cssClass: string
  remark: string
  createTime: string
}
export type DictDataPageReqVO = {
  label: string
  dictType: string
  status: number
}

export type DictDataExportReqVO = {
  label: string
  dictType: string
  status: number
}
