import { CrudSchema } from '@/hooks/web/useCrudSchemas'
import type { VxeGridProps, VxeGridPropTypes, VxeTablePropTypes } from 'vxe-table'

export type XTableProps<D = any> = VxeGridProps<D> & {
  allSchemas?: CrudSchema
  height?: number // 高度 默认730
  topActionSlots?: boolean // 是否开启表格内顶部操作栏插槽
  treeConfig?: VxeTablePropTypes.TreeConfig // 树形表单配置
  isList?: boolean // 是否不带分页的list
  getListApi?: Function
  getAllListApi?: Function
  deleteApi?: Function
  exportListApi?: Function
  exportName?: string // 导出文件夹名称
  params?: any
  pagination?: boolean | VxeGridPropTypes.PagerConfig
  toolBar?: boolean | VxeGridPropTypes.ToolbarConfig
  afterFetch?: Function
}
export type XColumns = VxeGridPropTypes.Columns

export type VxeTableColumn = {
  field: string
  title?: string
  children?: VxeTableColumn[]
} & Recordable
