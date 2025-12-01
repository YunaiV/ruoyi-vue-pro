<script lang="ts" setup>
import { reactive } from 'vue';

import { ElInput, ElOption, ElSelect, ElSwitch } from 'element-plus';

import Title from '../title/index.vue';

defineOptions({ name: 'AiMusicModeDesc' });

const formData = reactive({
  desc: '',
  pure: false,
  version: '3',
});

defineExpose({
  formData,
});
</script>

<template>
  <div>
    <Title
      title="音乐/歌词说明"
      desc="描述您想要的音乐风格和主题，使用流派和氛围而不是特定的艺术家和歌曲"
    >
      <ElInput
        v-model="formData.desc"
        type="textarea"
        :autosize="{ minRows: 6, maxRows: 6 }"
        :maxlength="1200"
        :show-word-limit="true"
        placeholder="一首关于糟糕分手的欢快歌曲"
      />
    </Title>

    <Title title="纯音乐" class="mt-5" desc="创建一首没有歌词的歌曲">
      <template #extra>
        <ElSwitch v-model="formData.pure" size="small" />
      </template>
    </Title>

    <Title
      title="版本"
      desc="描述您想要的音乐风格和主题，使用流派和氛围而不是特定的艺术家和歌曲"
    >
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
