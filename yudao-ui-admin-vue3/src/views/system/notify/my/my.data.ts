import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryTitle: ' ',
  primaryType: 'checkbox',
  action: true,
  actionWidth: '200', // 3个按钮默认200，如有删减对应增减即可
  columns: [
    {
      title: '发送人名称',
      field: 'templateNickname',
      table: {
        width: 120
      }
    },
    {
      title: '发送时间',
      field: 'createTime',
      isForm: false,
      formatter: 'formatDate',
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      },
      table: {
        width: 180
      }
    },
    {
      title: '类型',
      field: 'templateType',
      dictType: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
      dictClass: 'number',
      table: {
        width: 80
      }
    },
    {
      title: '内容',
      field: 'templateContent'
    },
    {
      title: '是否已读',
      field: 'readStatus',
      dictType: DICT_TYPE.INFRA_BOOLEAN_STRING,
      dictClass: 'boolean',
      table: {
        width: 80
      },
      isSearch: true
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
