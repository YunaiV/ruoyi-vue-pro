import download from '@/utils/download'
import { Table, TableExpose } from '@/components/Table'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { computed, nextTick, reactive, ref, unref, watch } from 'vue'
import type { TableProps } from '@/components/Table/src/types'
import { useI18n } from '@/hooks/web/useI18n'
import { TableSetPropsType } from '@/types/table'

const { t } = useI18n()
interface ResponseType<T = any> {
  list: T[]
  total?: number
}

interface UseTableConfig<T = any> {
  getListApi: (option: any) => Promise<T>
  delListApi?: (option: any) => Promise<T>
  exportListApi?: (option: any) => Promise<T>
  // 返回数据格式配置
  response?: ResponseType
  // 默认传递的参数
  defaultParams?: Recordable
  props?: TableProps
}

interface TableObject<T = any> {
  pageSize: number
  currentPage: number
  total: number
  tableList: T[]
  params: any
  loading: boolean
  exportLoading: boolean
  currentRow: Nullable<T>
}

export const useTable = <T = any>(config?: UseTableConfig<T>) => {
  const tableObject = reactive<TableObject<T>>({
    // 页数
    pageSize: 10,
    // 当前页
    currentPage: 1,
    // 总条数
    total: 10,
    // 表格数据
    tableList: [],
    // AxiosConfig 配置
    params: {
      ...(config?.defaultParams || {})
    },
    // 加载中
    loading: true,
    // 导出加载中
    exportLoading: false,
    // 当前行的数据
    currentRow: null
  })

  const paramsObj = computed(() => {
    return {
      ...tableObject.params,
      pageSize: tableObject.pageSize,
      pageNo: tableObject.currentPage
    }
  })

  watch(
    () => tableObject.currentPage,
    () => {
      methods.getList()
    }
  )

  watch(
    () => tableObject.pageSize,
    () => {
      // 当前页不为1时，修改页数后会导致多次调用getList方法
      if (tableObject.currentPage === 1) {
        methods.getList()
      } else {
        tableObject.currentPage = 1
        methods.getList()
      }
    }
  )

  // Table实例
  const tableRef = ref<typeof Table & TableExpose>()

  // ElTable实例
  const elTableRef = ref<ComponentRef<typeof ElTable>>()

  const register = (ref: typeof Table & TableExpose, elRef: ComponentRef<typeof ElTable>) => {
    tableRef.value = ref
    elTableRef.value = elRef
  }

  const getTable = async () => {
    await nextTick()
    const table = unref(tableRef)
    if (!table) {
      console.error('The table is not registered. Please use the register method to register')
    }
    return table
  }

  const delData = async (ids: string | number | string[] | number[]) => {
    let idsLength = 1
    if (ids instanceof Array) {
      idsLength = ids.length
      await Promise.all(
        ids.map(async (id: string | number) => {
          await (config?.delListApi && config?.delListApi(id))
        })
      )
    } else {
      await (config?.delListApi && config?.delListApi(ids))
    }
    ElMessage.success(t('common.delSuccess'))

    // 计算出临界点
    tableObject.currentPage =
      tableObject.total % tableObject.pageSize === idsLength || tableObject.pageSize === 1
        ? tableObject.currentPage > 1
          ? tableObject.currentPage - 1
          : tableObject.currentPage
        : tableObject.currentPage
    await methods.getList()
  }

  const methods = {
    getList: async () => {
      tableObject.loading = true
      const res = await config?.getListApi(unref(paramsObj)).finally(() => {
        tableObject.loading = false
      })
      if (res) {
        tableObject.tableList = (res as unknown as ResponseType).list
        if ((res as unknown as ResponseType).total) {
          tableObject.total = (res as unknown as ResponseType).total as unknown as number
        }
      }
    },
    setProps: async (props: TableProps = {}) => {
      const table = await getTable()
      table?.setProps(props)
    },
    setColumn: async (columnProps: TableSetPropsType[]) => {
      const table = await getTable()
      table?.setColumn(columnProps)
    },
    getSelections: async () => {
      const table = await getTable()
      return (table?.selections || []) as T[]
    },
    // 与Search组件结合
    setSearchParams: (data: Recordable) => {
      tableObject.params = Object.assign(tableObject.params, {
        pageSize: tableObject.pageSize,
        pageNo: 1,
        ...data
      })
      // 页码不等于1时更新页码重新获取数据，页码等于1时重新获取数据
      if (tableObject.currentPage !== 1) {
        tableObject.currentPage = 1
      } else {
        methods.getList()
      }
    },
    // 删除数据
    delList: async (
      ids: string | number | string[] | number[],
      multiple: boolean,
      message = true
    ) => {
      const tableRef = await getTable()
      if (multiple) {
        if (!tableRef?.selections.length) {
          ElMessage.warning(t('common.delNoData'))
          return
        }
      }
      if (message) {
        ElMessageBox.confirm(t('common.delMessage'), t('common.confirmTitle'), {
          confirmButtonText: t('common.ok'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }).then(async () => {
          await delData(ids)
        })
      } else {
        await delData(ids)
      }
    },
    // 导出列表
    exportList: async (fileName: string) => {
      tableObject.exportLoading = true
      ElMessageBox.confirm(t('common.exportMessage'), t('common.confirmTitle'), {
        confirmButtonText: t('common.ok'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
        .then(async () => {
          const res = await config?.exportListApi?.(unref(paramsObj) as unknown as T)
          if (res) {
            download.excel(res as unknown as Blob, fileName)
          }
        })
        .finally(() => {
          tableObject.exportLoading = false
        })
    }
  }

  config?.props && methods.setProps(config.props)

  return {
    register,
    elTableRef,
    tableObject,
    methods
  }
}
