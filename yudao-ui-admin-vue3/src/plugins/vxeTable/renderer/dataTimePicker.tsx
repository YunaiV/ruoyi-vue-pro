import { useI18n } from '@/hooks/web/useI18n'
import { ElDatePicker } from 'element-plus'
import { VXETable } from 'vxe-table'

// 日期区间选择渲染
VXETable.renderer.add('XDataTimePicker', {
  // 默认显示模板
  renderItemContent(renderOpts, params) {
    const { t } = useI18n()
    const { data, field } = params
    const { content } = renderOpts
    return (
      <ElDatePicker
        v-model={data[field]}
        style="width: 100%"
        type={content ? (content as any) : 'datetimerange'}
        value-format="YYYY-MM-DD HH:mm:ss"
        range-separator="-"
        start-placeholder={t('common.startTimeText')}
        end-placeholder={t('common.endTimeText')}
      ></ElDatePicker>
    )
  }
})
