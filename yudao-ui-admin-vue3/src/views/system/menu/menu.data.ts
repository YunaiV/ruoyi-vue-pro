import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
// 国际化
const { t } = useI18n()
// 修改
export const modelSchema = reactive<FormSchema[]>([
  {
    label: '上级菜单',
    field: 'parentId',
    component: 'Input',
    formItemProps: {
      rules: [required]
    }
  },
  {
    label: '菜单类型',
    field: 'type',
    component: 'RadioButton',
    formItemProps: {
      rules: [required]
    },
    componentProps: {
      options: [
        {
          label: '目录',
          value: 1
        },
        {
          label: '菜单',
          value: 2
        },
        {
          label: '按钮',
          value: 3
        }
      ]
    }
  },
  {
    label: '菜单图标',
    field: 'icon',
    component: 'Input',
    formItemProps: {
      rules: [required]
    }
  },
  {
    label: '菜单名称',
    field: 'name',
    component: 'Input',
    formItemProps: {
      rules: [required]
    }
  },
  {
    label: '显示排序',
    field: 'sort',
    component: 'Input'
  },
  {
    label: '路由地址',
    field: 'path',
    component: 'Input',
    labelMessage: '访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头'
  },
  {
    label: '组件路径',
    field: 'component',
    component: 'Input'
  },
  {
    label: '权限标识',
    field: 'permission',
    component: 'Input',
    labelMessage:
      'Controller 方法上的权限字符，如：@PreAuthorize(`@ss.hasPermission(`system:user:list`)`)'
  },
  {
    label: t('common.status'),
    field: 'status',
    component: 'RadioButton',
    value: 0,
    formItemProps: {
      rules: [required]
    },
    componentProps: {
      options: [
        {
          label: '开启',
          value: 0
        },
        {
          label: '关闭',
          value: 1
        }
      ]
    },
    labelMessage: '选择停用时，路由将不会出现在侧边栏，也不能被访问'
  },
  {
    label: '是否显示',
    field: 'visible',
    component: 'RadioButton',
    value: 0,
    formItemProps: {
      rules: [required]
    },
    componentProps: {
      options: [
        {
          label: '显示',
          value: 0
        },
        {
          label: '隐藏',
          value: 1
        }
      ]
    },
    labelMessage: '选择隐藏时，路由将不会出现在侧边栏，但仍然可以访问'
  },
  {
    label: '是否缓存',
    field: 'keepAlive',
    component: 'RadioButton',
    value: 0,
    componentProps: {
      options: [
        {
          label: '缓存',
          value: 0
        },
        {
          label: '不缓存',
          value: 1
        }
      ]
    },
    labelMessage: '选择缓存时，则会被 `keep-alive` 缓存，需要匹配组件的 `name` 和路由地址保持一致'
  }
])
// 列表
export const columns = reactive<TableColumn[]>([
  {
    label: '菜单名称',
    field: 'name'
  },
  {
    label: '权限标识',
    field: 'permission',
    component: 'Input',
    formItemProps: {
      rules: [required]
    }
  },
  {
    label: '排序',
    field: 'sort'
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '180px'
  }
])
