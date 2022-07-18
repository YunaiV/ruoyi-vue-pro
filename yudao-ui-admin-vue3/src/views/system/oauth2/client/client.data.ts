import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n()
// 国际化

// 表单校验
export const rules = reactive({
  clientId: [required],
  secret: [required],
  name: [required],
  logo: [required],
  status: [required],
  accessTokenValiditySeconds: [required],
  refreshTokenValiditySeconds: [required],
  redirectUris: [required],
  authorizedGrantTypes: [required]
})

// CrudSchema
const crudSchemas = reactive<CrudSchema[]>([
  {
    label: '客户端编号',
    field: 'clientId',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '客户端密钥',
    field: 'secret'
  },
  {
    label: '应用名',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '应用图标',
    field: 'logo'
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '访问令牌的有效期',
    field: 'accessTokenValiditySeconds'
  },
  {
    label: '刷新令牌的有效期',
    field: 'refreshTokenValiditySeconds'
  },
  {
    label: '授权类型',
    field: 'authorizedGrantTypes',
    dictType: DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE
  },
  {
    label: '授权范围',
    field: 'scopes',
    table: {
      show: false
    }
  },
  {
    label: '自动授权范围',
    field: 'autoApproveScopes',
    table: {
      show: false
    }
  },
  {
    label: '可重定向的 URI 地址',
    field: 'redirectUris',
    table: {
      show: false
    }
  },
  {
    label: '权限',
    field: 'authorities',
    table: {
      show: false
    }
  },
  {
    label: '资源',
    field: 'resourceIds',
    table: {
      show: false
    }
  },
  {
    label: '附加信息',
    field: 'additionalInformation',
    table: {
      show: false
    },
    form: {
      component: 'Input',
      componentProps: {
        type: 'textarea',
        rows: 4
      },
      colProps: {
        span: 24
      }
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
