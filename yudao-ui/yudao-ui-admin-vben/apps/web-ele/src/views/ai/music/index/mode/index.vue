<script lang="ts" setup>
import type { Nullable, Recordable } from '@vben/types';

import { ref, unref } from 'vue';

import { ElButton, ElCard, ElRadioButton, ElRadioGroup } from 'element-plus';

import desc from './desc.vue';
import lyric from './lyric.vue';

defineOptions({ name: 'AiMusicModeIndex' });

const emits = defineEmits(['generateMusic']);

const generateMode = ref('lyric');

const modeRef = ref<Nullable<{ formData: Recordable<any> }>>(null);

function generateMusic() {
  emits('generateMusic', { formData: unref(modeRef)?.formData });
}
</script>

<template>
  <ElCard class="!mb-0 h-full w-80">
    <ElRadioGroup v-model="generateMode" class="mb-4">
      <ElRadioButton value="desc"> 描述模式 </ElRadioButton>
      <ElRadioButton value="lyric"> 歌词模式 </ElRadioButton>
    </ElRadioGroup>

    <!-- 描述模式/歌词模式 切换 -->
    <component :is="generateMode === 'desc' ? desc : lyric" ref="modeRef" />

    <ElButton type="primary" round class="w-full" @click="generateMusic">
      创作音乐
    </ElButton>
  </ElCard>
</template>
