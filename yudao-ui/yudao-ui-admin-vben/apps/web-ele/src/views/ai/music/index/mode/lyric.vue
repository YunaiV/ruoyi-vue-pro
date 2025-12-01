<script lang="ts" setup>
import { reactive, ref } from 'vue';

import {
  ElButton,
  ElInput,
  ElOption,
  ElSelect,
  ElSpace,
  ElTag,
} from 'element-plus';

import Title from '../title/index.vue';

defineOptions({ name: 'AiMusicModeLyric' });

const tags = ['rock', 'punk', 'jazz', 'soul', 'country', 'kidsmusic', 'pop'];

const showCustom = ref(false);

const formData = reactive({
  lyric: '',
  style: '',
  name: '',
  version: '',
});

defineExpose({
  formData,
});
</script>

<template>
  <div class="">
    <Title title="歌词" desc="自己编写歌词或使用Ai生成歌词，两节/8行效果最佳">
      <ElInput
        v-model="formData.lyric"
        type="textarea"
        :autosize="{ minRows: 6, maxRows: 6 }"
        :maxlength="1200"
        :show-word-limit="true"
        placeholder="请输入您自己的歌词"
      />
    </Title>

    <Title title="音乐风格">
      <ElSpace class="flex-wrap">
        <ElTag v-for="tag in tags" :key="tag" class="mb-2">
          {{ tag }}
        </ElTag>
      </ElSpace>

      <ElButton
        :type="showCustom ? 'primary' : 'default'"
        round
        size="small"
        class="mb-2"
        @click="showCustom = !showCustom"
      >
        自定义风格
      </ElButton>
    </Title>

    <Title
      v-show="showCustom"
      desc="描述您想要的音乐风格，Suno无法识别艺术家的名字，但可以理解流派和氛围"
      class="mt-3"
    >
      <ElInput
        v-model="formData.style"
        type="textarea"
        :autosize="{ minRows: 4, maxRows: 4 }"
        :maxlength="256"
        show-word-limit
        placeholder="输入音乐风格(英文)"
      />
    </Title>

    <Title title="音乐/歌曲名称">
      <ElInput
        class="w-full"
        v-model="formData.name"
        placeholder="请输入音乐/歌曲名称"
      />
    </Title>

    <Title title="版本">
      <ElSelect v-model="formData.version" class="w-full" placeholder="请选择">
        <ElOption
          v-for="item in [
            {
              value: '3',
              label: 'V3',
            },
            {
              value: '2',
              label: 'V2',
            },
          ]"
          :key="item.value"
          :value="item.value"
          :label="item.label"
        />
      </ElSelect>
    </Title>
  </div>
</template>
