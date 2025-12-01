<script setup lang="ts">
import { ref, watch } from 'vue';

import { MyProcessViewer } from '#/views/bpm/components/bpmn-process-designer/package';

defineOptions({ name: 'ProcessInstanceBpmnViewer' });

const props = withDefaults(
  defineProps<{
    bpmnXml?: string;
    loading?: boolean; // 是否加载中
    modelView?: Object;
  }>(),
  {
    loading: false,
    modelView: () => ({}),
    bpmnXml: '',
  },
);

// BPMN 流程图数据
const view = ref({
  bpmnXml: '',
});

/** 监控 modelView 更新 */
watch(
  () => props.modelView,
  async (newModelView) => {
    // 加载最新
    if (newModelView) {
      // @ts-ignore
      view.value = newModelView;
    }
  },
);

/** 监听 bpmnXml */
watch(
  () => props.bpmnXml,
  (value) => {
    view.value.bpmnXml = value;
  },
);
</script>

<template>
  <div
    v-loading="loading"
    class="h-full w-full overflow-auto rounded-lg border border-gray-200 bg-white p-4"
  >
    <MyProcessViewer
      key="processViewer"
      :xml="view.bpmnXml"
      :view="view"
      class="h-full min-h-[500px] w-full"
    />
  </div>
</template>
