export interface DescriptionsSchema {
  span?: number // 占多少分
  field: string // 字段名
  label?: string // label名
  width?: string | number
  minWidth?: string | number
  align?: 'left' | 'center' | 'right'
  labelAlign?: 'left' | 'center' | 'right'
  className?: string
  labelClassName?: string
  dateFormat?: string
  dictType?: string
}
