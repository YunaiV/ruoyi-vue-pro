import { VXETable } from 'vxe-table'

// 超链接渲染
VXETable.renderer.add('XLink', {
  // 默认显示模板
  renderDefault(renderOpts, params) {
    const { row, column } = params
    const { events = {} } = renderOpts
    return (
      <a class="link" onClick={() => events.click(params)}>
        {row[column.field]}
      </a>
    )
  }
})
