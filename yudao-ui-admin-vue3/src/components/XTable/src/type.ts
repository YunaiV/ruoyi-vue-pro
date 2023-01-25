import { CrudSchema } from '@/hooks/web/useCrudSchemas'
import type { VxeGridProps, VxeGridPropTypes, VxeTablePropTypes } from 'vxe-table'

export type XTableProps<D = any> = VxeGridProps<D> & {
  allSchemas?: CrudSchema
  height?: number // 高度 默认730
  topActionSlots?: boolean // 是否开启表格内顶部操作栏插槽
  treeConfig?: VxeTablePropTypes.TreeConfig // 树形表单配置
  isList?: boolean // 是否不带分页的list
  getListApi?: Function // 获取列表接口
  getAllListApi?: Function // 获取全部数据接口 用于 vxe 导出
  deleteApi?: Function // 删除接口
  deleteListApi?: Function // 批量删除接口
  exportListApi?: Function // 导出接口
  exportName?: string // 导出文件夹名称
  params?: any // 其他查询参数
  pagination?: boolean | VxeGridPropTypes.PagerConfig // 分页配置参数
  toolBar?: boolean | VxeGridPropTypes.ToolbarConfig // 右侧工具栏配置参数
}
export type XColumns = VxeGridPropTypes.Columns

export type VxeTableColumn = {
  field: string
  title?: string
  children?: VxeTableColumn[]
} & Recordable
