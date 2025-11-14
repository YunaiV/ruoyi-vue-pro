<!-- 告警配置组件 -->
<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Form, Select, Tag } from 'ant-design-vue';

import { getAlertConfigPage } from '#/api/iot/alert/config';

/** 告警配置组件 */
defineOptions({ name: 'AlertConfig' });

const props = defineProps<{
  modelValue?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value?: number): void;
}>();

const localValue = useVModel(props, 'modelValue', emit);

const loading = ref(false); // 加载状态
const alertConfigs = ref<any[]>([]); // 告警配置列表

/**
 * 处理选择变化事件
 * @param value 选中的值
 */
function handleChange(value?: any) {
  emit('update:modelValue', value);
}

/**
 * 加载告警配置列表
 */
async function loadAlertConfigs() {
  loading.value = true;
  try {
    const data = await getAlertConfigPage({
      pageNo: 1,
      pageSize: 100,
      enabled: true, // 只加载启用的配置
    });
    alertConfigs.value = data.list || [];
  } finally {
    loading.value = false;
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadAlertConfigs();
});
</script>

<template>
  <div class="w-full">
    <Form.Item label="告警配置" required>
      <Select
        v-model="localValue"
        placeholder="请选择告警配置"
        filterable
        clearable
        @change="handleChange"
        class="w-full"
        :loading="loading"
      >
        <Select.Option
          v-for="config in alertConfigs"
          :key="config.id"
          :label="config.name"
          :value="config.id"
        >
          <div class="flex items-center justify-between">
            <span>{{ config.name }}</span>
            <Tag :type="config.enabled ? 'success' : 'danger'" size="small">
              {{ config.enabled ? '启用' : '禁用' }}
            </Tag>
          </div>
        </Select.Option>
      </Select>
    </Form.Item>
  </div>
</template>
