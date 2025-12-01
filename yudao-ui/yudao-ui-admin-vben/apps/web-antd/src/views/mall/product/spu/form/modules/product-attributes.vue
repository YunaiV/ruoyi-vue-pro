<!-- 商品发布 - 库存价格 - 属性列表 -->
<script lang="ts" setup>
import type { MallPropertyApi } from '#/api/mall/product/property';
import type { PropertyAndValues } from '#/views/mall/product/spu/components';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Col, Divider, message, Select, Tag } from 'ant-design-vue';

import {
  createPropertyValue,
  getPropertyValueSimpleList,
} from '#/api/mall/product/property';
import { $t } from '#/locales';

defineOptions({ name: 'ProductAttributes' });

const props = withDefaults(defineProps<Props>(), {
  propertyList: () => [],
  isDetail: false,
});

const emit = defineEmits(['success']);

interface Props {
  propertyList?: PropertyAndValues[];
  isDetail?: boolean;
}

const inputValue = ref<string[]>([]); // 输入框值（tags 模式使用数组）
const attributeIndex = ref<null | number>(null); // 获取焦点时记录当前属性项的 index
const inputVisible = computed(() => (index: number) => {
  if (attributeIndex.value === null) {
    return false;
  }
  if (attributeIndex.value === index) {
    return true;
  }
}); // 输入框显隐控制

interface InputRefItem {
  inputRef?: {
    attributes: {
      id: string;
    };
  };
  focus: () => void;
}

const inputRef = ref<InputRefItem[]>([]); // 标签输入框 Ref
const attributeList = ref<PropertyAndValues[]>([]); // 商品属性列表
const attributeOptions = ref<MallPropertyApi.PropertyValue[]>([]); // 商品属性值下拉框

/** 解决 ref 在 v-for 中的获取问题*/
function setInputRef(el: any) {
  if (el === null || el === undefined) return;
  // 如果不存在 id 相同的元素才添加
  if (
    !inputRef.value.some(
      (item) => item.inputRef?.attributes.id === el.inputRef?.attributes.id,
    )
  ) {
    inputRef.value.push(el);
  }
}

watch(
  () => props.propertyList,
  (data) => {
    if (!data) {
      return;
    }
    attributeList.value = data;
  },
  {
    deep: true,
    immediate: true,
  },
);

/** 删除属性值 */
function handleCloseValue(index: number, value: PropertyAndValues) {
  if (attributeList.value[index]?.values) {
    attributeList.value[index].values = attributeList.value[
      index
    ].values?.filter((item) => item.id !== value.id);
  }
}

/** 删除属性 */
function handleCloseProperty(item: PropertyAndValues) {
  attributeList.value = attributeList.value.filter(
    (attribute) => attribute.id !== item.id,
  );
  emit('success', attributeList.value);
}

/** 显示输入框并获取焦点 */
async function showInput(index: number) {
  attributeIndex.value = index;
  inputRef.value?.[index]?.focus();
  // 获取属性下拉选项
  await getAttributeOptions(attributeList.value?.[index]?.id!);
}

/** 定义 success 事件，用于操作成功后的回调 */
async function handleInputConfirm(index: number, propertyId: number) {
  // 从数组中取最后一个输入的值（tags 模式下 inputValue 是数组）
  const currentValue = inputValue.value?.[inputValue.value.length - 1]?.trim();

  if (currentValue) {
    // 1. 重复添加校验
    if (
      attributeList.value?.[index]?.values?.find(
        (item) => item.name === currentValue,
      )
    ) {
      message.warning('已存在相同属性值，请重试');
      attributeIndex.value = null;
      inputValue.value = [];
      return;
    }

    // 2.1 情况一：属性值已存在，则直接使用并结束
    const existValue = attributeOptions.value.find(
      (item) => item.name === currentValue,
    );
    if (existValue) {
      attributeIndex.value = null;
      inputValue.value = [];
      attributeList.value?.[index]?.values?.push({
        id: existValue.id!,
        name: existValue.name,
      });
      emit('success', attributeList.value);
      return;
    }

    // 2.2 情况二：新属性值，则进行保存
    try {
      const id = await createPropertyValue({
        propertyId,
        name: currentValue,
      });
      attributeList.value?.[index]?.values?.push({
        id,
        name: currentValue,
      });
      message.success($t('ui.actionMessage.operationSuccess'));
      emit('success', attributeList.value);
    } catch {
      message.error($t('ui.actionMessage.operationFailed'));
    }
  }
  attributeIndex.value = null;
  inputValue.value = [];
}

/** 获取商品属性下拉选项 */
async function getAttributeOptions(propertyId: number) {
  attributeOptions.value = await getPropertyValueSimpleList(propertyId);
}
</script>

<template>
  <Col v-for="(attribute, index) in attributeList" :key="index">
    <Divider class="my-3" />
    <div class="mt-2 flex flex-wrap items-center gap-2">
      <span class="mx-1">属性名：</span>
      <Tag
        :closable="!isDetail"
        class="mx-1"
        color="success"
        @close="handleCloseProperty(attribute)"
      >
        {{ attribute.name }}
      </Tag>
    </div>
    <div class="mt-2 flex flex-wrap items-center gap-2">
      <span class="mx-1">属性值：</span>
      <Tag
        v-for="(value, valueIndex) in attribute.values"
        :key="valueIndex"
        :closable="!isDetail"
        class="mx-1"
        @close="handleCloseValue(index, value)"
      >
        {{ value?.name }}
      </Tag>
      <Select
        v-show="inputVisible(index)"
        :id="`input${index}`"
        :ref="setInputRef"
        v-model:value="inputValue"
        allow-clear
        mode="tags"
        :max-tag-count="1"
        :filter-option="true"
        size="small"
        style="width: 100px"
        @blur="handleInputConfirm(index, attribute.id)"
        @change="handleInputConfirm(index, attribute.id)"
        @keyup.enter="handleInputConfirm(index, attribute.id)"
      >
        <Select.Option
          v-for="item2 in attributeOptions"
          :key="item2.id"
          :value="item2.name"
        >
          {{ item2.name }}
        </Select.Option>
      </Select>
      <Tag
        v-show="!inputVisible(index)"
        @click="showInput(index)"
        class="mx-1 border-dashed bg-gray-100"
      >
        <div class="flex items-center">
          <IconifyIcon class="mr-2" icon="lucide:plus" />
          添加
        </div>
      </Tag>
    </div>
  </Col>
</template>
