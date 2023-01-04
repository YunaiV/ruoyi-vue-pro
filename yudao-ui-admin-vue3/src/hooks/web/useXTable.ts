import { ref, unref } from 'vue'
import { XTableProps } from '@/components/XTable/src/type'

export interface tableMethod {
  reload: () => void
  setProps: (props: XTableProps) => void
  deleteData: (ids: string | number) => void
  exportList: (fileName?: string) => void
}

export const useXTable = (props: XTableProps): [Function, tableMethod] => {
  const tableRef = ref<Nullable<tableMethod>>(null)

  const register = (instance) => {
    tableRef.value = instance
    props && instance.setProps(props)
  }
  const getInstance = (): tableMethod => {
    const table = unref(tableRef)
    if (!table) {
      console.error('表格实例不存在')
    }
    return table as tableMethod
  }
  const methods: tableMethod = {
    reload: () => getInstance().reload(),
    setProps: (props) => getInstance().setProps(props),
    deleteData: (ids: string | number) => getInstance().deleteData(ids),
    exportList: (fileName?: string) => getInstance().exportList(fileName)
  }
  return [register, methods]
}
