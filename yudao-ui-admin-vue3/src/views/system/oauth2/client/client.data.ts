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
      field: 'logo'
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS, // TODO @星语：编辑时，status 展示为 0 或者 1 了
      isSearch: true
    },
    {
      title: '访问令牌的有效期',
      field: 'accessTokenValiditySeconds' // TODO @星语：数字输入框
    },
    {
      title: '刷新令牌的有效期',
      field: 'refreshTokenValiditySeconds' // TODO @星语：数字输入框
    },
    {
      title: '授权类型',
      field: 'authorizedGrantTypes',
      dictType: DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE,
      form: {
        component: 'Select' // TODO @星语：多选
      }
    },
    {
      title: '授权范围',
      field: 'scopes', // TODO @星语：带输入的 SELECT
      isTable: false
    },
    {
      title: '自动授权范围',
      field: 'autoApproveScopes', // TODO @星语：带输入的 SELECT
      isTable: false
    },
    {
      title: '可重定向的 URI 地址',
      field: 'redirectUris', // TODO @星语：带输入的 SELECT
      isTable: false
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
      formatter: 'formatDate', // TODO @星语：宽度，保证时间可以展示出来
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
