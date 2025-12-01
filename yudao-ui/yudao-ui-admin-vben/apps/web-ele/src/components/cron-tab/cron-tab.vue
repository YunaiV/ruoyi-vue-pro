<script lang="ts" setup>
import type { PropType } from 'vue';

import { computed, onMounted, reactive, ref, watch } from 'vue';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElTabPane,
  ElTabs,
} from 'element-plus';

defineOptions({ name: 'Crontab' });

const props = defineProps({
  modelValue: {
    type: String,
    default: '* * * * * ?',
  },
  shortcuts: { type: Array as PropType<shortcutsType[]>, default: () => [] },
});

const emit = defineEmits(['update:modelValue']);

// TODO @puhui999：可以参考 apps/web-antd/src/components/cron-tab/cron-tab.vue 简化到 types；ps：可以用 idea 对比两个 ts 或者 vue 文件，看看差异的地方。差异的地方越少越好（容易维护）
interface shortcutsType {
  text: string;
  value: string;
}

const defaultValue = ref('');
const dialogVisible = ref(false);
const getYear = () => {
  const v: number[] = [];
  const y = new Date().getFullYear();
  for (let i = 0; i < 11; i++) {
    v.push(y + i);
  }
  return v;
};
// TODO @puhui999：可以参考 apps/web-antd/src/components/cron-tab/cron-tab.vue 简化到 types
const cronValue = reactive({
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
    appoint: [] as string[],
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
    appoint: [] as string[],
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
    appoint: [] as string[],
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
    appoint: [] as string[],
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
    appoint: [] as string[],
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
    appoint: [] as string[],
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
    appoint: [] as string[],
  },
});
const data = reactive({
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
});

const value_second = computed(() => {
  const v = cronValue.second;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    default: {
      return '*';
    }
  }
});

const value_minute = computed(() => {
  const v = cronValue.minute;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    default: {
      return '*';
    }
  }
});

const value_hour = computed(() => {
  const v = cronValue.hour;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    default: {
      return '*';
    }
  }
});

const value_day = computed(() => {
  const v = cronValue.day;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    case '4': {
      return 'L';
    }
    case '5': {
      return '?';
    }
    default: {
      return '*';
    }
  }
});

const value_month = computed(() => {
  const v = cronValue.month;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    default: {
      return '*';
    }
  }
});

const value_week = computed(() => {
  const v = cronValue.week;
  switch (v.type) {
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.end}#${v.loop.start}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '*';
    }
    case '4': {
      return `${v.last}L`;
    }
    case '5': {
      return '?';
    }
    default: {
      return '*';
    }
  }
});

const value_year = computed(() => {
  const v = cronValue.year;
  switch (v.type) {
    case '-1': {
      return '';
    }
    case '0': {
      return '*';
    }
    case '1': {
      return `${v.range.start}-${v.range.end}`;
    }
    case '2': {
      return `${v.loop.start}/${v.loop.end}`;
    }
    case '3': {
      return v.appoint.length > 0 ? v.appoint.join(',') : '';
    }
    default: {
      return '';
    }
  }
});

watch(
  () => cronValue.week.type,
  (val: string) => {
    if (val !== '5') {
      cronValue.day.type = '5';
    }
  },
);

watch(
  () => cronValue.day.type,
  (val: string) => {
    if (val !== '5') {
      cronValue.week.type = '5';
    }
  },
);

watch(
  () => props.modelValue,
  () => {
    defaultValue.value = props.modelValue;
  },
);

onMounted(() => {
  defaultValue.value = props.modelValue;
});

const select = ref<string>();

watch(
  () => select.value,
  () => {
    if (select.value === 'custom') {
      open();
    } else {
      defaultValue.value = select.value || '';
      emit('update:modelValue', defaultValue.value);
    }
  },
);

function open() {
  set();
  dialogVisible.value = true;
}

function set() {
  defaultValue.value = props.modelValue;
  let arr = (props.modelValue || '* * * * * ?').split(' ');
  // 简单检查
  if (arr.length < 6) {
    ElMessage.warning('cron表达式错误，已转换为默认表达式');
    arr = '* * * * * ?'.split(' ');
  }

  // 秒
  if (arr[0] === '*') {
    cronValue.second.type = '0';
  } else if (arr[0]?.includes('-')) {
    cronValue.second.type = '1';
    cronValue.second.range.start = Number(arr[0].split('-')[0]);
    cronValue.second.range.end = Number(arr[0].split('-')[1]);
  } else if (arr[0]?.includes('/')) {
    cronValue.second.type = '2';
    cronValue.second.loop.start = Number(arr[0].split('/')[0]);
    cronValue.second.loop.end = Number(arr[0].split('/')[1]);
  } else {
    cronValue.second.type = '3';
    cronValue.second.appoint = arr[0]?.split(',') || [];
  }

  // 分
  if (arr[1] === '*') {
    cronValue.minute.type = '0';
  } else if (arr[1]?.includes('-')) {
    cronValue.minute.type = '1';
    cronValue.minute.range.start = Number(arr[1].split('-')[0]);
    cronValue.minute.range.end = Number(arr[1].split('-')[1]);
  } else if (arr[1]?.includes('/')) {
    cronValue.minute.type = '2';
    cronValue.minute.loop.start = Number(arr[1].split('/')[0]);
    cronValue.minute.loop.end = Number(arr[1].split('/')[1]);
  } else {
    cronValue.minute.type = '3';
    cronValue.minute.appoint = arr[1]?.split(',') || [];
  }

  // 小时
  if (arr[2] === '*') {
    cronValue.hour.type = '0';
  } else if (arr[2]?.includes('-')) {
    cronValue.hour.type = '1';
    cronValue.hour.range.start = Number(arr[2].split('-')[0]);
    cronValue.hour.range.end = Number(arr[2].split('-')[1]);
  } else if (arr[2]?.includes('/')) {
    cronValue.hour.type = '2';
    cronValue.hour.loop.start = Number(arr[2].split('/')[0]);
    cronValue.hour.loop.end = Number(arr[2].split('/')[1]);
  } else {
    cronValue.hour.type = '3';
    cronValue.hour.appoint = arr[2]?.split(',') || [];
  }

  // 日
  switch (arr[3]) {
    case '*': {
      cronValue.day.type = '0';
      break;
    }
    case '?': {
      cronValue.day.type = '5';
      break;
    }
    case 'L': {
      cronValue.day.type = '4';
      break;
    }
    default: {
      if (arr[3]?.includes('-')) {
        cronValue.day.type = '1';
        cronValue.day.range.start = Number(arr[3].split('-')[0]);
        cronValue.day.range.end = Number(arr[3].split('-')[1]);
      } else if (arr[3]?.includes('/')) {
        cronValue.day.type = '2';
        cronValue.day.loop.start = Number(arr[3].split('/')[0]);
        cronValue.day.loop.end = Number(arr[3].split('/')[1]);
      } else {
        cronValue.day.type = '3';
        cronValue.day.appoint = arr[3]?.split(',') || [];
      }
    }
  }

  // 月
  if (arr[4] === '*') {
    cronValue.month.type = '0';
  } else if (arr[4]?.includes('-')) {
    cronValue.month.type = '1';
    cronValue.month.range.start = Number(arr[4].split('-')[0]);
    cronValue.month.range.end = Number(arr[4].split('-')[1]);
  } else if (arr[4]?.includes('/')) {
    cronValue.month.type = '2';
    cronValue.month.loop.start = Number(arr[4].split('/')[0]);
    cronValue.month.loop.end = Number(arr[4].split('/')[1]);
  } else {
    cronValue.month.type = '3';
    cronValue.month.appoint = arr[4]?.split(',') || [];
  }

  // 周
  if (arr[5] === '*') {
    cronValue.week.type = '0';
  } else if (arr[5] === '?') {
    cronValue.week.type = '5';
  } else if (arr[5]?.includes('-')) {
    cronValue.week.type = '1';
    cronValue.week.range.start = arr[5].split('-')[0] || '';
    cronValue.week.range.end = arr[5].split('-')[1] || '';
  } else if (arr[5]?.includes('#')) {
    cronValue.week.type = '2';
    cronValue.week.loop.start = Number(arr[5].split('#')[1]);
    cronValue.week.loop.end = arr[5].split('#')[0] || '';
  } else if (arr[5]?.includes('L')) {
    cronValue.week.type = '4';
    cronValue.week.last = arr[5].split('L')[0] || '';
  } else {
    cronValue.week.type = '3';
    cronValue.week.appoint = arr[5]?.split(',') || [];
  }

  // 年
  if (!arr[6]) {
    cronValue.year.type = '-1';
  } else if (arr[6] === '*') {
    cronValue.year.type = '0';
  } else if (arr[6]?.includes('-')) {
    cronValue.year.type = '1';
    cronValue.year.range.start = Number(arr[6].split('-')[0]);
    cronValue.year.range.end = Number(arr[6].split('-')[1]);
  } else if (arr[6]?.includes('/')) {
    cronValue.year.type = '2';
    cronValue.year.loop.start = Number(arr[6].split('/')[1]);
    cronValue.year.loop.end = Number(arr[6].split('/')[0]);
  } else {
    cronValue.year.type = '3';
    cronValue.year.appoint = arr[6]?.split(',') || [];
  }
}

function submit() {
  const year = value_year.value ? ` ${value_year.value}` : '';
  defaultValue.value = `${value_second.value} ${value_minute.value} ${
    value_hour.value
  } ${value_day.value} ${value_month.value} ${value_week.value}${year}`;
  emit('update:modelValue', defaultValue.value);
  dialogVisible.value = false;
}

function inputChange() {
  emit('update:modelValue', defaultValue.value);
}
</script>
<template>
  <ElInput
    v-model="defaultValue"
    class="input-with-select"
    v-bind="$attrs"
    @input="inputChange"
  >
    <template #append>
      <ElSelect v-model="select" placeholder="生成器" style="width: 115px">
        <ElOption label="每分钟" value="0 * * * * ?" />
        <ElOption label="每小时" value="0 0 * * * ?" />
        <ElOption label="每天零点" value="0 0 0 * * ?" />
        <ElOption label="每月一号零点" value="0 0 0 1 * ?" />
        <ElOption label="每月最后一天零点" value="0 0 0 L * ?" />
        <ElOption label="每周星期日零点" value="0 0 0 ? * 1" />
        <ElOption
          v-for="(item, index) in shortcuts"
          :key="index"
          :label="item.text"
          :value="item.value"
        />
        <ElOption label="自定义" value="custom" />
      </ElSelect>
    </template>
  </ElInput>

  <ElDialog
    v-model="dialogVisible"
    :width="580"
    append-to-body
    destroy-on-close
    title="cron规则生成器"
  >
    <div class="sc-cron">
      <ElTabs>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>秒</h2>
              <h4>{{ value_second }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.second.type">
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.second.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.second.range.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.second.range.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.second.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.second.loop.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒开始，每
              <ElInputNumber
                v-model="cronValue.second.loop.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.second.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.second.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.second"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>分钟</h2>
              <h4>{{ value_minute }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.minute.type">
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.minute.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.minute.range.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.minute.range.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.minute.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.minute.loop.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟开始，每
              <ElInputNumber
                v-model="cronValue.minute.loop.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.minute.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.minute.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.minute"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>小时</h2>
              <h4>{{ value_hour }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.hour.type">
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.hour.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.hour.range.start"
                :max="23"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.hour.range.end"
                :max="23"
                :min="0"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.hour.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.hour.loop.start"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时开始，每
              <ElInputNumber
                v-model="cronValue.hour.loop.end"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.hour.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.hour.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.hour"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>日</h2>
              <h4>{{ value_day }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.day.type">
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
                <ElRadioButton value="4">本月最后一天</ElRadioButton>
                <ElRadioButton value="5">不指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.day.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.day.range.start"
                :max="31"
                :min="1"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.day.range.end"
                :max="31"
                :min="1"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.day.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.day.loop.start"
                :max="31"
                :min="1"
                controls-position="right"
              />
              号开始，每
              <ElInputNumber
                v-model="cronValue.day.loop.end"
                :max="31"
                :min="1"
                controls-position="right"
              />
              天执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.day.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.day.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.day"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>月</h2>
              <h4>{{ value_month }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.month.type">
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.month.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.month.range.start"
                :max="12"
                :min="1"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.month.range.end"
                :max="12"
                :min="1"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.month.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.month.loop.start"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月开始，每
              <ElInputNumber
                v-model="cronValue.month.loop.end"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.month.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.month.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.month"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>周</h2>
              <h4>{{ value_week }}</h4>
            </div>
          </template>
          <ElForm>
            <ElForm>
              <ElFormItem label="类型">
                <ElRadioGroup v-model="cronValue.week.type">
                  <ElRadioButton value="0">任意值</ElRadioButton>
                  <ElRadioButton value="1">范围</ElRadioButton>
                  <ElRadioButton value="2">间隔</ElRadioButton>
                  <ElRadioButton value="3">指定</ElRadioButton>
                  <ElRadioButton value="4">本月最后一周</ElRadioButton>
                  <ElRadioButton value="5">不指定</ElRadioButton>
                </ElRadioGroup>
              </ElFormItem>
              <ElFormItem v-if="cronValue.week.type === '1'" label="范围">
                <ElSelect v-model="cronValue.week.range.start">
                  <ElOption
                    v-for="(item, index) in data.week"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
                <span style="padding: 0 15px">-</span>
                <ElSelect v-model="cronValue.week.range.end">
                  <ElOption
                    v-for="(item, index) in data.week"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
              <ElFormItem v-if="cronValue.week.type === '2'" label="间隔">
                第
                <ElInputNumber
                  v-model="cronValue.week.loop.start"
                  :max="4"
                  :min="1"
                  controls-position="right"
                />
                周的星期
                <ElSelect v-model="cronValue.week.loop.end">
                  <ElOption
                    v-for="(item, index) in data.week"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
                执行一次
              </ElFormItem>
              <ElFormItem v-if="cronValue.week.type === '3'" label="指定">
                <ElSelect
                  v-model="cronValue.week.appoint"
                  multiple
                  style="width: 100%"
                >
                  <ElOption
                    v-for="(item, index) in data.week"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
              <ElFormItem v-if="cronValue.week.type === '4'" label="最后一周">
                <ElSelect v-model="cronValue.week.last">
                  <ElOption
                    v-for="(item, index) in data.week"
                    :key="index"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElForm>
          </ElForm>
        </ElTabPane>
        <ElTabPane>
          <template #label>
            <div class="sc-cron-num">
              <h2>年</h2>
              <h4>{{ value_year }}</h4>
            </div>
          </template>
          <ElForm>
            <ElFormItem label="类型">
              <ElRadioGroup v-model="cronValue.year.type">
                <ElRadioButton value="-1">忽略</ElRadioButton>
                <ElRadioButton value="0">任意值</ElRadioButton>
                <ElRadioButton value="1">范围</ElRadioButton>
                <ElRadioButton value="2">间隔</ElRadioButton>
                <ElRadioButton value="3">指定</ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem v-if="cronValue.year.type === '1'" label="范围">
              <ElInputNumber
                v-model="cronValue.year.range.start"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <ElInputNumber
                v-model="cronValue.year.range.end"
                controls-position="right"
              />
            </ElFormItem>
            <ElFormItem v-if="cronValue.year.type === '2'" label="间隔">
              <ElInputNumber
                v-model="cronValue.year.loop.start"
                controls-position="right"
              />
              年开始，每
              <ElInputNumber
                v-model="cronValue.year.loop.end"
                :min="1"
                controls-position="right"
              />
              年执行一次
            </ElFormItem>
            <ElFormItem v-if="cronValue.year.type === '3'" label="指定">
              <ElSelect
                v-model="cronValue.year.appoint"
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="(item, index) in data.year"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </ElSelect>
            </ElFormItem>
          </ElForm>
        </ElTabPane>
      </ElTabs>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">取 消</ElButton>
      <ElButton type="primary" @click="submit()">确 认</ElButton>
    </template>
  </ElDialog>
</template>

<style scoped>
.sc-cron:deep(.el-tabs__item) {
  height: auto;
  padding: 0 7px;
  line-height: 1;
  vertical-align: bottom;
}

.sc-cron-num {
  width: 100%;
  margin-bottom: 15px;
  text-align: center;
}

.sc-cron-num h2 {
  margin-bottom: 15px;
  font-size: 12px;
  font-weight: normal;
}

.sc-cron-num h4 {
  display: block;
  width: 100%;
  height: 32px;
  padding: 0 15px;
  font-size: 12px;
  line-height: 30px;
  background: var(--el-color-primary-light-9);
  border-radius: 4px;
}

.sc-cron:deep(.el-tabs__item.is-active) .sc-cron-num h4 {
  color: #fff;
  background: var(--el-color-primary);
}

[data-theme='dark'] .sc-cron-num h4 {
  background: var(--el-color-white);
}

.input-with-select .el-input-group__prepend {
  background-color: var(--el-fill-color-blank);
}
</style>
