import { DescriptionsSchema } from '@/types/descriptions'
import { getIntDictOptions } from '@/utils/dict'
import { reactive } from 'vue'
import {
  FormItemRenderOptions,
  VxeColumnPropTypes,
  VxeFormItemProps,
  VxeGridPropTypes,
  VxeTableDefines
} from 'vxe-table'
import { eachTree } from 'xe-utils'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeTableColumn } from '@/types/table'
import { FormSchema } from '@/types/form'
import { ComponentOptions } from '@/types/components'

export type VxeCrudSchema = {
  // 主键ID
  primaryKey?: string
  primaryType?: VxeColumnPropTypes.Type
  // 是否开启操作栏插槽
  action?: boolean
  columns: VxeCrudColumns[]
}
type VxeCrudColumns = Omit<VxeTableColumn, 'children'> & {
  field: string
  title?: string
  formatter?: VxeColumnPropTypes.Formatter
  isSearch?: boolean
  search?: VxeFormItemProps
  isTable?: boolean
  table?: VxeTableDefines.ColumnOptions
  isForm?: boolean
  form?: FormSchema
  isDetail?: boolean
  detail?: DescriptionsSchema
  print?: CrudPrintParams
  children?: VxeCrudColumns[]
  dictType?: string
}

type CrudPrintParams = {
  // 是否显示表单项
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
  const searchSchema: VxeFormItemProps[] = []
  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isSearch) {
      let itemRenderName = schemaItem?.search?.itemRender?.name || '$input'
      const options: any[] = []
      let itemRender: FormItemRenderOptions = {
        name: itemRenderName,
        props: { placeholder: t('common.inputText') }
      }
      if (schemaItem.dictType) {
        const allOptions = { label: '全部', value: '' }
        options.push(allOptions)
        getIntDictOptions(schemaItem.dictType).forEach((dict) => {
          options.push(dict)
        })
        itemRender.options = options
        if (!schemaItem?.search?.itemRender?.name) itemRenderName = '$select'
        itemRender = {
          name: itemRenderName,
          options: options,
          props: { placeholder: t('common.selectText') }
        }
      }
      const searchSchemaItem = {
        // 默认为 input
        folding: searchSchema.length > 2,
        itemRender: schemaItem.itemRender ? schemaItem.itemRender : itemRender,
        field: schemaItem.field,
        title: schemaItem.search?.title || schemaItem.title,
        span: 8
      }

      searchSchema.push(searchSchemaItem)
    }
  })
  // 添加搜索按钮
  const buttons: VxeFormItemProps = {
    span: 24,
    align: 'center',
    collapseNode: searchSchema.length > 3,
    itemRender: {
      name: '$buttons',
      children: [
        { props: { type: 'submit', content: t('common.query'), status: 'primary' } },
        { props: { type: 'reset', content: t('common.reset') } }
      ]
    }
  }
  searchSchema.push(buttons)
  return searchSchema
}

// 过滤 table 结构
const filterTableSchema = (crudSchema: VxeCrudSchema): VxeGridPropTypes.Columns => {
  const { t } = useI18n()
  const tableSchema: VxeGridPropTypes.Columns = []
  // 主键ID
  if (crudSchema.primaryKey) {
    const tableSchemaItem = {
      title: t('common.index'),
      field: crudSchema.primaryKey,
      type: crudSchema.primaryType ? crudSchema.primaryType : 'seq',
      width: '50px'
    }
    tableSchema.push(tableSchemaItem)
  }
  eachTree(crudSchema.columns, (schemaItem: VxeCrudColumns) => {
    // 判断是否显示
    if (schemaItem?.isTable !== false) {
      const tableSchemaItem = {
        ...schemaItem.table,
        field: schemaItem.field,
        title: schemaItem.table?.title || schemaItem.title
      }
      if (schemaItem?.formatter) {
        tableSchemaItem.formatter = schemaItem.formatter
      }
      if (schemaItem?.dictType) {
        tableSchemaItem.cellRender = {
          name: 'XDict',
          content: schemaItem.dictType
        }
      }

      tableSchema.push(tableSchemaItem)
    }
  })
  // 操作栏插槽
  if (crudSchema.action && crudSchema.action == true) {
    const tableSchemaItem = {
      title: t('table.action'),
      field: 'actionbtns',
      width: '240px',
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
    if (schemaItem?.isForm !== false) {
      let component = schemaItem?.form?.component || 'Input'
      const options: ComponentOptions[] = []
      let comonentProps = {}
      if (schemaItem.dictType) {
        getIntDictOptions(schemaItem.dictType).forEach((dict) => {
          options.push(dict)
        })
        comonentProps = {
          options: options
        }
        if (!(schemaItem.form && schemaItem.form.component)) component = 'Select'
      }
      const formSchemaItem = {
        // 默认为 input
        component: component,
        componentProps: comonentProps,
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
    if (schemaItem?.isDetail !== false) {
      const descriptionsSchemaItem = {
        ...schemaItem.detail,
        field: schemaItem.field,
        label: schemaItem.detail?.label || schemaItem.title
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
