<script lang="ts" setup>
import type { CrmProductApi } from '#/api/crm/product';

import { useDescription } from '#/components/description';

import { useDetailBaseSchema } from '../data';

defineProps<{
  product: CrmProductApi.Product;
}>();

const [ProductDescriptions] = useDescription({
  bordered: false,
  column: 4,
  schema: useDetailBaseSchema(),
});
</script>

<template>
  <div>
    <ProductDescriptions :data="product" />
  </div>
</template>
