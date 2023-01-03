import { CrudSchema } from '@/hooks/web/useCrudSchemas'
import type { VxeGridProps, VxeGridPropTypes } from 'vxe-table'

export type XTableProps<D = any> = VxeGridProps<D> & {
  allSchemas?: CrudSchema
  getListApi?: Function
  deleteApi?: Function
  exportListApi?: Function
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
