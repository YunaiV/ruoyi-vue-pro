<template>
  <VxeGrid v-bind="getProps" ref="xGrid" :class="`${prefixCls}`" class="xtable-scrollbar">
    <template #[item]="data" v-for="item in Object.keys($slots)" :key="item">
      <slot :name="item" v-bind="data || {}"></slot>
    </template>
  </VxeGrid>
</template>
<script lang="ts" setup name="XTable">
import { PropType } from 'vue'
import { SizeType, VxeGridInstance } from 'vxe-table'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'
import { XTableProps } from './type'
import { isBoolean, isFunction } from '@/utils/is'

import download from '@/utils/download'

const { t } = useI18n()
const message = useMessage() // 消息弹窗

const appStore = useAppStore()

const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('x-vxe-table')

const attrs = useAttrs()
const emit = defineEmits(['register'])

watch(
  () => appStore.getIsDark,
  () => {
    if (appStore.getIsDark == true) {
      import('./style/dark.scss')
    }
    if (appStore.getIsDark == false) {
      import('./style/light.scss')
    }
  },
  { immediate: true }
)

const currentSize = computed(() => {
  let resSize: SizeType = 'small'
  const appsize = appStore.getCurrentSize
  switch (appsize) {
    case 'large':
      resSize = 'medium'
      break
    case 'default':
      resSize = 'small'
      break
    case 'small':
      resSize = 'mini'
      break
  }
  return resSize
})

const props = defineProps({
  options: {
    type: Object as PropType<XTableProps>,
    default: () => {}
  }
})
const innerProps = ref<Partial<XTableProps>>()

const getProps = computed(() => {
  const options = innerProps.value || props.options
  options.size = currentSize as any
  options.height = 700
  getOptionInitConfig(options)
  getColumnsConfig(options)
  getProxyConfig(options)
  getPageConfig(options)
  getToolBarConfig(options)
  // console.log(options);
  return {
    ...options,
    ...attrs
  }
})

const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref

let proxyForm = false

const getOptionInitConfig = (options: XTableProps) => {
  options.size = currentSize as any
  options.rowConfig = {
    isCurrent: true, // 当鼠标点击行时，是否要高亮当前行
    isHover: true // 当鼠标移到行时，是否要高亮当前行
  }
}

// columns
const getColumnsConfig = (options: XTableProps) => {
  const { allSchemas } = options
  if (!allSchemas) return
  if (allSchemas.printSchema) {
    options.printConfig = {
      columns: allSchemas.printSchema
    }
  }
  if (allSchemas.formSchema) {
    proxyForm = true
    options.formConfig = {
      enabled: true,
      titleWidth: 100,
      titleAlign: 'right',
      items: allSchemas.searchSchema
    }
  }
  if (allSchemas.tableSchema) {
    options.columns = allSchemas.tableSchema
  }
}

// 动态请求
const getProxyConfig = (options: XTableProps) => {
  const { getListApi, proxyConfig, data, isList } = options
  if (proxyConfig || data) return
  if (getListApi && isFunction(getListApi)) {
    if (!isList) {
      options.proxyConfig = {
        seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
        form: proxyForm, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
        props: { result: 'list', total: 'total' },
        ajax: {
          query: async ({ page, form }) => {
            let queryParams: any = Object.assign({}, JSON.parse(JSON.stringify(form)))
            if (options.params) {
              queryParams = Object.assign(queryParams, options.params)
            }
            if (!options?.treeConfig) {
              queryParams.pageSize = page.pageSize
              queryParams.pageNo = page.currentPage
            }
            return new Promise(async (resolve) => {
              resolve(await getListApi(queryParams))
            })
          },
          delete: ({ body }) => {
            return new Promise(async (resolve) => {
              if (options.deleteApi) {
                resolve(await options.deleteApi(JSON.stringify(body)))
              } else {
                Promise.reject('未设置deleteApi')
              }
            })
          },
          queryAll: ({ form }) => {
            const queryParams = Object.assign({}, JSON.parse(JSON.stringify(form)))
            return new Promise(async (resolve) => {
              if (options.getAllListApi) {
                resolve(await options.getAllListApi(queryParams))
              } else {
                resolve(await getListApi(queryParams))
              }
            })
          }
        }
      }
    } else {
      options.proxyConfig = {
        seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
        form: true, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
        props: { result: 'data' },
        ajax: {
          query: ({ form }) => {
            let queryParams: any = Object.assign({}, JSON.parse(JSON.stringify(form)))
            if (options?.params) {
              queryParams = Object.assign(queryParams, options.params)
            }
            return new Promise(async (resolve) => {
              resolve(await getListApi(queryParams))
            })
          }
        }
      }
    }
  }
  if (options.exportListApi) {
    options.exportConfig = {
      filename: options?.exportName,
      // 默认选中类型
      type: 'csv',
      // 自定义数据量列表
      modes: options?.getAllListApi ? ['current', 'all'] : ['current'],
      columns: options?.allSchemas?.printSchema
    }
  }
}

// 分页
const getPageConfig = (options: XTableProps) => {
  const { pagination, pagerConfig, treeConfig, isList } = options
  if (isList) return
  if (treeConfig) {
    options.treeConfig = options.treeConfig
    return
  }
  if (pagerConfig) return
  if (pagination) {
    if (isBoolean(pagination)) {
      options.pagerConfig = {
        border: false, // 带边框
        background: false, // 带背景颜色
        perfect: false, // 配套的样式
        pageSize: 10, // 每页大小
        pagerCount: 7, // 显示页码按钮的数量
        autoHidden: false, // 当只有一页时自动隐藏
        pageSizes: [5, 10, 20, 30, 50, 100], // 每页大小选项列表
        layouts: [
          'PrevJump',
          'PrevPage',
          'JumpNumber',
          'NextPage',
          'NextJump',
          'Sizes',
          'FullJump',
          'Total'
        ]
      }
      return
    }
    options.pagerConfig = pagination
  } else {
    if (pagination != false) {
      options.pagerConfig = {
        border: false, // 带边框
        background: false, // 带背景颜色
        perfect: false, // 配套的样式
        pageSize: 10, // 每页大小
        pagerCount: 7, // 显示页码按钮的数量
        autoHidden: false, // 当只有一页时自动隐藏
        pageSizes: [5, 10, 20, 30, 50, 100], // 每页大小选项列表
        layouts: [
          'Sizes',
          'PrevJump',
          'PrevPage',
          'Number',
          'NextPage',
          'NextJump',
          'FullJump',
          'Total'
        ]
      }
    }
  }
}

// tool bar
const getToolBarConfig = (options: XTableProps) => {
  const { toolBar, toolbarConfig, topActionSlots } = options
  if (toolbarConfig) return
  if (toolBar) {
    if (!isBoolean(toolBar)) {
      console.info(2)
      options.toolbarConfig = toolBar
      return
    }
  } else if (topActionSlots != false) {
    options.toolbarConfig = {
      slots: { buttons: 'toolbar_buttons' }
    }
  } else {
    options.toolbarConfig = {
      enabled: true
    }
  }
}

// 刷新列表
const reload = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  g.commitProxy('query')
}

// 删除
const deleteData = async (id: string | number) => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  const options = innerProps.value || props.options
  if (!options.deleteApi) {
    console.error('未传入delListApi')
    return
  }
  return new Promise(async () => {
    message.delConfirm().then(async () => {
      await (options?.deleteApi && options?.deleteApi(id))
      message.success(t('common.delSuccess'))
      // 刷新列表
      reload()
    })
  })
}

// 批量删除
const deleteBatch = async () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  const rows = g.getCheckboxRecords() || g.getRadioRecord()
  let ids: any[] = []
  if (rows.length == 0) {
    message.error('请选择数据')
    return
  } else {
    rows.forEach((row) => {
      ids.push(row.id)
    })
  }
  const options = innerProps.value || props.options
  if (options.deleteListApi) {
    return new Promise(async () => {
      message.delConfirm().then(async () => {
        await (options?.deleteListApi && options?.deleteListApi(ids))
        message.success(t('common.delSuccess'))
        // 刷新列表
        reload()
      })
    })
  } else if (options.deleteApi) {
    return new Promise(async () => {
      message.delConfirm().then(async () => {
        ids.forEach(async (id) => {
          await (options?.deleteApi && options?.deleteApi(id))
        })
        message.success(t('common.delSuccess'))
        // 刷新列表
        reload()
      })
    })
  } else {
    console.error('未传入delListApi')
    return
  }
}

// 导出
const exportList = async (fileName?: string) => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  const options = innerProps.value || props.options
  if (!options?.exportListApi) {
    console.error('未传入exportListApi')
    return
  }
  const queryParams = Object.assign({}, JSON.parse(JSON.stringify(g.getProxyInfo()?.form)))
  message.exportConfirm().then(async () => {
    const res = await (options?.exportListApi && options?.exportListApi(queryParams))
    download.excel(res as unknown as Blob, fileName ? fileName : 'excel.xls')
  })
}

// 获取查询参数
const getSearchData = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  const queryParams = Object.assign({}, JSON.parse(JSON.stringify(g.getProxyInfo()?.form)))
  return queryParams
}

// 获取当前列
const getCurrentColumn = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  return g.getCurrentColumn()
}

// 获取当前选中列，redio
const getRadioRecord = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  return g.getRadioRecord(false)
}

// 获取当前选中列，checkbox
const getCheckboxRecords = () => {
  const g = unref(xGrid)
  if (!g) {
    return
  }
  return g.getCheckboxRecords(false)
}
const setProps = (prop: Partial<XTableProps>) => {
  innerProps.value = { ...unref(innerProps), ...prop }
}

defineExpose({ reload, Ref: xGrid, getSearchData, deleteData, exportList })
emit('register', {
  reload,
  getSearchData,
  setProps,
  deleteData,
  deleteBatch,
  exportList,
  getCurrentColumn,
  getRadioRecord,
  getCheckboxRecords
})
</script>
<style lang="scss">
@import './style/index.scss';
</style>
