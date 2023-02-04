import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

const { t } = useI18n() // 国际化

// crudSchemas
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '任务编号',
      field: 'id',
      table: {
        width: 320
      }
    },
    {
      title: '任务名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '所属流程',
      field: 'processInstance.name'
    },
    {
      title: '流程发起人',
      field: 'processInstance.startUserNickname'
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      table: {
        width: 180
      },
      isSearch: true,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '任务状态',
      field: 'suspensionState',
      table: {
        slots: {
          default: 'suspensionState_default'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
