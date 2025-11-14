<script lang="ts" setup>
import { reactive, ref } from 'vue';

import { Button, Input, Select, Space, Tag, Textarea } from 'ant-design-vue';

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
      <Textarea
        v-model:value="formData.lyric"
        :auto-size="{ minRows: 6, maxRows: 6 }"
        :maxlength="1200"
        :show-count="true"
        placeholder="请输入您自己的歌词"
      />
    </Title>

    <Title title="音乐风格">
      <Space class="flex-wrap">
        <Tag v-for="tag in tags" :key="tag" class="mb-2">
          {{ tag }}
        </Tag>
      </Space>

      <Button
        :type="showCustom ? 'primary' : 'default'"
        shape="round"
        size="small"
        class="mb-2"
        @click="showCustom = !showCustom"
      >
        自定义风格
      </Button>
    </Title>

    <Title
      v-show="showCustom"
      desc="描述您想要的音乐风格，Suno无法识别艺术家的名字，但可以理解流派和氛围"
      class="mt-3"
    >
      <Textarea
        v-model="formData.style"
        :auto-size="{ minRows: 4, maxRows: 4 }"
        :maxlength="256"
        show-count
        placeholder="输入音乐风格(英文)"
      />
    </Title>

    <Title title="音乐/歌曲名称">
      <Input
        class="w-full"
        v-model="formData.name"
        placeholder="请输入音乐/歌曲名称"
      />
    </Title>

    <Title title="版本">
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
