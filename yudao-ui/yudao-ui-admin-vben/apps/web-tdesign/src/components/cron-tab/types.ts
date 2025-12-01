export interface ShortcutsType {
  text: string;
  value: string;
}

export interface CronRange {
  start: number | string | undefined;
  end: number | string | undefined;
}

export interface CronLoop {
  start: number | string | undefined;
  end: number | string | undefined;
}

export interface CronItem {
  type: string;
  range: CronRange;
  loop: CronLoop;
  appoint: string[];
  last?: string;
}

export interface CronValue {
  second: CronItem;
  minute: CronItem;
  hour: CronItem;
  day: CronItem;
  month: CronItem;
  week: CronItem & { last: string };
  year: CronItem;
}

export interface WeekOption {
  value: string;
  label: string;
}

export interface CronData {
  second: string[];
  minute: string[];
  hour: string[];
  day: string[];
  month: string[];
  week: WeekOption[];
  year: number[];
}

const getYear = (): number[] => {
  const v: number[] = [];
  const y = new Date().getFullYear();
  for (let i = 0; i < 11; i++) {
    v.push(y + i);
  }
  return v;
};

export const CronValueDefault: CronValue = {
  second: {
    type: '0',
    range: {
      start: 1,
      end: 2,
    },
    loop: {
      start: 0,
      end: 1,
    },
    appoint: [],
  },
  minute: {
    type: '0',
    range: {
      start: 1,
      end: 2,
    },
    loop: {
      start: 0,
      end: 1,
    },
    appoint: [],
  },
  hour: {
    type: '0',
    range: {
      start: 1,
      end: 2,
    },
    loop: {
      start: 0,
      end: 1,
    },
    appoint: [],
  },
  day: {
    type: '0',
    range: {
      start: 1,
      end: 2,
    },
    loop: {
      start: 1,
      end: 1,
    },
    appoint: [],
  },
  month: {
    type: '0',
    range: {
      start: 1,
      end: 2,
    },
    loop: {
      start: 1,
      end: 1,
    },
    appoint: [],
  },
  week: {
    type: '5',
    range: {
      start: '2',
      end: '3',
    },
    loop: {
      start: 0,
      end: '2',
    },
    last: '2',
    appoint: [],
  },
  year: {
    type: '-1',
    range: {
      start: getYear()[0],
      end: getYear()[1],
    },
    loop: {
      start: getYear()[0],
      end: 1,
    },
    appoint: [],
  },
};

export const CronDataDefault: CronData = {
  second: [
    '0',
    '5',
    '15',
    '20',
    '25',
    '30',
    '35',
    '40',
    '45',
    '50',
    '55',
    '59',
  ],
  minute: [
    '0',
    '5',
    '15',
    '20',
    '25',
    '30',
    '35',
    '40',
    '45',
    '50',
    '55',
    '59',
  ],
  hour: [
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
    '10',
    '11',
    '12',
    '13',
    '14',
    '15',
    '16',
    '17',
    '18',
    '19',
    '20',
    '21',
    '22',
    '23',
  ],
  day: [
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
    '10',
    '11',
    '12',
    '13',
    '14',
    '15',
    '16',
    '17',
    '18',
    '19',
    '20',
    '21',
    '22',
    '23',
    '24',
    '25',
    '26',
    '27',
    '28',
    '29',
    '30',
    '31',
  ],
  month: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
  week: [
    {
      value: '1',
      label: '周日',
    },
    {
      value: '2',
      label: '周一',
    },
    {
      value: '3',
      label: '周二',
    },
    {
      value: '4',
      label: '周三',
    },
    {
      value: '5',
      label: '周四',
    },
    {
      value: '6',
      label: '周五',
    },
    {
      value: '7',
      label: '周六',
    },
  ],
  year: getYear(),
};
