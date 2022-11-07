import { reactive } from 'vue'
import { VxeGridProps } from 'vxe-table'

export const useVxeGrid = (allSchemas, getPageApi) => {
  const gridOptions = reactive<VxeGridProps>({
    loading: false,
    height: 800,
    rowConfig: {
      keyField: 'id',
      isHover: true
    },
    toolbarConfig: {
      custom: true,
      slots: { buttons: 'toolbar_buttons' }
    },
    printConfig: {
      columns: allSchemas.printSchema
    },
    formConfig: {
      titleWidth: 100,
      titleAlign: 'right',
      items: allSchemas.searchSchema
    },
    columns: allSchemas.tableSchema,
    pagerConfig: {
      border: false,
      background: false,
      perfect: true,
      pageSize: 10,
      pagerCount: 7,
      pageSizes: [5, 10, 15, 20, 50, 100, 200, 500],
      layouts: [
        'PrevJump',
        'PrevPage',
        'Jump',
        'PageCount',
        'NextPage',
        'NextJump',
        'Sizes',
        'Total'
      ]
    },
    proxyConfig: {
      seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
      form: true, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
      props: { result: 'list', total: 'total' },
      ajax: {
        query: ({ page, form }) => {
          const queryParams = Object.assign({}, form)
          queryParams.pageSize = page.pageSize
          queryParams.pageNo = page.currentPage
          return new Promise(async (resolve) => {
            resolve(await getPageApi(queryParams))
          })
        }
      }
    }
  })
  return gridOptions
}
