import dayjs from 'dayjs';

import { $t } from '#/locales';

/** 时间段选择器拓展 */
export function getRangePickerDefaultProps() {
  return {
    // 设置日期格式，为数组时支持多格式匹配，展示以第一个为准。配置参考 dayjs，支持自定义格式
    format: 'YYYY-MM-DD HH:mm:ss',
    // 绑定值的格式，对 value、defaultValue、defaultPickerValue 起作用。不指定则绑定值为 dayjs 对象
    valueFormat: 'YYYY-MM-DD HH:mm:ss',
    // 输入框提示文字
    placeholder: [
      $t('utils.rangePicker.beginTime'),
      $t('utils.rangePicker.endTime'),
    ],
    // 快捷时间范围
    presets: [
      {
        label: $t('utils.rangePicker.today'),
        value: [dayjs().startOf('day'), dayjs().endOf('day')],
      },
      {
        label: $t('utils.rangePicker.last7Days'),
        value: [
          dayjs().subtract(7, 'day').startOf('day'),
          dayjs().endOf('day'),
        ],
      },
      {
        label: $t('utils.rangePicker.last30Days'),
        value: [
          dayjs().subtract(30, 'day').startOf('day'),
          dayjs().endOf('day'),
        ],
      },
      {
        label: $t('utils.rangePicker.yesterday'),
        value: [
          dayjs().subtract(1, 'day').startOf('day'),
          dayjs().subtract(1, 'day').endOf('day'),
        ],
      },
      {
        label: $t('utils.rangePicker.thisWeek'),
        value: [dayjs().startOf('week'), dayjs().endOf('day')],
      },
      {
        label: $t('utils.rangePicker.thisMonth'),
        value: [dayjs().startOf('month'), dayjs().endOf('day')],
      },
      {
        label: $t('utils.rangePicker.lastWeek'),
        value: [
          dayjs().subtract(1, 'week').startOf('day'),
          dayjs().endOf('day'),
        ],
      },
    ],
    showTime: {
      defaultValue: [
        dayjs('00:00:00', 'HH:mm:ss'),
        dayjs('23:59:59', 'HH:mm:ss'),
      ],
      format: 'HH:mm:ss',
    },
  };
}
