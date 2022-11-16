import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

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
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'clientId',
  primaryType: 'seq',
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
        type: 'html',
        formatter: 'formatImg'
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      isSearch: true
    },
    {
      title: '访问令牌的有效期',
      field: 'accessTokenValiditySeconds', // TODO 星语：展示时，要有 xxx 秒
      form: {
        component: 'InputNumber'
      }
    },
    {
      title: '刷新令牌的有效期',
      field: 'refreshTokenValiditySeconds', // TODO 星语：展示时，要有 xxx 秒
      form: {
        component: 'InputNumber'
      }
    },
    {
      title: '授权类型',
      field: 'authorizedGrantTypes', // TODO 星语：详情展示时，应该类似 table 也是多个 tag
      table: {
        width: 300,
        slots: {
          default: 'authorizedGrantTypes_default'
        }
      }
    },
    {
      title: '授权范围',
      field: 'scopes', // TODO @星语：带输入的 SELECT
      isTable: false // TODO 星语：详情展示时，应该类似 table 也是多个 tag
    },
    {
      title: '自动授权范围',
      field: 'autoApproveScopes', // TODO @星语：带输入的 SELECT
      isTable: false // TODO 星语：详情展示时，应该类似 table 也是多个 tag
    },
    {
      title: '可重定向的 URI 地址',
      field: 'redirectUris', // TODO @星语：带输入的 SELECT
      isTable: false // TODO 星语：详情展示时，应该类似 table 也是多个 tag
    },
    {
      title: '权限',
      field: 'authorities',
      isTable: false
    },
    {
      title: '资源',
      field: 'resourceIds',
      isTable: false
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
