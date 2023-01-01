import { DictTag } from '@/components/DictTag'
import { VXETable } from 'vxe-table'

// 字典渲染
VXETable.renderer.add('XDict', {
  // 默认显示模板
  renderDefault(renderOpts, params) {
    const { row, column } = params
    const { content } = renderOpts
    return <DictTag type={content as unknown as string} value={row[column.field]}></DictTag>
  }
})
