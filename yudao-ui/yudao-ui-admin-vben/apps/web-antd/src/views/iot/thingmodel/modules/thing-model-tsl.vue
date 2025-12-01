<script setup lang="ts">
import type { Ref } from 'vue';

import type { IotProductApi } from '#/api/iot/product/product';

import { computed, inject, ref, watch } from 'vue';

import { Modal, Radio, Textarea } from 'ant-design-vue';

import { getThingModelTSL } from '#/api/iot/thingmodel';
import { IOT_PROVIDE_KEY } from '#/views/iot/utils/constants';

defineOptions({ name: 'ThingModelTsl' });

const dialogVisible = ref(false); // 弹窗的是否展示
const dialogTitle = ref('物模型 TSL'); // 弹窗的标题
const product = inject<Ref<IotProductApi.Product>>(IOT_PROVIDE_KEY.PRODUCT); // 注入产品信息
const viewMode = ref('view'); // 查看模式：view-代码视图，editor-编辑器视图

/** 打开弹窗 */
async function open() {
  dialogVisible.value = true;
  await getTsl();
}
defineExpose({ open });

/** 获取 TSL */
const thingModelTSL = ref<any>({});
const tslString = ref(''); // 用于编辑器的字符串格式

async function getTsl() {
  try {
    thingModelTSL.value = await getThingModelTSL(product?.value?.id || 0);
    // 将对象转换为格式化的 JSON 字符串
    tslString.value = JSON.stringify(thingModelTSL.value, null, 2);
  } catch (error) {
    console.error('获取 TSL 失败:', error);
    thingModelTSL.value = {};
    tslString.value = '{}';
  }
}

/** 格式化的 TSL 用于只读展示 */
const formattedTSL = computed(() => {
  try {
    if (typeof thingModelTSL.value === 'string') {
      return JSON.stringify(JSON.parse(thingModelTSL.value), null, 2);
    }
    return JSON.stringify(thingModelTSL.value, null, 2);
  } catch {
    return JSON.stringify(thingModelTSL.value, null, 2);
  }
});

/** 监听编辑器内容变化，实时更新数据 */
watch(tslString, (newValue) => {
  try {
    thingModelTSL.value = JSON.parse(newValue);
  } catch {
    // JSON 解析失败时保持原值
  }
});
</script>

<template>
  <Modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    :footer="null"
    width="800px"
  >
    <div class="mb-4">
      <Radio.Group v-model:value="viewMode" size="small">
        <Radio.Button value="view">代码视图</Radio.Button>
        <Radio.Button value="editor">编辑器视图</Radio.Button>
      </Radio.Group>
    </div>
    <!-- 代码视图 - 只读展示 -->
    <div v-if="viewMode === 'view'" class="json-viewer-container">
      <pre class="json-code"><code>{{ formattedTSL }}</code></pre>
    </div>
    <!-- 编辑器视图 - 可编辑 -->
    <Textarea
      v-else
      v-model:value="tslString"
      :rows="20"
      placeholder="请输入 JSON 格式的物模型 TSL"
      class="json-editor"
    />
  </Modal>
</template>

<style scoped>
.json-viewer-container {
  max-height: 600px;
  padding: 12px;
  overflow-y: auto;
  background-color: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.json-code {
  margin: 0;
  font-family: Monaco, Menlo, 'Ubuntu Mono', Consolas, monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.json-editor {
  font-family: Monaco, Menlo, 'Ubuntu Mono', Consolas, monospace;
  font-size: 13px;
}
</style>
