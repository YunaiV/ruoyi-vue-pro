<script setup lang="ts">
import { PropType, reactive, ref } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { VxeGrid, VxeGridProps, VXETable, VxeTableInstance } from 'vxe-table'
import XEUtils from 'xe-utils'

const props = defineProps({
  columns: {
    type: Array as PropType<any[]>,
    default: () => []
  },
  form: {
    type: Array as PropType<any[]>,
    default: () => []
  },
  api: propTypes.string.def('')
})
const xGrid = ref<VxeTableInstance>()

const gridOptions = reactive<VxeGridProps>({
  id: 'crud',
  height: 600,
  showHeaderOverflow: true,
  showOverflow: true,
  align: null,
  loading: true,
  columnConfig: {
    resizable: true
  },
  formConfig: {
    titleWidth: 100,
    titleAlign: 'right',
    items: []
  },
  pagerConfig: {
    pageSize: 10,
    pageSizes: [5, 10, 15, 20, 50, 100, 200, 500]
  },
  columns: [],
  toolbarConfig: {
    buttons: [
      { code: 'insert', name: '新增' },
      { code: 'delete', name: '删除' },
      { code: 'save', name: '保存', status: 'success' }
    ],
    refresh: true,
    import: true,
    export: true,
    print: true,
    zoom: true,
    custom: true
  },
  importConfig: {
    remote: true,
    types: ['xlsx'],
    modes: ['insert'],
    // 自定义服务端导入
    async importMethod({ file }) {
      const formBody = new FormData()
      formBody.append('file', file)
      try {
        const response = await fetch(`/api/pub/import`, {
          method: 'POST',
          body: formBody
        })
        const data = await response.json()
        VXETable.modal.message({
          content: `成功导入 ${data.result.insertRows} 条记录！`,
          status: 'success'
        })
        // 导入完成，刷新表格
        xGrid.value.commitProxy('query')
      } catch {
        VXETable.modal.message({ content: '导入失败，请检查数据是否正确！', status: 'error' })
      }
    }
  },
  proxyConfig: {
    seq: true, // 启用动态序号代理，每一页的序号会根据当前页数变化
    sort: true, // 启用排序代理，当点击排序时会自动触发 query 行为
    filter: true, // 启用筛选代理，当点击筛选时会自动触发 query 行为
    form: true, // 启用表单代理，当点击表单提交按钮时会自动触发 reload 行为
    // 对应响应结果 { result: [], page: { total: 100 } }
    props: {
      result: 'result', // 配置响应结果列表字段
      total: 'page.total' // 配置响应结果总页数字段
    },
    // 只接收Promise，具体实现自由发挥
    ajax: {
      // 当点击工具栏查询按钮或者手动提交指令 query或reload 时会被触发
      query: ({ page, sorts, filters, form }) => {
        const queryParams: any = Object.assign({}, form)
        // 处理排序条件
        const firstSort = sorts[0]
        if (firstSort) {
          queryParams.sort = firstSort.field
          queryParams.order = firstSort.order
        }
        // 处理筛选条件
        filters.forEach(({ field, values }) => {
          queryParams[field] = values.join(',')
        })
        return fetch(
          props.api + `/list${page.pageSize}/${page.currentPage}?${XEUtils.serialize(queryParams)}`
        ).then((response) => response.json())
      },
      // 当点击工具栏删除按钮或者手动提交指令 delete 时会被触发
      delete: ({ body }) => {
        return fetch(props.api + `/save`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        }).then((response) => response.json())
      },
      // 当点击工具栏保存按钮或者手动提交指令 save 时会被触发
      save: ({ body }) => {
        return fetch(props.api + `/save`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        }).then((response) => response.json())
      }
    }
  },
  exportConfig: {
    remote: true,
    types: ['xlsx'],
    modes: ['current', 'selected', 'all'],
    // 自定义服务端导出
    async exportMethod({ options }) {
      const $grid = xGrid.value
      const proxyInfo = $grid.getProxyInfo()
      // 传给服务端的参数
      const body = {
        filename: options.filename,
        sheetName: options.sheetName,
        isHeader: options.isHeader,
        original: options.original,
        mode: options.mode,
        pager: proxyInfo ? proxyInfo.pager : null,
        ids: options.mode === 'selected' ? options.data.map((item) => item.id) : [],
        fields: options.columns.map((column) => {
          return {
            field: column.field,
            title: column.title
          }
        })
      }
      // 开始服务端导出
      try {
        const response = await fetch(`/api/pub/export`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        })
        const data = await response.json()
        if (data.id) {
          VXETable.modal.message({ content: '导出成功，开始下载', status: 'success' })
          // 读取路径，请求文件
          fetch(`/api/pub/export/download/${data.id}`).then((response_1) => {
            response_1.blob().then((blob) => {
              // 开始下载
              VXETable.saveFile({ filename: '导出数据', type: 'xlsx', content: blob })
            })
          })
        }
      } catch {
        VXETable.modal.message({ content: '导出失败！', status: 'error' })
      }
    }
  }
})
const init = () => {
  gridOptions.columns = props.columns
  gridOptions.formConfig.items = props.form
  gridOptions.loading = false
}
init()
</script>

<template>
  <vxe-grid ref="xGrid" v-bind="gridOptions" class="pro-table-scrollbar" />
</template>

<style scoped>
/*滚动条整体部分*/
.pro-table-scrollbar ::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
/*滚动条的轨道*/
.pro-table-scrollbar ::-webkit-scrollbar-track {
  background-color: #ffffff;
}
/*滚动条里面的小方块，能向上向下移动*/
.pro-table-scrollbar ::-webkit-scrollbar-thumb {
  background-color: #bfbfbf;
  border-radius: 5px;
  border: 1px solid #f1f1f1;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
}
.pro-table-scrollbar ::-webkit-scrollbar-thumb:hover {
  background-color: #a8a8a8;
}
.pro-table-scrollbar ::-webkit-scrollbar-thumb:active {
  background-color: #787878;
}
/*边角，即两个滚动条的交汇处*/
.pro-table-scrollbar ::-webkit-scrollbar-corner {
  background-color: #ffffff;
}
</style>
