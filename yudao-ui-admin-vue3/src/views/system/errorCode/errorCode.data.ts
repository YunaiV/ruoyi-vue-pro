import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化
// 表单校验
export const rules = reactive({
  applicationName: [required],
  code: [required],
  message: [required]
})
// 新增 + 修改
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  columns: [
    {
      title: '错误码类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_ERROR_CODE_TYPE,
      search: {
        show: true
      }
    },
    {
      title: '应用名',
      field: 'applicationName',
      search: {
        show: true
      }
    },
    {
      title: '错误码编码',
      field: 'code',
      search: {
        show: true
      }
    },
    {
      title: '错误码错误提示',
      field: 'message'
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      form: {
        show: false
      },
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
