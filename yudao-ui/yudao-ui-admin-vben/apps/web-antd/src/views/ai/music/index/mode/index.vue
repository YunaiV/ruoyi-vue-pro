<script lang="ts" setup>
import type { Nullable, Recordable } from '@vben/types';

import { ref, unref } from 'vue';

import { Button, Card, Radio } from 'ant-design-vue';

import desc from './desc.vue';
import lyric from './lyric.vue';

defineOptions({ name: 'AiMusicModeIndex' });

const emits = defineEmits(['generateMusic']);

const generateMode = ref('lyric');

const modeRef = ref<Nullable<{ formData: Recordable<any> }>>(null);

/*
 *@Description: 根据信息生成音乐
 *@MethodAuthor: xiaohong
 *@Date: 2024-06-27 16:40:16
 */
function generateMusic() {
  emits('generateMusic', { formData: unref(modeRef)?.formData });
}
</script>

<template>
  <Card class="!mb-0 h-full w-80">
    <Radio.Group v-model:value="generateMode" class="mb-4">
      <Radio.Button value="desc"> 描述模式 </Radio.Button>
      <Radio.Button value="lyric"> 歌词模式 </Radio.Button>
    </Radio.Group>

    <!-- 描述模式/歌词模式 切换 -->
    <component :is="generateMode === 'desc' ? desc : lyric" ref="modeRef" />

    <Button type="primary" shape="round" class="w-full" @click="generateMusic">
      创作音乐
    </Button>
  </Card>
</template>
