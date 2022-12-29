import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  columns: [
    {
      title: '文件名',
      field: 'name'
    },
    {
      title: '文件路径',
      field: 'path',
      isSearch: true
    },
    {
      title: 'URL',
      field: 'url',
      table: {
        cellRender: {
          name: 'XImg'
        }
      }
    },
    {
      title: '文件大小',
      field: 'size',
      formatter: 'formatSize'
    },
    {
      title: '文件类型',
      field: 'type',
      isSearch: true
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
