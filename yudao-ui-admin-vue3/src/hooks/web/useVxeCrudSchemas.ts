import {
  FormItemRenderOptions,
  VxeColumnPropTypes,
  VxeFormItemProps,
  VxeGridPropTypes,
  VxeTableDefines
} from 'vxe-table'
import { eachTree } from 'xe-utils'

import { getBoolDictOptions, getDictOptions, getIntDictOptions } from '@/utils/dict'
import { FormSchema } from '@/types/form'
import { VxeTableColumn } from '@/types/table'
import { ComponentOptions } from '@/types/components'
import { DescriptionsSchema } from '@/types/descriptions'

export type VxeCrudSchema = {
  primaryKey?: string // 主键ID
  primaryTitle?: string // 主键标题 默认为序号
  primaryType?: VxeColumnPropTypes.Type | 'id' // 还支持 "id" | "seq" | "radio" | "checkbox" | "expand" | "html" | null
  firstColumn?: VxeColumnPropTypes.Type // 第一列显示类型
  action?: boolean // 是否开启表格内右侧操作栏插槽
  actionTitle?: string // 操作栏标题 默认为操作
  actionWidth?: string // 操作栏插槽宽度,一般2个字带图标 text 类型按钮 50-70
  columns: VxeCrudColumns[]
  searchSpan?: number
}
type VxeCrudColumns = Omit<VxeTableColumn, 'children'> & {
  field: string // 字段名
  title?: string // 标题名
  formatter?: VxeColumnPropTypes.Formatter // vxe formatter格式化
  isSearch?: boolean // 是否在查询显示
  search?: CrudSearchParams // 查询的详细配置
  isTable?: boolean // 是否在列表显示
  table?: CrudTableParams // 列表的详细配置
  isForm?: boolean // 是否在表单显示
  form?: CrudFormParams // 表单的详细配置
  isDetail?: boolean // 是否在详情显示
  detail?: CrudDescriptionsParams // 详情的详细配置
  print?: CrudPrintParams // vxe 打印的字段
  children?: VxeCrudColumns[] // 子级
  dictType?: string // 字典类型
  dictClass?: 'string' | 'number' | 'boolean' // 字典数据类型 string | number | boolean
}

type CrudSearchParams = {
  // 是否显示在查询项
  show?: boolean
} & Omit<VxeFormItemProps, 'field'>

type CrudTableParams = {
  // 是否显示表头
  show?: boolean
} & Omit<VxeTableDefines.ColumnOptions, 'field'>

type CrudFormParams = {
  // 是否显示表单项
  show?: boolean
} & Omit<FormSchema, 'field'>

type CrudDescriptionsParams = {
  // 是否显示表单项
  show?: boolean
} & Omit<DescriptionsSchema, 'field'>

type CrudPrintParams = {
  // 是否显示打印项
  show?: boolean
} & Omit<VxeTableDefines.ColumnInfo[], 'field'>

export type VxeAllSchemas = {
  searchSchema: VxeFormItemProps[]
  tableSchema: VxeGridPropTypes.Columns
  formSchema: FormSchema[]
  detailSchema: DescriptionsSchema[]
  printSchema: VxeTableDefines.ColumnInfo[]
}

// 过滤所有结构
export const useVxeCrudSchemas = (
  crudSchema: VxeCrudSchema
): {
  allSchemas: VxeAllSchemas
} => {
  // 所有结构数据
  const allSchemas = reactive<VxeAllSchemas>({
    searchSchema: [],
    tableSchema: [],
    formSchema: [],
    detailSchema: [],
    printSchema: []
  })

  const searchSchema = filterSearchSchema(crudSchema)
  allSchemas.searchSchema = searchSchema || []

  const tableSchema = filterTableSchema(crudSchema)
  allSchemas.tableSchema = tableSchema || []

  const formSchema = filterFormSchema(crudSchema)
  allSchemas.formSchema = formSchema

  const detailSchema = filterDescriptionsSchema(crudSchema)
  allSchemas.detailSchema = detailSchema

  const printSchema = filterPrintSchema(crudSchema)
  allSchemas.printSchema = printSchema

  return {
    allSchemas
  }
}

// 过滤 Search 结构
const filterSearchSchema = (crudSchema: VxeCrudSchema): VxeFormItemProps[] => {
  const { t } = useI18n()
  const span = crudSchema.searchSpan ? crudSchema.searchSpan : 6
  const spanLength = 24 / span
  const searchSchema: VxeFormItemProps[] = []
  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isSearch || schemaItem.search?.show) {
      let itemRenderName = schemaItem?.search?.itemRender?.name || '$input'
      const options: any[] = []
      let itemRender: FormItemRenderOptions = {}

      if (schemaItem.dictType) {
        const allOptions = { label: '全部', value: '' }
        options.push(allOptions)
        getDictOptions(schemaItem.dictType).forEach((dict) => {
          options.push(dict)
        })
        itemRender.options = options
        if (!schemaItem?.search?.itemRender?.name) itemRenderName = '$select'
        itemRender = {
          name: itemRenderName,
          options: options,
          props: { placeholder: t('common.selectText') }
        }
      } else {
        if (schemaItem.search?.itemRender) {
          itemRender = schemaItem.search.itemRender
        } else {
          itemRender = {
            name: itemRenderName,
            props:
              itemRenderName == '$input'
                ? { placeholder: t('common.inputText') }
                : { placeholder: t('common.selectText') }
          }
        }
      }
      const searchSchemaItem = {
        // 默认为 input
        folding: searchSchema.length > spanLength - 1,
        itemRender: schemaItem.itemRender ? schemaItem.itemRender : itemRender,
        field: schemaItem.field,
        title: schemaItem.search?.title || schemaItem.title,
        slots: schemaItem.search?.slots,
        span: span
      }
      searchSchema.push(searchSchemaItem)
    }
  })
  if (searchSchema.length > 0) {
    // 添加搜索按钮
    const buttons: VxeFormItemProps = {
      span: 24,
      align: 'right',
      collapseNode: searchSchema.length > spanLength,
      itemRender: {
        name: '$buttons',
        children: [
          { props: { type: 'submit', content: t('common.query'), status: 'primary' } },
          { props: { type: 'reset', content: t('common.reset') } }
        ]
      }
    }
    searchSchema.push(buttons)
  }
  return searchSchema
}

// 过滤 table 结构
const filterTableSchema = (crudSchema: VxeCrudSchema): VxeGridPropTypes.Columns => {
  const { t } = useI18n()
  const tableSchema: VxeGridPropTypes.Columns = []
  // 第一列
  if (crudSchema.firstColumn) {
    const tableSchemaItem = {
      type: crudSchema.firstColumn,
      width: '50px'
    }
    tableSchema.push(tableSchemaItem)
  }
  // 主键ID
  if (crudSchema.primaryKey && crudSchema.primaryType) {
    const primaryTitle = crudSchema.primaryTitle ? crudSchema.primaryTitle : t('common.index')
    const primaryWidth = primaryTitle.length * 30 + 'px'

    let tableSchemaItem: { [x: string]: any } = {
      title: primaryTitle,
      field: crudSchema.primaryKey,
      width: primaryWidth
    }
    if (crudSchema.primaryType != 'id') {
      tableSchemaItem = {
        ...tableSchemaItem,
        type: crudSchema.primaryType
      }
    }
    tableSchema.push(tableSchemaItem)
  }

  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isTable !== false && schemaItem?.table?.show !== false) {
      const tableSchemaItem = {
        ...schemaItem.table,
        field: schemaItem.field,
        title: schemaItem.table?.title || schemaItem.title,
        minWidth: '80px'
      }
      tableSchemaItem.showOverflow = 'tooltip'
      if (schemaItem?.formatter) {
        tableSchemaItem.formatter = schemaItem.formatter
        tableSchemaItem.width = tableSchemaItem.width ? tableSchemaItem.width : 160
      }
      if (schemaItem?.dictType) {
        tableSchemaItem.cellRender = {
          name: 'XDict',
          content: schemaItem.dictType
        }
        tableSchemaItem.width = tableSchemaItem.width ? tableSchemaItem.width : 160
      }

      tableSchema.push(tableSchemaItem)
    }
  })
  // 操作栏插槽
  if (crudSchema.action && crudSchema.action == true) {
    const tableSchemaItem = {
      title: crudSchema.actionTitle ? crudSchema.actionTitle : t('table.action'),
      field: 'actionbtns',
      fixed: 'right' as unknown as VxeColumnPropTypes.Fixed,
      width: crudSchema.actionWidth ? crudSchema.actionWidth : '200px',
      slots: {
        default: 'actionbtns_default'
      }
    }
    tableSchema.push(tableSchemaItem)
  }
  return tableSchema
}

// 过滤 form 结构
const filterFormSchema = (crudSchema: VxeCrudSchema): FormSchema[] => {
  const formSchema: FormSchema[] = []

  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isForm !== false && schemaItem?.form?.show !== false) {
      // 默认为 input
      let component = schemaItem?.form?.component || 'Input'
      let defaultValue: any = ''
      if (schemaItem.form?.value) {
        defaultValue = schemaItem.form?.value
      } else {
        if (component === 'InputNumber') {
          defaultValue = 0
        }
      }
      let comonentProps = {}
      if (schemaItem.dictType) {
        const options: ComponentOptions[] = []
        if (schemaItem.dictClass && schemaItem.dictClass === 'number') {
          getIntDictOptions(schemaItem.dictType).forEach((dict) => {
            options.push(dict)
          })
        } else if (schemaItem.dictClass && schemaItem.dictClass === 'boolean') {
          getBoolDictOptions(schemaItem.dictType).forEach((dict) => {
            options.push(dict)
          })
        } else {
          getDictOptions(schemaItem.dictType).forEach((dict) => {
            options.push(dict)
          })
        }
        comonentProps = {
          options: options
        }
        if (!(schemaItem.form && schemaItem.form.component)) component = 'Select'
      }
      const formSchemaItem = {
        component: component,
        componentProps: comonentProps,
        value: defaultValue,
        ...schemaItem.form,
        field: schemaItem.field,
        label: schemaItem.form?.label || schemaItem.title
      }

      formSchema.push(formSchemaItem)
    }
  })

  return formSchema
}

// 过滤 descriptions 结构
const filterDescriptionsSchema = (crudSchema: VxeCrudSchema): DescriptionsSchema[] => {
  const descriptionsSchema: DescriptionsSchema[] = []

  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isDetail !== false && schemaItem.detail?.show !== false) {
      const descriptionsSchemaItem = {
        ...schemaItem.detail,
        field: schemaItem.field,
        label: schemaItem.detail?.label || schemaItem.title
      }
      if (schemaItem.dictType) {
        descriptionsSchemaItem.dictType = schemaItem.dictType
      }
      if (schemaItem.detail?.dateFormat || schemaItem.formatter == 'formatDate') {
        // 优先使用 detail 下的配置，如果没有默认为 YYYY-MM-DD HH:mm:ss
        descriptionsSchemaItem.dateFormat = schemaItem?.detail?.dateFormat
          ? schemaItem?.detail?.dateFormat
          : 'YYYY-MM-DD HH:mm:ss'
      }

      descriptionsSchema.push(descriptionsSchemaItem)
    }
  })

  return descriptionsSchema
}

// 过滤 打印 结构
const filterPrintSchema = (crudSchema: VxeCrudSchema): any[] => {
  const printSchema: any[] = []

  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.print?.show !== false) {
      const printSchemaItem = {
        field: schemaItem.field
      }

      printSchema.push(printSchemaItem)
    }
  })

  return printSchema
}
