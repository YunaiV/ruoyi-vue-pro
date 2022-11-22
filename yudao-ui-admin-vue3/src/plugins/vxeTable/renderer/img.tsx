import { VXETable } from 'vxe-table'

// 图片渲染
VXETable.renderer.add('XImg', {
  // 默认显示模板
  renderDefault(_renderOpts, params) {
    const { row, column } = params
    return (
      <el-image
        style="width: 80px; height: 50px"
        src={row[column.field]}
        key={row[column.field]}
        fit="contain"
        lazy
      ></el-image>
    )
  }
})
