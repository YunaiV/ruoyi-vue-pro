<script setup>
import { ref, watch } from 'vue';

import {
  Button,
  Checkbox,
  DatePicker,
  Input,
  InputNumber,
  Radio,
  Tabs,
} from 'ant-design-vue';

const props = defineProps({
  value: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['change']);

const tab = ref('cron');
const cronStr = ref(props.value || '* * * * * ?');
const fields = ref({
  second: '*',
  minute: '*',
  hour: '*',
  day: '*',
  month: '*',
  week: '?',
  year: '',
});
const cronFieldList = [
  { key: 'second', label: '秒', min: 0, max: 59 },
  { key: 'minute', label: '分', min: 0, max: 59 },
  { key: 'hour', label: '时', min: 0, max: 23 },
  { key: 'day', label: '天', min: 1, max: 31 },
  { key: 'month', label: '月', min: 1, max: 12 },
  { key: 'week', label: '周', min: 1, max: 7 },
  { key: 'year', label: '年', min: 1970, max: 2099 },
];
const activeField = ref('second');
const cronMode = ref({
  second: 'appoint',
  minute: 'every',
  hour: 'every',
  day: 'every',
  month: 'every',
  week: 'every',
  year: 'every',
});
const cronAppoint = ref({
  second: ['00', '01'],
  minute: [],
  hour: [],
  day: [],
  month: [],
  week: [],
  year: [],
});
const cronRange = ref({
  second: [0, 1],
  minute: [0, 1],
  hour: [0, 1],
  day: [1, 2],
  month: [1, 2],
  week: [1, 2],
  year: [1970, 1971],
});
const cronStep = ref({
  second: [1, 1],
  minute: [1, 1],
  hour: [1, 1],
  day: [1, 1],
  month: [1, 1],
  week: [1, 1],
  year: [1970, 1],
});

function pad(n) {
  return n < 10 ? `0${n}` : `${n}`;
}

watch(
  [fields, cronMode, cronAppoint, cronRange, cronStep],
  () => {
    // 组装cron表达式
    const arr = cronFieldList.map((f) => {
      if (cronMode.value[f.key] === 'every') return '*';
      if (cronMode.value[f.key] === 'appoint')
        return cronAppoint.value[f.key].join(',') || '*';
      if (cronMode.value[f.key] === 'range')
        return `${cronRange.value[f.key][0]}-${cronRange.value[f.key][1]}`;
      if (cronMode.value[f.key] === 'step')
        return `${cronStep.value[f.key][0]}/${cronStep.value[f.key][1]}`;
      return fields.value[f.key] || '*';
    });
    // week和year特殊处理
    arr[5] = arr[5] || '?';
    cronStr.value = arr.join(' ');
    if (tab.value === 'cron') emit('change', cronStr.value);
  },
  { deep: true },
);

// 标准格式
const isoStr = ref('');
const repeat = ref(1);
const isoDate = ref('');
const isoDuration = ref('');
function setDuration(type, val) {
  // 组装ISO 8601字符串
  let d = isoDuration.value;
  if (d.includes(type)) {
    d = d.replace(new RegExp(`\\d+${type}`), val + type);
  } else {
    d += val + type;
  }
  isoDuration.value = d;
  updateIsoStr();
}
function updateIsoStr() {
  let str = `R${repeat.value}`;
  if (isoDate.value)
    str += `/${
      typeof isoDate.value === 'string'
        ? isoDate.value
        : new Date(isoDate.value).toISOString()
    }`;
  if (isoDuration.value) str += `/${isoDuration.value}`;
  isoStr.value = str;
  if (tab.value === 'iso') emit('change', isoStr.value);
}
watch([repeat, isoDate, isoDuration], updateIsoStr);
watch(
  () => props.value,
  (val) => {
    if (!val) return;
    if (tab.value === 'cron') cronStr.value = val;
    if (tab.value === 'iso') isoStr.value = val;
  },
  { immediate: true },
);
</script>
<template>
  <Tabs v-model:active-key="tab">
    <Tabs.TabPane key="cron" tab="CRON表达式">
      <div style="margin-bottom: 10px">
        <Input
          v-model:value="cronStr"
          readonly
          style="width: 400px; font-weight: bold"
          key="cronStr"
        />
      </div>
      <div style="display: flex; gap: 8px; margin-bottom: 8px">
        <Input
          v-model:value="fields.second"
          placeholder="秒"
          style="width: 80px"
          key="second"
        />
        <Input
          v-model:value="fields.minute"
          placeholder="分"
          style="width: 80px"
          key="minute"
        />
        <Input
          v-model:value="fields.hour"
          placeholder="时"
          style="width: 80px"
          key="hour"
        />
        <Input
          v-model:value="fields.day"
          placeholder="天"
          style="width: 80px"
          key="day"
        />
        <Input
          v-model:value="fields.month"
          placeholder="月"
          style="width: 80px"
          key="month"
        />
        <Input
          v-model:value="fields.week"
          placeholder="周"
          style="width: 80px"
          key="week"
        />
        <Input
          v-model:value="fields.year"
          placeholder="年"
          style="width: 80px"
          key="year"
        />
      </div>
      <Tabs
        v-model:active-key="activeField"
        type="card"
        style="margin-bottom: 8px"
      >
        <Tabs.TabPane v-for="f in cronFieldList" :key="f.key" :tab="f.label">
          <div style="margin-bottom: 8px">
            <Radio.Group
              v-model:value="cronMode[f.key]"
              :key="`radio-${f.key}`"
            >
              <Radio value="every" :key="`every-${f.key}`">
                每{{ f.label }}
              </Radio>
              <Radio value="range" :key="`range-${f.key}`">
                从
                <InputNumber
                  v-model:value="cronRange[f.key][0]"
                  :min="f.min"
                  :max="f.max"
                  size="small"
                  style="width: 60px"
                  :key="`range0-${f.key}`"
                />
                到
                <InputNumber
                  v-model:value="cronRange[f.key][1]"
                  :min="f.min"
                  :max="f.max"
                  size="small"
                  style="width: 60px"
                  :key="`range1-${f.key}`"
                />
                之间每{{ f.label }}
              </Radio>
              <Radio value="step" :key="`step-${f.key}`">
                从第
                <InputNumber
                  v-model:value="cronStep[f.key][0]"
                  :min="f.min"
                  :max="f.max"
                  size="small"
                  style="width: 60px"
                  :key="`step0-${f.key}`"
                />
                开始每
                <InputNumber
                  v-model:value="cronStep[f.key][1]"
                  :min="1"
                  :max="f.max"
                  size="small"
                  style="width: 60px"
                  :key="`step1-${f.key}`"
                />
                {{ f.label }}
              </Radio>
              <Radio value="appoint" :key="`appoint-${f.key}`"> 指定 </Radio>
            </Radio.Group>
          </div>
          <div v-if="cronMode[f.key] === 'appoint'">
            <Checkbox.Group
              v-model:value="cronAppoint[f.key]"
              :key="`group-${f.key}`"
            >
              <Checkbox
                v-for="n in f.max + 1"
                :key="`cb-${f.key}-${n - 1}`"
                :value="pad(n - 1)"
              >
                {{ pad(n - 1) }}
              </Checkbox>
            </Checkbox.Group>
          </div>
        </Tabs.TabPane>
      </Tabs>
    </Tabs.TabPane>
    <Tabs.TabPane key="iso" title="标准格式" tab="iso-tab">
      <div style="margin-bottom: 10px">
        <Input
          v-model:value="isoStr"
          placeholder="如R1/2025-05-21T21:59:54/P3DT30M30S"
          style="width: 400px; font-weight: bold"
          key="isoStr"
        />
      </div>
      <div style="margin-bottom: 10px">
        循环次数：<InputNumber
          v-model:value="repeat"
          :min="1"
          style="width: 100px"
          key="repeat"
        />
      </div>
      <div style="margin-bottom: 10px">
        日期时间：<DatePicker
          v-model:value="isoDate"
          show-time
          placeholder="选择日期时间"
          style="width: 200px"
          key="isoDate"
        />
      </div>
      <div style="margin-bottom: 10px">
        当前时长：<Input
          v-model:value="isoDuration"
          placeholder="如P3DT30M30S"
          style="width: 200px"
          key="isoDuration"
        />
      </div>
      <div>
        <div>
          秒：
          <Button
            v-for="s in [5, 10, 30, 50]"
            @click="setDuration('S', s)"
            :key="`sec-${s}`"
          >
            {{ s }}
          </Button>
          自定义
        </div>
        <div>
          分：
          <Button
            v-for="m in [5, 10, 30, 50]"
            @click="setDuration('M', m)"
            :key="`min-${m}`"
          >
            {{ m }}
          </Button>
          自定义
        </div>
        <div>
          小时：
          <Button
            v-for="h in [4, 8, 12, 24]"
            @click="setDuration('H', h)"
            :key="`hour-${h}`"
          >
            {{ h }}
          </Button>
          自定义
        </div>
        <div>
          天：
          <Button
            v-for="d in [1, 2, 3, 4]"
            @click="setDuration('D', d)"
            :key="`day-${d}`"
          >
            {{ d }}
          </Button>
          自定义
        </div>
        <div>
          月：
          <Button
            v-for="mo in [1, 2, 3, 4]"
            @click="setDuration('M', mo)"
            :key="`mon-${mo}`"
          >
            {{ mo }}
          </Button>
          自定义
        </div>
        <div>
          年：
          <Button
            v-for="y in [1, 2, 3, 4]"
            @click="setDuration('Y', y)"
            :key="`year-${y}`"
          >
            {{ y }}
          </Button>
          自定义
        </div>
      </div>
    </Tabs.TabPane>
  </Tabs>
</template>
