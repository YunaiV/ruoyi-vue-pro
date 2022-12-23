import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

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
    label: '流程名',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '流程分类',
    field: 'category',
    dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
    dictClass: 'number',
    search: {
      show: true
    }
  },
  {
    label: '当前审批任务',
    field: 'tasks'
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
    dictClass: 'number',
    search: {
      show: true
    }
  },
  {
    label: '结果',
    field: 'result',
    dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT,
    dictClass: 'number',
    search: {
      show: true
    }
  },
  {
    label: '提交时间',
    field: 'createTime',
    form: {
      show: false
    },
    search: {
      show: true
    }
  },
  {
    label: '结束时间',
    field: 'endTime',
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
