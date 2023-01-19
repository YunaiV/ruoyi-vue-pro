import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const dictDataRules = reactive({
  label: [required],
  value: [required],
  sort: [required]
})
// crudSchemas
export const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  actionWidth: '140px',
  searchSpan: 12,
  columns: [
    {
      title: '字典类型',
      field: 'dictType',
      isTable: false,
      isForm: false
    },
    {
      title: '数据标签',
      field: 'label',
      isSearch: true
    },
    {
      title: '数据键值',
      field: 'value'
    },
    // {
    //   title: '标签类型',
    //   field: 'colorType',
    //   form: {
    //     component: 'Select',
    //     componentProps: {
    //       options: [
    //         {
    //           label: 'default',
    //           value: ''
    //         },
    //         {
    //           label: 'success',
    //           value: 'success'
    //         },
    //         {
    //           label: 'info',
    //           value: 'info'
    //         },
    //         {
    //           label: 'warning',
    //           value: 'warning'
    //         },
    //         {
    //           label: 'danger',
    //           value: 'danger'
    //         }
    //       ]
    //     }
    //   },
    //   isTable: false
    // },
    {
      title: '颜色',
      field: 'cssClass',
      isTable: false,
      form: {
        component: 'ColorPicker',
        componentProps: {
          predefine: ['#ffffff', '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c71585']
        }
      }
    },
    {
      title: '显示排序',
      field: 'sort',
      isTable: false
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number'
    },
    {
      title: t('form.remark'),
      field: 'remark',
      form: {
        component: 'Input',
        componentProps: {
          type: 'textarea',
          rows: 4
        },
        colProps: {
          span: 24
        }
      },
      isTable: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
