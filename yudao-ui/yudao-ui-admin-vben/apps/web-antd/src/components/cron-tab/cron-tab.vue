<script lang="ts" setup>
import type { PropType } from 'vue';

import type { CronData, CronValue, ShortcutsType } from './types';

import { computed, onMounted, reactive, ref, watch } from 'vue';

import {
  Button,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  RadioButton,
  RadioGroup,
  Select,
  Tabs,
} from 'ant-design-vue';

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
  // 简单检查
  if (arr.length < 6) {
    message.warning('cron表达式错误，已转换为默认表达式');
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
  <Input
    v-model:value="defaultValue"
    class="input-with-select"
    v-bind="$attrs"
    @input="inputChange"
  >
    <template #addonAfter>
      <Select v-model:value="select" placeholder="生成器" class="w-36">
        <Select.Option value="0 * * * * ?">每分钟</Select.Option>
        <Select.Option value="0 0 * * * ?">每小时</Select.Option>
        <Select.Option value="0 0 0 * * ?">每天零点</Select.Option>
        <Select.Option value="0 0 0 1 * ?">每月一号零点</Select.Option>
        <Select.Option value="0 0 0 L * ?">每月最后一天零点</Select.Option>
        <Select.Option value="0 0 0 ? * 1">每周星期日零点</Select.Option>
        <Select.Option
          v-for="(item, index) in shortcuts"
          :key="index"
          :value="item.value"
        >
          {{ item.text }}
        </Select.Option>
        <Select.Option value="custom">自定义</Select.Option>
      </Select>
    </template>
  </Input>

  <Modal
    v-model:open="dialogVisible"
    :width="720"
    destroy-on-close
    title="cron规则生成器"
  >
    <div class="sc-cron">
      <Tabs>
        <Tabs.TabPane key="second">
          <template #tab>
            <div class="sc-cron-num">
              <h2>秒</h2>
              <h4>{{ value_second }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.second.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.second.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.second.range.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.second.range.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.second.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.second.loop.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒开始，每
              <InputNumber
                v-model:value="cronValue.second.loop.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
              秒执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.second.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.second.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.second"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="minute">
          <template #tab>
            <div class="sc-cron-num">
              <h2>分钟</h2>
              <h4>{{ value_minute }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.minute.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.minute.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.minute.range.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.minute.range.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.minute.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.minute.loop.start"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟开始，每
              <InputNumber
                v-model:value="cronValue.minute.loop.end"
                :max="59"
                :min="0"
                controls-position="right"
              />
              分钟执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.minute.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.minute.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.minute"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="hour">
          <template #tab>
            <div class="sc-cron-num">
              <h2>小时</h2>
              <h4>{{ value_hour }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.hour.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.hour.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.hour.range.start"
                :max="23"
                :min="0"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.hour.range.end"
                :max="23"
                :min="0"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.hour.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.hour.loop.start"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时开始，每
              <InputNumber
                v-model:value="cronValue.hour.loop.end"
                :max="23"
                :min="0"
                controls-position="right"
              />
              小时执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.hour.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.hour.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.hour"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="day">
          <template #tab>
            <div class="sc-cron-num">
              <h2>日</h2>
              <h4>{{ value_day }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.day.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
                <RadioButton value="4">本月最后一天</RadioButton>
                <RadioButton value="5">不指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.day.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.day.range.start"
                :max="31"
                :min="1"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.day.range.end"
                :max="31"
                :min="1"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.day.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.day.loop.start"
                :max="31"
                :min="1"
                controls-position="right"
              />
              号开始，每
              <InputNumber
                v-model:value="cronValue.day.loop.end"
                :max="31"
                :min="1"
                controls-position="right"
              />
              天执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.day.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.day.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.day"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="month">
          <template #tab>
            <div class="sc-cron-num">
              <h2>月</h2>
              <h4>{{ value_month }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.month.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.month.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.month.range.start"
                :max="12"
                :min="1"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.month.range.end"
                :max="12"
                :min="1"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.month.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.month.loop.start"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月开始，每
              <InputNumber
                v-model:value="cronValue.month.loop.end"
                :max="12"
                :min="1"
                controls-position="right"
              />
              月执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.month.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.month.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.month"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="week">
          <template #tab>
            <div class="sc-cron-num">
              <h2>周</h2>
              <h4>{{ value_week }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.week.type">
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
                <RadioButton value="4">本月最后一周</RadioButton>
                <RadioButton value="5">不指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.week.type === '1'" label="范围">
              <Select v-model:value="cronValue.week.range.start">
                <Select.Option
                  v-for="(item, index) in data.week"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </Select>
              <span style="padding: 0 15px">-</span>
              <Select v-model:value="cronValue.week.range.end">
                <Select.Option
                  v-for="(item, index) in data.week"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </Select>
            </Form.Item>
            <Form.Item v-if="cronValue.week.type === '2'" label="间隔">
              第
              <InputNumber
                v-model:value="cronValue.week.loop.start"
                :max="4"
                :min="1"
                controls-position="right"
              />
              周的星期
              <Select v-model:value="cronValue.week.loop.end">
                <Select.Option
                  v-for="(item, index) in data.week"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </Select>
              执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.week.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.week.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.week"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </Select>
            </Form.Item>
            <Form.Item v-if="cronValue.week.type === '4'" label="最后一周">
              <Select v-model:value="cronValue.week.last">
                <Select.Option
                  v-for="(item, index) in data.week"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>

        <Tabs.TabPane key="year">
          <template #tab>
            <div class="sc-cron-num">
              <h2>年</h2>
              <h4>{{ value_year }}</h4>
            </div>
          </template>
          <Form>
            <Form.Item label="类型">
              <RadioGroup v-model:value="cronValue.year.type">
                <RadioButton value="-1">忽略</RadioButton>
                <RadioButton value="0">任意值</RadioButton>
                <RadioButton value="1">范围</RadioButton>
                <RadioButton value="2">间隔</RadioButton>
                <RadioButton value="3">指定</RadioButton>
              </RadioGroup>
            </Form.Item>
            <Form.Item v-if="cronValue.year.type === '1'" label="范围">
              <InputNumber
                v-model:value="cronValue.year.range.start"
                controls-position="right"
              />
              <span style="padding: 0 15px">-</span>
              <InputNumber
                v-model:value="cronValue.year.range.end"
                controls-position="right"
              />
            </Form.Item>
            <Form.Item v-if="cronValue.year.type === '2'" label="间隔">
              <InputNumber
                v-model:value="cronValue.year.loop.start"
                controls-position="right"
              />
              年开始，每
              <InputNumber
                v-model:value="cronValue.year.loop.end"
                :min="1"
                controls-position="right"
              />
              年执行一次
            </Form.Item>
            <Form.Item v-if="cronValue.year.type === '3'" label="指定">
              <Select
                v-model:value="cronValue.year.appoint"
                mode="multiple"
                style="width: 100%"
              >
                <Select.Option
                  v-for="(item, index) in data.year"
                  :key="index"
                  :label="item"
                  :value="item"
                />
              </Select>
            </Form.Item>
          </Form>
        </Tabs.TabPane>
      </Tabs>
    </div>

    <template #footer>
      <Button @click="dialogVisible = false">取 消</Button>
      <Button type="primary" @click="submit()">确 认</Button>
    </template>
  </Modal>
</template>

<style scoped>
.sc-cron :deep(.ant-tabs-tab) {
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
  background: hsl(var(--primary) / 10%);
  border-radius: 4px;
}

.sc-cron :deep(.ant-tabs-tab.ant-tabs-tab-active) .sc-cron-num h4 {
  color: #fff;
  background: hsl(var(--primary));
}

[data-theme='dark'] .sc-cron-num h4 {
  background: hsl(var(--white));
}

.input-with-select .ant-input-group-addon {
  background-color: hsl(var(--muted));
}
</style>
