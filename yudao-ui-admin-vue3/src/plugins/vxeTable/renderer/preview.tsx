import { VXETable } from 'vxe-table'
import { ElImage, ElLink } from 'element-plus'

// 图片渲染
VXETable.renderer.add('XPreview', {
  // 默认显示模板
  renderDefault(_renderOpts, params) {
    const { row, column } = params
    if (row.type.indexOf('image/') === 0) {
      return (
        <ElImage
          style="width: 80px; height: 50px"
          src={row[column.field]}
          key={row[column.field]}
          preview-src-list={[row[column.field]]}
          fit="contain"
          lazy
        ></ElImage>
      )
    } else if (row.type.indexOf('video/') === 0) {
      return (
        <video>
          <source src={row[column.field]}></source>
        </video>
      )
    } else {
      return (
        <ElLink href={row[column.field]} target="_blank">
          {row[column.field]}
        </ElLink>
      )
    }
  }
})
