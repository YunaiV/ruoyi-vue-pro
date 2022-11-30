import { ElDatePicker } from 'element-plus'
import { VXETable } from 'vxe-table'

// 日期区间选择渲染
VXETable.renderer.add('XDataPicker', {
  // 默认显示模板
  renderItemContent(renderOpts, params) {
    const { data, field } = params
    const { content } = renderOpts
    return (
      <ElDatePicker
        v-model={data[field]}
        style="width: 100%"
        type={content ? (content as any) : 'datetime'}
        value-format="YYYY-MM-DD HH:mm:ss"
        clearable
      ></ElDatePicker>
    )
  }
})
