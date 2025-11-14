import dayjs from 'dayjs';

import { $t } from '#/locales';

/** 时间段选择器拓展  */
export function getRangePickerDefaultProps() {
  return {
    startPlaceholder: $t('utils.rangePicker.beginTime'),
    endPlaceholder: $t('utils.rangePicker.endTime'),
    type: 'datetimerange',
    format: 'YYYY-MM-dd HH:mm:ss',
    valueFormat: 'YYYY-MM-dd HH:mm:ss',
    defaultTime: ['00:00:00', '23:59:59'],
    shortcuts: {
      [$t('utils.rangePicker.today')]: () =>
        [
          dayjs().startOf('day').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.last7Days')]: () =>
        [
          dayjs().subtract(7, 'day').startOf('day').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.last30Days')]: () =>
        [
          dayjs().subtract(30, 'day').startOf('day').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.yesterday')]: () =>
        [
          dayjs().subtract(1, 'day').startOf('day').toDate(),
          dayjs().subtract(1, 'day').endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.thisWeek')]: () =>
        [
          dayjs().startOf('week').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.thisMonth')]: () =>
        [
          dayjs().startOf('month').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
      [$t('utils.rangePicker.lastWeek')]: () =>
        [
          dayjs().subtract(1, 'week').startOf('day').toDate(),
          dayjs().endOf('day').toDate(),
        ] as const,
    },
  };
}
