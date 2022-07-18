import download from '@/utils/download'
import { useI18n } from '@/hooks/web/useI18n'
import { Table, TableExpose } from '@/components/Table'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { computed, nextTick, reactive, ref, unref, watch } from 'vue'
import type { TableProps } from '@/components/Table/src/types'
const { t } = useI18n()

interface UseTableConfig<T, L> {
  getListApi: (option: L) => Promise<T>
  delListApi?: (ids: string | number) => Promise<unknown>
  exportListApi?: (option: L) => Promise<T>
  props?: TableProps
}

interface TableObject<K, L> {
  pageSize: number
  currentPage: number
  total: number
  tableList: K[]
  paramsObj: L
  loading: boolean
  exportLoading: boolean
  currentRow: Nullable<K>
}

export const useTable = <T, K, L extends AxiosConfig = AxiosConfig>(
  config?: UseTableConfig<T, L>
) => {
  const tableObject = reactive<TableObject<K, L>>({
    // 页数
    pageSize: 10,
    // 当前页
    currentPage: 1,
    // 总条数
    total: 10,
    // 表格数据
    tableList: [],
    // AxiosConfig 配置
    paramsObj: {} as L,
    // 加载中
    loading: true,
    // 导出加载中
    exportLoading: false,
    // 当前行的数据
    currentRow: null
  })

  const paramsObj = computed(() => {
    return {
      params: {
        ...tableObject.paramsObj.params,
        pageSize: tableObject.pageSize,
        pageNo: tableObject.currentPage
      }
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
    methods.getList()
  }

  const methods: {
    setProps: (props: Recordable) => void
    getList: () => Promise<void>
    setColumn: (columnProps: TableSetPropsType[]) => void
    setSearchParams: (data: Recordable) => void
    getSelections: () => Promise<K[]>
    delList: (ids: string | number | string[] | number[], multiple: boolean) => Promise<void>
    exportList: (fileName: string) => Promise<void>
  } = {
    getList: async () => {
      tableObject.loading = true
      const res = await config
        ?.getListApi(unref(paramsObj) as unknown as L)
        .catch(() => {})
        .finally(() => {
          tableObject.loading = false
        })
      if (res) {
        tableObject.tableList = res?.list
        tableObject.total = res?.total
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
      return (table?.selections || []) as K[]
    },
    // 与Search组件结合
    setSearchParams: (data: Recordable) => {
      tableObject.currentPage = 1
      tableObject.paramsObj = Object.assign(tableObject.paramsObj, {
        params: {
          pageSize: tableObject.pageSize,
          pageNo: tableObject.currentPage,
          ...data
        }
      })
      methods.getList()
    },
    // 删除数据
    delList: async (ids: string | number | string[] | number[], multiple: boolean) => {
      const tableRef = await getTable()
      let message = 'common.delDataMessage'
      if (multiple) {
        if (!tableRef?.selections.length) {
          ElMessage.warning(t('common.delNoData'))
          return
        } else {
          message = 'common.delMessage'
        }
      }
      ElMessageBox.confirm(t(message), t('common.confirmTitle'), {
        confirmButtonText: t('common.ok'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
        .then(async () => {
          await delData(ids)
        })
        .catch(() => {})
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
          const res = await config
            ?.exportListApi?.(unref(paramsObj) as unknown as L)
            .catch(() => {})
          if (res) {
            download.excel(res, fileName)
          }
        })
        .catch(() => {})
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
