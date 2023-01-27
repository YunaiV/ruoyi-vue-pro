import { computed, nextTick, reactive } from 'vue'
import { SizeType, VxeGridProps, VxeTablePropTypes } from 'vxe-table'
import { useAppStore } from '@/store/modules/app'
import { VxeAllSchemas } from './useVxeCrudSchemas'

import download from '@/utils/download'

const { t } = useI18n()
const message = useMessage() // 消息弹窗

interface UseVxeGridConfig<T = any> {
  allSchemas: VxeAllSchemas
  height?: number // 高度 默认730
  topActionSlots?: boolean // 是否开启表格内顶部操作栏插槽
  treeConfig?: VxeTablePropTypes.TreeConfig // 树形表单配置
  isList?: boolean // 是否不带分页的list
  getListApi: (option: any) => Promise<T> // 获取列表接口
  getAllListApi?: (option: any) => Promise<T> // 获取全部数据接口 用于VXE导出
  deleteApi?: (option: any) => Promise<T> // 删除接口
  exportListApi?: (option: any) => Promise<T> // 导出接口
  exportName?: string // 导出文件夹名称
  queryParams?: any // 其他查询参数
}

const appStore = useAppStore()

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

export const useVxeGrid = <T = any>(config?: UseVxeGridConfig<T>) => {
  /**
   * grid options 初始化
   */
  const gridOptions = reactive<VxeGridProps<any>>({
    loading: true,
    size: currentSize as any,
    height: config?.height ? config.height : 730,
    rowConfig: {
      isCurrent: true, // 当鼠标点击行时，是否要高亮当前行
      isHover: true // 当鼠标移到行时，是否要高亮当前行
    },
    toolbarConfig: {
      slots:
        !config?.topActionSlots && config?.topActionSlots != false
          ? { buttons: 'toolbar_buttons' }
          : {}
    },
    printConfig: {
      columns: config?.allSchemas.printSchema
    },
    formConfig: {
      enabled: true,
      titleWidth: 100,
      titleAlign: 'right',
      items: config?.allSchemas.searchSchema
    },
    columns: config?.allSchemas.tableSchema,
    proxyConfig: {
      seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
      form: true, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
      props: { result: 'list', total: 'total' },
      ajax: {
        query: ({ page, form }) => {
          let queryParams: any = Object.assign({}, JSON.parse(JSON.stringify(form)))
          if (config?.queryParams) {
            queryParams = Object.assign(queryParams, config.queryParams)
          }
          if (!config?.treeConfig) {
            queryParams.pageSize = page.pageSize
            queryParams.pageNo = page.currentPage
          }
          gridOptions.loading = false
          return new Promise(async (resolve) => {
            resolve(await config?.getListApi(queryParams))
          })
        },
        delete: ({ body }) => {
          return new Promise(async (resolve) => {
            if (config?.deleteApi) {
              resolve(await config?.deleteApi(JSON.stringify(body)))
            } else {
              Promise.reject('未设置deleteApi')
            }
          })
        },
        queryAll: ({ form }) => {
          const queryParams = Object.assign({}, JSON.parse(JSON.stringify(form)))
          return new Promise(async (resolve) => {
            if (config?.getAllListApi) {
              resolve(await config?.getAllListApi(queryParams))
            } else {
              resolve(await config?.getListApi(queryParams))
            }
          })
        }
      }
    },
    exportConfig: {
      filename: config?.exportName,
      // 默认选中类型
      type: 'csv',
      // 自定义数据量列表
      modes: config?.getAllListApi ? ['current', 'all'] : ['current'],
      columns: config?.allSchemas.printSchema
    }
  })

  if (config?.treeConfig) {
    gridOptions.treeConfig = config.treeConfig
  } else if (config?.isList) {
    gridOptions.proxyConfig = {
      seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
      form: true, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
      props: { result: 'data' },
      ajax: {
        query: ({ form }) => {
          let queryParams: any = Object.assign({}, JSON.parse(JSON.stringify(form)))
          if (config?.queryParams) {
            queryParams = Object.assign(queryParams, config.queryParams)
          }
          gridOptions.loading = false
          return new Promise(async (resolve) => {
            resolve(await config?.getListApi(queryParams))
          })
        }
      }
    }
  } else {
    gridOptions.pagerConfig = {
      border: false, // 带边框
      background: true, // 带背景颜色
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
  }

  /**
   * 刷新列表
   * @param ref
   * @returns
   */
  const getList = async (ref) => {
    if (!ref) {
      console.error('未传入gridRef')
      return
    }
    await nextTick()
    ref.value.commitProxy('query')
  }

  // 获取查询参数
  const getSearchData = async (ref) => {
    if (!ref) {
      console.error('未传入gridRef')
      return
    }
    await nextTick()
    const queryParams = Object.assign(
      {},
      JSON.parse(JSON.stringify(ref.value.getProxyInfo()?.form))
    )
    return queryParams
  }

  /**
   * 删除
   * @param ref
   * @param ids rowid
   * @returns
   */
  const deleteData = async (ref, ids: string | number) => {
    if (!ref) {
      console.error('未传入gridRef')
      return
    }
    if (!config?.deleteApi) {
      console.error('未传入delListApi')
      return
    }
    await nextTick()
    return new Promise(async () => {
      message.delConfirm().then(async () => {
        await (config?.deleteApi && config?.deleteApi(ids))
        message.success(t('common.delSuccess'))
        // 刷新列表
        ref.value.commitProxy('query')
      })
    })
  }
  /**
   * 导出
   * @param ref
   * @param fileName 文件名，默认excel.xls
   * @returns
   */
  const exportList = async (ref, fileName?: string) => {
    if (!ref) {
      console.error('未传入gridRef')
      return
    }
    if (!config?.exportListApi) {
      console.error('未传入exportListApi')
      return
    }
    await nextTick()
    const queryParams = Object.assign(
      {},
      JSON.parse(JSON.stringify(ref.value?.getProxyInfo()?.form))
    )
    message.exportConfirm().then(async () => {
      const res = await (config?.exportListApi && config?.exportListApi(queryParams))
      download.excel(res as unknown as Blob, fileName ? fileName : 'excel.xls')
    })
  }
  /**
   * 表格最大/最小化
   * @param ref
   * @returns
   */
  const zoom = async (ref) => {
    if (!ref) {
      console.error('未传入gridRef')
      return
    }
    await nextTick()
    ref.value.zoom(!ref.value.isMaximized())
  }

  return {
    gridOptions,
    getList,
    getSearchData,
    deleteData,
    exportList,
    zoom
  }
}
