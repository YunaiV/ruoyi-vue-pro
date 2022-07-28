import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required]
})

// CrudSchema
const crudSchemas = reactive<CrudSchema[]>([
  {
    label: t('common.index'),
    field: 'id',
    type: 'index',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '流程标识',
    field: 'key',
    search: {
      show: true
    }
  },
  {
    label: '流程名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '流程分类',
    field: 'category',
    dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
    search: {
      show: true
    }
  },
  {
    label: '表单信息',
    field: 'formId'
  },
  {
    label: '最新部署的流程定义',
    field: 'processDefinition',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '240px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
