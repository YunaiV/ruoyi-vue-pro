<!-- 商品橱窗组件：用于展示和选择商品 SPU -->
<script lang="ts" setup>
import type { MallSpuApi } from '#/api/mall/product/spu';

import { computed, ref, watch } from 'vue';

import { CloseCircleFilled, PlusOutlined } from '@vben/icons';

import { Image, Tooltip } from 'ant-design-vue';

import { getSpuDetailList } from '#/api/mall/product/spu';

import SpuTableSelect from './spu-table-select.vue';

interface SpuShowcaseProps {
  modelValue?: number | number[];
  limit?: number;
  disabled?: boolean;
}

const props = withDefaults(defineProps<SpuShowcaseProps>(), {
  modelValue: undefined,
  limit: Number.MAX_VALUE,
  disabled: false,
});

const emit = defineEmits(['update:modelValue', 'change']);

const productSpus = ref<MallSpuApi.Spu[]>([]); // 已选择的商品列表
const spuTableSelectRef = ref<InstanceType<typeof SpuTableSelect>>(); // 商品选择表格组件引用
const isMultiple = computed(() => props.limit !== 1); // 是否为多选模式

/** 计算是否可以添加 */
const canAdd = computed(() => {
  if (props.disabled) {
    return false;
  }
  if (!props.limit) {
    return true;
  }
  return productSpus.value.length < props.limit;
});

/** 监听 modelValue 变化，加载商品详情 */
watch(
  () => props.modelValue,
  async (newValue) => {
    // eslint-disable-next-line unicorn/no-nested-ternary
    const ids = Array.isArray(newValue) ? newValue : newValue ? [newValue] : [];
    if (ids.length === 0) {
      productSpus.value = [];
      return;
    }
    // 只有商品发生变化时才重新查询
    if (
      productSpus.value.length === 0 ||
      productSpus.value.some((spu) => !ids.includes(spu.id!))
    ) {
      productSpus.value = await getSpuDetailList(ids);
    }
  },
  { immediate: true },
);

/** 打开商品选择对话框 */
function handleOpenSpuSelect() {
  spuTableSelectRef.value?.open(productSpus.value);
}

/** 选择商品后触发 */
function handleSpuSelected(spus: MallSpuApi.Spu | MallSpuApi.Spu[]) {
  productSpus.value = Array.isArray(spus) ? spus : [spus];
  emitSpuChange();
}

/** 删除商品 */
function handleRemoveSpu(index: number) {
  productSpus.value.splice(index, 1);
  emitSpuChange();
}

/** 触发变更事件 */
function emitSpuChange() {
  if (props.limit === 1) {
    const spu = productSpus.value.length > 0 ? productSpus.value[0] : null;
    emit('update:modelValue', spu?.id || 0);
    emit('change', spu);
  } else {
    emit(
      'update:modelValue',
      productSpus.value.map((spu) => spu.id!),
    );
    emit('change', productSpus.value);
  }
}
</script>

<template>
  <div class="flex flex-wrap items-center gap-2">
    <!-- 已选商品列表 -->
    <div
      v-for="(spu, index) in productSpus"
      :key="spu.id"
      class="group relative h-[60px] w-[60px] overflow-hidden rounded-lg"
    >
      <Tooltip :title="spu.name">
        <div class="relative h-full w-full">
          <Image
            :src="spu.picUrl"
            class="h-full w-full rounded-lg object-cover"
          />
          <!-- 删除按钮 -->
          <!-- TODO @AI：还是使用 IconifyIcon：使用自己的 + 图标 -->
          <CloseCircleFilled
            v-if="!disabled"
            class="absolute -right-2 -top-2 cursor-pointer text-xl text-red-500 opacity-0 transition-opacity hover:text-red-600 group-hover:opacity-100"
            @click="handleRemoveSpu(index)"
          />
        </div>
      </Tooltip>
    </div>

    <!-- 添加商品按钮 -->
    <Tooltip v-if="canAdd" title="选择商品">
      <div
        class="flex h-[60px] w-[60px] cursor-pointer items-center justify-center rounded-lg border-2 border-dashed transition-colors hover:border-primary hover:bg-primary/5"
        @click="handleOpenSpuSelect"
      >
        <!-- TODO @AI：还是使用 IconifyIcon：使用自己的 + 图标 -->
        <PlusOutlined class="text-xl text-gray-400" />
      </div>
    </Tooltip>
  </div>

  <!-- 商品选择对话框 -->
  <SpuTableSelect
    ref="spuTableSelectRef"
    :multiple="isMultiple"
    @change="handleSpuSelected"
  />
</template>
