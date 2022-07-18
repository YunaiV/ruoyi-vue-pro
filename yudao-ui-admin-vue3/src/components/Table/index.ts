import Table from './src/Table.vue'

export interface TableExpose {
  setProps: (props: Recordable) => void
  setColumn: (columnProps: TableSetPropsType[]) => void
  selections: Recordable[]
}

export { Table }
