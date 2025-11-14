<template>
  <!-- SKU 选择的提示框 -->
  <detail-cell label="选择" :value="value" />
</template>

<script setup>
  import { computed } from 'vue';
  import detailCell from './detail-cell.vue';

  const props = defineProps({
    modelValue: {
      type: Array,
      default() {
        return [];
      },
    },
    sku: {
      type: Object
    }
  });
  const value = computed(() => {
    if (!props.sku?.id) {
      return '请选择商品规格';
    }
    let str = '';
    props.sku.properties.forEach(property => {
      str += property.propertyName + ':' + property.valueName + ' ';
    });
    return str;
  });
</script>
