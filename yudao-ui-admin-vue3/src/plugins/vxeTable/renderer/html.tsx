import { VXETable } from 'vxe-table'

// 图片渲染
VXETable.renderer.add('XHtml', {
  // 默认显示模板
  renderDefault(_renderOpts, params) {
    const { row, column } = params
    return <span v-html={row[column.field]}></span>
  }
})
