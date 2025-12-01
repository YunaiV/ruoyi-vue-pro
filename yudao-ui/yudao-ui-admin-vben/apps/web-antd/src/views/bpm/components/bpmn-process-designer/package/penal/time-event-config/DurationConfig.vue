<script setup>
import { ref, watch } from 'vue';

import { Button, Input } from 'ant-design-vue';

const props = defineProps({
  value: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['change']);

const units = [
  { key: 'Y', label: '年', presets: [1, 2, 3, 4] },
  { key: 'M', label: '月', presets: [1, 2, 3, 4] },
  { key: 'D', label: '天', presets: [1, 2, 3, 4] },
  { key: 'H', label: '时', presets: [4, 8, 12, 24] },
  { key: 'm', label: '分', presets: [5, 10, 30, 50] },
  { key: 'S', label: '秒', presets: [5, 10, 30, 50] },
];
const custom = ref({ Y: '', M: '', D: '', H: '', m: '', S: '' });
const isoString = ref('');

function setUnit(key, val) {
  if (!val || Number.isNaN(val)) {
    custom.value[key] = '';
    return;
  }
  custom.value[key] = val;
  updateIsoString();
}

function updateIsoString() {
  let str = 'P';
  if (custom.value.Y) str += `${custom.value.Y}Y`;
  if (custom.value.M) str += `${custom.value.M}M`;
  if (custom.value.D) str += `${custom.value.D}D`;
  if (custom.value.H || custom.value.m || custom.value.S) str += 'T';
  if (custom.value.H) str += `${custom.value.H}H`;
  if (custom.value.m) str += `${custom.value.m}M`;
  if (custom.value.S) str += `${custom.value.S}S`;
  isoString.value = str === 'P' ? '' : str;
  emit('change', isoString.value);
}

watch(
  () => props.value,
  (val) => {
    if (!val) return;
    // 解析ISO 8601字符串到custom
    const match = val.match(
      /^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?$/,
    );
    if (match) {
      custom.value.Y = match[1] || '';
      custom.value.M = match[2] || '';
      custom.value.D = match[3] || '';
      custom.value.H = match[4] || '';
      custom.value.m = match[5] || '';
      custom.value.S = match[6] || '';
      updateIsoString();
    }
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <div style="margin-bottom: 10px">
      当前选择：<Input
        v-model:value="isoString"
        readonly
        style="width: 300px"
      />
    </div>
    <div v-for="unit in units" :key="unit.key" style="margin-bottom: 8px">
      <span>{{ unit.label }}：</span>
      <Button.Group>
        <Button
          v-for="val in unit.presets"
          :key="val"
          size="small"
          @click="setUnit(unit.key, val)"
        >
          {{ val }}
        </Button>
        <Input
          v-model:value="custom[unit.key]"
          size="small"
          style="width: 60px; margin-left: 8px"
          placeholder="自定义"
          @change="setUnit(unit.key, custom[unit.key])"
        />
      </Button.Group>
    </div>
  </div>
</template>
