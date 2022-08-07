declare type TableColumn = {
  field: string
  label?: string
  children?: TableColumn[]
} & Recordable

declare type TableSlotDefault = {
  row: Recordable
  column: TableColumn
  $index: number
} & Recordable

declare interface Pagination {
  small?: boolean
  background?: boolean
  pageSize?: number
  defaultPageSize?: number
  total?: number
  pageCount?: number
  pagerCount?: number
  currentPage?: number
  defaultCurrentPage?: number
  layout?: string
  pageSizes?: number[]
  popperClass?: string
  prevText?: string
  nextText?: string
  disabled?: boolean
  hideOnSinglePage?: boolean
}

declare interface TableSetPropsType {
  field: string
  path: string
  value: any
}
