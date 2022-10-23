import Table from './src/Table.vue'
import { ElTable } from 'element-plus'
import { TableSetPropsType } from '@/types/table'

export interface TableExpose {
  setProps: (props: Recordable) => void
  setColumn: (columnProps: TableSetPropsType[]) => void
  selections: Recordable[]
  elTableRef: ComponentRef<typeof ElTable>
}

export { Table }
