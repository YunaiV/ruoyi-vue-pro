<script lang="ts" setup>
import type { Nullable } from '@vben/types';

import type { CodeEditorProps } from './types';

import {
  nextTick,
  onMounted,
  onUnmounted,
  ref,
  unref,
  watch,
  watchEffect,
} from 'vue';

import { usePreferences } from '@vben/preferences';

import { useDebounceFn, useWindowSize } from '@vueuse/core';
import CodeMirror from 'codemirror';

import { MODE } from './types';

// modes
import 'codemirror/mode/javascript/javascript';
import 'codemirror/mode/css/css';
import 'codemirror/mode/htmlmixed/htmlmixed';

// css
import './codemirror.css';
import 'codemirror/theme/idea.css';
import 'codemirror/theme/material-palenight.css';

const props = withDefaults(defineProps<CodeEditorProps>(), {
  mode: MODE.JSON,
  value: '',
  readonly: false,
  bordered: false,
  autoFormat: true,
});

const emit = defineEmits(['change']);

const { isDark } = usePreferences();
const { width, height } = useWindowSize();

const el = ref();
let editor: Nullable<CodeMirror.Editor>;

const debounceRefresh = useDebounceFn(refresh, 100);

watch(
  () => props.value,
  async (value) => {
    await nextTick();
    const oldValue = editor?.getValue();
    if (value !== oldValue) editor?.setValue(value || '');
  },
  { flush: 'post' },
);

watchEffect(() => {
  editor?.setOption('mode', props.mode);
});

watch(
  () => isDark.value,
  async () => {
    setTheme();
  },
  {
    immediate: true,
  },
);

watch(
  () => [width.value, height.value],
  async () => {
    debounceRefresh();
  },
);

function setTheme() {
  unref(editor)?.setOption(
    'theme',
    isDark.value ? 'material-palenight' : 'idea',
  );
}

function refresh() {
  editor?.refresh();
}

async function init() {
  const addonOptions = {
    autoCloseBrackets: true,
    autoCloseTags: true,
    foldGutter: true,
    gutters: ['CodeMirror-linenumbers'],
  };

  editor = CodeMirror(el.value!, {
    value: '',
    mode: props.mode,
    readOnly: props.readonly,
    tabSize: 2,
    theme: 'material-palenight',
    lineWrapping: true,
    lineNumbers: true,
    ...addonOptions,
  });
  editor?.setValue(props.value);
  setTheme();
  editor?.on('change', () => {
    emit('change', editor?.getValue());
  });
}

onMounted(async () => {
  await nextTick();
  init();
});

onUnmounted(() => {
  editor = null;
});
</script>

<template>
  <div
    ref="el"
    class="relative !h-full w-full overflow-hidden"
    :class="{
      'ant-input': props.bordered,
      'css-dev-only-do-not-override-kqecok': props.bordered,
    }"
  ></div>
</template>
