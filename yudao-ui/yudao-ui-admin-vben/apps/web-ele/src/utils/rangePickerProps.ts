import dayjs from 'dayjs';

import { $t } from '#/locales';

/** 时间段选择器拓展 */
export function getRangePickerDefaultProps() {
  return {
    // 显示在输入框中的格式
    format: 'YYYY-MM-DD HH:mm:ss',
    // 绑定值的格式。 不指定则绑定值为 Date 对象
    valueFormat: 'YYYY-MM-DD HH:mm:ss',
    defaultTime: [new Date('1 00:00:00'), new Date('1 23:59:59')],
    // 输入框提示文字
    startPlaceholder: $t('utils.rangePicker.beginTime'),
    endPlaceholder: $t('utils.rangePicker.endTime'),
    // 快捷时间范围
    shortcuts: [
      {
        text: $t('utils.rangePicker.today'),
        value: () => {
          return [dayjs().startOf('day'), dayjs().endOf('day')];
        },
      },
      {
        text: $t('utils.rangePicker.yesterday'),
        value: () => {
          return [
            dayjs().subtract(1, 'day').startOf('day'),
            dayjs().subtract(1, 'day').endOf('day'),
          ];
        },
      },
      {
        text: $t('utils.rangePicker.last7Days'),
        value: () => {
          return [
            dayjs().subtract(7, 'day').startOf('day'),
            dayjs().endOf('day'),
          ];
        },
      },
      {
        text: $t('utils.rangePicker.last30Days'),
        value: () => {
          return [
            dayjs().subtract(30, 'day').startOf('day'),
            dayjs().endOf('day'),
          ];
        },
      },
      {
        text: $t('utils.rangePicker.thisWeek'),
        value: () => {
          return [dayjs().startOf('week'), dayjs().endOf('day')];
        },
      },
      {
        text: $t('utils.rangePicker.lastWeek'),
        value: () => {
          return [
            dayjs().subtract(1, 'week').startOf('day'),
            dayjs().endOf('day'),
          ];
        },
      },
      {
        text: $t('utils.rangePicker.thisMonth'),
        value: () => {
          return [dayjs().startOf('month'), dayjs().endOf('day')];
        },
      },
    ],
  };
}
