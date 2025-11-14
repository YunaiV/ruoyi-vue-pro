<script lang="ts" setup>
import type { CodeEditorProps } from './types';

import { computed } from 'vue';

import { isString } from '@vben/utils';

import CodeMirrorEditor from './code-mirror.vue';
import { MODE } from './types';

const props = withDefaults(defineProps<CodeEditorProps>(), {
  value: '',
  mode: MODE.JSON,
  readonly: false,
  autoFormat: true,
  bordered: false,
});

const emit = defineEmits(['change', 'update:value', 'formatError']);

const getValue = computed(() => {
  const { value, mode, autoFormat } = props;
  if (!autoFormat || mode !== MODE.JSON) return value as string;

  let result = value;
  if (isString(value)) {
    try {
      result = JSON.parse(value);
    } catch {
      emit('formatError', value);
      return value as string;
    }
  }
  return JSON.stringify(result, null, 2);
});

function handleValueChange(v: string) {
  emit('update:value', v);
  emit('change', v);
}
</script>

<template>
  <div class="h-full">
    <CodeMirrorEditor
      :value="getValue"
      :mode="mode"
      :readonly="readonly"
      :bordered="bordered"
      :auto-format="autoFormat"
      @change="handleValueChange"
    />
  </div>
</template>
