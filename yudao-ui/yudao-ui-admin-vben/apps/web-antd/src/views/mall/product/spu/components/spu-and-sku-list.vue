<script generic="T extends MallSpuApi.Spu" lang="ts" setup>
import type { MallSpuApi, RuleConfig, SpuProperty } from './type';

import { ref, watch } from 'vue';

import { confirm } from '@vben/common-ui';
import { formatToFraction } from '@vben/utils';

import { Button, Image } from 'ant-design-vue';

import { VxeColumn, VxeTable } from '#/adapter/vxe-table';

import SkuList from './sku-list.vue';

defineOptions({ name: 'PromotionSpuAndSkuList' });

const props = withDefaults(
  defineProps<{
    deletable?: boolean; // SPU 是否可删除
    ruleConfig: RuleConfig[];
    spuList: T[];
    spuPropertyListP: SpuProperty<T>[];
  }>(),
  {
    deletable: false,
  },
);

const emit = defineEmits<{
  (e: 'delete', spuId: number): void;
}>();

const spuData = ref<MallSpuApi.Spu[]>([]); // spu 详情数据列表
const skuListRef = ref<InstanceType<typeof SkuList> | undefined>(); // 商品属性列表Ref
const spuPropertyList = ref<SpuProperty<T>[]>([]); // spuId 对应的 sku 的属性列表
const expandRowKeys = ref<string[]>([]); // 控制展开行需要设置 row-key 属性才能使用，该属性为展开行的 keys 数组。

/**
 * 获取所有 sku 活动配置
 *
 * @param extendedAttribute 在 sku 上扩展的属性，例：秒杀活动 sku 扩展属性 productConfig 请参考 seckillActivity.ts
 */
function getSkuConfigs(extendedAttribute: string) {
  // 验证 SKU 数据（如果有 ref 的话）
  if (skuListRef.value) {
    skuListRef.value.validateSku();
  }
  const seckillProducts: unknown[] = [];
  spuPropertyList.value.forEach((item) => {
    item.spuDetail.skus?.forEach((sku) => {
      const extendedValue = (sku as Record<string, unknown>)[extendedAttribute];
      if (extendedValue) {
        seckillProducts.push(extendedValue);
      }
    });
  });
  return seckillProducts;
}

defineExpose({ getSkuConfigs }); // 暴露出给表单提交时使用

/** 多选时可以删除 SPU */
async function deleteSpu(spuId: number) {
  await confirm(`是否删除商品编号为${spuId}的数据？`);
  const index = spuData.value.findIndex((item) => item.id === spuId);
  if (index !== -1) {
    spuData.value.splice(index, 1);
    emit('delete', spuId);
  }
}

/** 将传进来的值赋值给 spuData */
watch(
  () => props.spuList,
  (data) => {
    if (!data) return;
    spuData.value = data as MallSpuApi.Spu[];
  },
  {
    deep: true,
    immediate: true,
  },
);

/** 将传进来的值赋值给 spuPropertyList */
watch(
  () => props.spuPropertyListP,
  (data) => {
    if (!data) return;
    spuPropertyList.value = data as SpuProperty<T>[];
    // 解决如果之前选择的是单规格 spu 的话后面选择多规格 sku 多规格属性信息不展示的问题。解决方法：让 SkuList 组件重新渲染（行折叠会干掉包含的组件展开时会重新加载）
    setTimeout(() => {
      expandRowKeys.value = data.map((item) => String(item.spuId));
    }, 200);
  },
  {
    deep: true,
    immediate: true,
  },
);
</script>

<template>
  <VxeTable
    :data="spuData"
    :expand-row-keys="expandRowKeys"
    :row-config="{
      keyField: 'id',
    }"
  >
    <VxeColumn type="expand" width="30">
      <template #content="{ row }">
        <SkuList
          ref="skuListRef"
          :is-activity-component="true"
          :prop-form-data="
            spuPropertyList.find((item) => item.spuId === row.id)?.spuDetail
          "
          :property-list="
            spuPropertyList.find((item) => item.spuId === row.id)?.propertyList
          "
          :rule-config="ruleConfig"
        >
          <template #extension>
            <slot></slot>
          </template>
        </SkuList>
      </template>
    </VxeColumn>
    <VxeColumn field="id" align="center" title="商品编号" />
    <VxeColumn title="商品图" min-width="80">
      <template #default="{ row }">
        <Image
          v-if="row.picUrl"
          :src="row.picUrl"
          class="h-[30px] w-[30px] cursor-pointer"
          :preview="true"
        />
      </template>
    </VxeColumn>
    <VxeColumn
      field="name"
      title="商品名称"
      min-width="300"
      show-overflow="tooltip"
    />
    <VxeColumn align="center" title="商品售价" min-width="90">
      <template #default="{ row }">
        {{ formatToFraction(row.price) }}
      </template>
    </VxeColumn>
    <VxeColumn field="salesCount" align="center" title="销量" min-width="90" />
    <VxeColumn field="stock" align="center" title="库存" min-width="90" />
    <VxeColumn
      v-if="spuData.length > 1 && deletable"
      align="center"
      title="操作"
      min-width="90"
    >
      <template #default="{ row }">
        <Button type="link" danger @click="deleteSpu(row.id)"> 删除</Button>
      </template>
    </VxeColumn>
  </VxeTable>
</template>
