import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

const authorizedGrantOptions = getStrDictOptions(DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE)

// 表单校验
export const rules = reactive({
  clientId: [required],
  secret: [required],
  name: [required],
  status: [required],
  accessTokenValiditySeconds: [required],
  refreshTokenValiditySeconds: [required],
  redirectUris: [required],
  authorizedGrantTypes: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'clientId',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '客户端密钥',
      field: 'secret'
    },
    {
      title: '应用名',
      field: 'name',
      isSearch: true
    },
    {
      title: '应用图标',
      field: 'logo',
      table: {
        cellRender: {
          name: 'XImg'
        }
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '访问令牌的有效期',
      field: 'accessTokenValiditySeconds',
      form: {
        component: 'InputNumber'
      },
      table: {
        slots: {
          default: 'accessTokenValiditySeconds_default'
        }
      }
    },
    {
      title: '刷新令牌的有效期',
      field: 'refreshTokenValiditySeconds',
      form: {
        component: 'InputNumber'
      },
      table: {
        slots: {
          default: 'refreshTokenValiditySeconds_default'
        }
      }
    },
    {
      title: '授权类型',
      field: 'authorizedGrantTypes',
      table: {
        width: 400,
        slots: {
          default: 'authorizedGrantTypes_default'
        }
      },
      form: {
        component: 'Select',
        componentProps: {
          options: authorizedGrantOptions,
          multiple: true,
          filterable: true
        }
      }
    },
    {
      title: '授权范围',
      field: 'scopes',
      isTable: false,
      form: {
        component: 'Select',
        componentProps: {
          options: [],
          multiple: true,
          filterable: true,
          allowCreate: true,
          defaultFirstOption: true
        }
      }
    },
    {
      title: '自动授权范围',
      field: 'autoApproveScopes',
      isTable: false,
      form: {
        component: 'Select',
        componentProps: {
          options: [],
          multiple: true,
          filterable: true,
          allowCreate: true,
          defaultFirstOption: true
        }
      }
    },
    {
      title: '可重定向的 URI 地址',
      field: 'redirectUris',
      isTable: false,
      form: {
        component: 'Select',
        componentProps: {
          options: [],
          multiple: true,
          filterable: true,
          allowCreate: true,
          defaultFirstOption: true
        }
      }
    },
    {
      title: '权限',
      field: 'authorities',
      isTable: false,
      form: {
        component: 'Select',
        componentProps: {
          options: [],
          multiple: true,
          filterable: true,
          allowCreate: true,
          defaultFirstOption: true
        }
      }
    },
    {
      title: '资源',
      field: 'resourceIds',
      isTable: false,
      form: {
        component: 'Select',
        componentProps: {
          options: [],
          multiple: true,
          filterable: true,
          allowCreate: true,
          defaultFirstOption: true
        }
      }
    },
    {
      title: '附加信息',
      field: 'additionalInformation',
      isTable: false,
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
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
