<script lang="ts" setup>
import type { PropType } from 'vue';

import type { CronData, CronValue, ShortcutsType } from './types';

import { computed, onMounted, reactive, ref, watch } from 'vue';

import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NTabPane,
  NTabs,
} from 'naive-ui';

import { message } from '#/adapter/naive';

import { CronDataDefault, CronValueDefault } from './types';

defineOptions({ name: 'Crontab' });

const props = defineProps({
  modelValue: {
    type: String,
    default: '* * * * * ?',
  },
  shortcuts: {
    type: Array as PropType<ShortcutsType[]>,
    default: () => [],
  },
});

const emit = defineEmits(['update:modelValue']);

const defaultValue = ref('');
const dialogVisible = ref(false);

const cronValue = reactive<CronValue>(CronValueDefault);

const data = reactive<CronData>(CronDataDefault);
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

  /** 简单检查 */
  if (arr.length < 6) {
    message.warning('cron表达式错误，已转换为默认表达式');
    arr = '* * * * * ?'.split(' ');
  }

  /** 秒 */
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

  /** 分 */
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

  /** 小时 */
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

  /** 日 */
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

  /** 月 */
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

  /** 周 */
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

  /** 年 */
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

const shortcutsOptions = computed(() => {
  return [
    {
      label: '每分钟',
      value: '0 * * * * ?',
    },
    {
      label: '每小时',
      value: '0 0 * * * ?',
    },
    {
      label: '每天零点',
      value: '0 0 0 * * ?',
    },
    {
      label: '每月一号零点',
      value: '0 0 0 1 * ?',
    },
    {
      label: '每月最后一天零点',
      value: '0 0 0 L * ?',
    },
    {
      label: '每周星期日零点',
      value: '0 0 0 ? * 1',
    },
    ...props.shortcuts.map((item) => ({
      label: item.text,
      value: item.value,
    })),
    {
      label: '自定义',
      value: 'custom',
    },
  ];
});
</script>

<template>
  <NInput
    v-model:value="defaultValue"
    class="input-with-select"
    v-bind="$attrs"
    @update:value="inputChange"
  >
    <template #suffix>
      <NSelect
        v-model:value="select"
        placeholder="生成器"
        class="w-36"
        :options="shortcutsOptions"
      />
    </template>
  </NInput>

  <NModal
    v-model:show="dialogVisible"
    class="w-[720px]"
    preset="card"
    destroy-on-close
    title="cron规则生成器"
  >
    <div class="sc-cron">
      <NTabs>
        <NTabPane name="second">
          <template #tab>
            <div class="sc-cron-num">
              <h2>秒</h2>
              <h4>{{ value_second }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.second.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.second.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.second.range.start as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.second.range.end as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.second.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.second.loop.start as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒开始，每
              <NInputNumber
                v-model:value="cronValue.second.loop.end as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.second.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.second.appoint"
                multiple
                class="w-full"
                :options="
                  data.second.map((item) => ({
                    label: item,
                    value: item,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="minute">
          <template #tab>
            <div class="sc-cron-num">
              <h2>分钟</h2>
              <h4>{{ value_minute }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.minute.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.minute.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.minute.range.start as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.minute.range.end as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.minute.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.minute.loop.start as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟开始，每
              <NInputNumber
                v-model:value="cronValue.minute.loop.end as number"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.minute.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.minute.appoint"
                multiple
                class="w-full"
                :options="
                  data.minute.map((item) => ({
                    label: item,
                    value: item,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="hour">
          <template #tab>
            <div class="sc-cron-num">
              <h2>小时</h2>
              <h4>{{ value_hour }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.hour.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.hour.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.hour.range.start as number"
                :max="23"
                :min="0"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.hour.range.end as number"
                :max="23"
                :min="0"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.hour.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.hour.loop.start as number"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时开始，每
              <NInputNumber
                v-model:value="cronValue.hour.loop.end as number"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.hour.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.hour.appoint"
                multiple
                class="w-full"
                :options="
                  data.hour.map((item) => ({
                    label: item,
                    value: item,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="day">
          <template #tab>
            <div class="sc-cron-num">
              <h2>日</h2>
              <h4>{{ value_day }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.day.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
                <NRadioButton value="4">本月最后一天</NRadioButton>
                <NRadioButton value="5">不指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.day.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.day.range.start as number"
                :max="31"
                :min="1"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.day.range.end as number"
                :max="31"
                :min="1"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.day.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.day.loop.start as number"
                :max="31"
                :min="1"
                controls-position="right"
              />
              号开始，每
              <NInputNumber
                v-model:value="cronValue.day.loop.end as number"
                :max="31"
                :min="1"
                controls-position="right"
              />
              天执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.day.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.day.appoint"
                multiple
                class="w-full"
                :options="
                  data.day.map((item) => ({
                    label: item,
                    value: item,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="month">
          <template #tab>
            <div class="sc-cron-num">
              <h2>月</h2>
              <h4>{{ value_month }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.month.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.month.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.month.range.start as number"
                :max="12"
                :min="1"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.month.range.end as number"
                :max="12"
                :min="1"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.month.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.month.loop.start as number"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月开始，每
              <NInputNumber
                v-model:value="cronValue.month.loop.end as number"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.month.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.month.appoint"
                multiple
                class="w-full"
                :options="
                  data.month.map((item) => ({
                    label: item,
                    value: item,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="week">
          <template #tab>
            <div class="sc-cron-num">
              <h2>周</h2>
              <h4>{{ value_week }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.week.type">
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
                <NRadioButton value="4">本月最后一周</NRadioButton>
                <NRadioButton value="5">不指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.week.type === '1'" label="范围">
              <NSelect
                v-model:value="cronValue.week.range.start"
                :options="
                  data.week.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }))
                "
              />
              <span class="px-4">-</span>
              <NSelect
                v-model:value="cronValue.week.range.end"
                :options="
                  data.week.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }))
                "
              />
            </NFormItem>
            <NFormItem v-if="cronValue.week.type === '2'" label="间隔">
              第
              <NInputNumber
                v-model:value="cronValue.week.loop.start as number"
                :max="4"
                :min="1"
                controls-position="right"
              />
              周的星期
              <NSelect
                v-model:value="cronValue.week.loop.end"
                :options="
                  data.week.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }))
                "
              />
              执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.week.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.week.appoint"
                multiple
                class="w-full"
                :options="
                  data.week.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }))
                "
              />
            </NFormItem>
            <NFormItem v-if="cronValue.week.type === '4'" label="最后一周">
              <NSelect
                v-model:value="cronValue.week.last"
                :options="
                  data.week.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>

        <NTabPane name="year">
          <template #tab>
            <div class="sc-cron-num">
              <h2>年</h2>
              <h4>{{ value_year }}</h4>
            </div>
          </template>
          <NForm>
            <NFormItem label="类型">
              <NRadioGroup v-model:value="cronValue.year.type">
                <NRadioButton value="-1">忽略</NRadioButton>
                <NRadioButton value="0">任意值</NRadioButton>
                <NRadioButton value="1">范围</NRadioButton>
                <NRadioButton value="2">间隔</NRadioButton>
                <NRadioButton value="3">指定</NRadioButton>
              </NRadioGroup>
            </NFormItem>
            <NFormItem v-if="cronValue.year.type === '1'" label="范围">
              <NInputNumber
                v-model:value="cronValue.year.range.start as number"
                controls-position="right"
              />
              <span class="px-4">-</span>
              <NInputNumber
                v-model:value="cronValue.year.range.end as number"
                controls-position="right"
              />
            </NFormItem>
            <NFormItem v-if="cronValue.year.type === '2'" label="间隔">
              <NInputNumber
                v-model:value="cronValue.year.loop.start as number"
                controls-position="right"
              />
              年开始，每
              <NInputNumber
                v-model:value="cronValue.year.loop.end as number"
                :min="1"
                controls-position="right"
              />
              年执行一次
            </NFormItem>
            <NFormItem v-if="cronValue.year.type === '3'" label="指定">
              <NSelect
                v-model:value="cronValue.year.appoint"
                multiple
                class="w-full"
                :options="
                  data.year.map((item) => ({
                    label: item.toString(),
                    value: item.toString(),
                  }))
                "
              />
            </NFormItem>
          </NForm>
        </NTabPane>
      </NTabs>
    </div>

    <template #footer>
      <NButton @click="dialogVisible = false">取 消</NButton>
      <NButton type="primary" @click="submit()">确 认</NButton>
    </template>
  </NModal>
</template>

<style scoped>
.sc-cron {
  width: 100%;
  height: 100%;
}
</style>
