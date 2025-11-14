<script lang="ts" setup>
import { reactive } from 'vue';

import { Select, Switch, Textarea } from 'ant-design-vue';

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
      <Textarea
        v-model:value="formData.desc"
        :auto-size="{ minRows: 6, maxRows: 6 }"
        :maxlength="1200"
        :show-count="true"
        placeholder="一首关于糟糕分手的欢快歌曲"
      />
    </Title>

    <Title title="纯音乐" class="mt-5" desc="创建一首没有歌词的歌曲">
      <template #extra>
        <Switch v-model:checked="formData.pure" size="small" />
      </template>
    </Title>

    <Title
      title="版本"
      desc="描述您想要的音乐风格和主题，使用流派和氛围而不是特定的艺术家和歌曲"
    >
      <Select
        v-model:value="formData.version"
        class="w-full"
        placeholder="请选择"
      >
        <Select.Option
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
        >
          {{ item.label }}
        </Select.Option>
      </Select>
    </Title>
  </div>
</template>
