<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import {
  MarkdownIt,
  Markmap,
  Toolbar,
  Transformer,
} from '@vben/plugins/markmap';
import { downloadImageByCanvas } from '@vben/utils';

import { Button, Card, message } from 'ant-design-vue';

const props = defineProps<{
  generatedContent: string; // 生成结果
  isEnd: boolean; // 是否结束
  isGenerating: boolean; // 是否正在生成
  isStart: boolean; // 开始状态，开始时需要清除 html
}>();
const md = MarkdownIt();
const contentRef = ref<HTMLDivElement>(); // 右侧出来 header 以下的区域
const mdContainerRef = ref<HTMLDivElement>(); // markdown 的容器，用来滚动到底下的
const mindMapRef = ref<HTMLDivElement>(); // 思维导图的容器
const svgRef = ref<SVGElement>(); // 思维导图的渲染 svg
const toolBarRef = ref<HTMLDivElement>(); // 思维导图右下角的工具栏，缩放等
const html = ref(''); // 生成过程中的文本
const contentAreaHeight = ref(0); // 生成区域的高度，出去 header 部分
let markMap: Markmap | null = null;
const transformer = new Transformer();
let resizeObserver: null | ResizeObserver = null;
const initialized = false;
onMounted(() => {
  resizeObserver = new ResizeObserver(() => {
    contentAreaHeight.value = contentRef.value?.clientHeight || 0;
    // 先更新高度，再更新思维导图
    if (contentAreaHeight.value && !initialized) {
      /** 初始化思维导图 */
      try {
        if (!markMap) {
          markMap = Markmap.create(svgRef.value!);
          const { el } = Toolbar.create(markMap);
          toolBarRef.value?.append(el);
        }
        nextTick(update);
      } catch {
        message.error('思维导图初始化失败');
      }
    }
  });
  if (contentRef.value) {
    resizeObserver.observe(contentRef.value);
  }
});
onBeforeUnmount(() => {
  if (resizeObserver && contentRef.value) {
    resizeObserver.unobserve(contentRef.value);
  }
});
watch(props, ({ generatedContent, isGenerating, isEnd, isStart }) => {
  // 开始生成的时候清空一下 markdown 的内容
  if (isStart) {
    html.value = '';
  }
  // 生成内容的时候使用 markdown 来渲染
  if (isGenerating) {
    html.value = md.render(generatedContent);
  }
  // 生成结束时更新思维导图
  if (isEnd) {
    update();
  }
});

/** 更新思维导图的展示 */
const update = () => {
  try {
    const { root } = transformer.transform(
      processContent(props.generatedContent),
    );
    markMap?.setData(root);
    markMap?.fit();
  } catch (error: any) {
    console.error(error);
  }
};
/** 处理内容 */
function processContent(text: string) {
  const arr: string[] = [];
  const lines = text.split('\n');
  for (let line of lines) {
    if (line.includes('```')) {
      continue;
    }
    // eslint-disable-next-line unicorn/prefer-string-replace-all
    line = line.replace(/([*_~`>])|(\d+\.)\s/g, '');
    arr.push(line);
  }
  return arr.join('\n');
}
/** 下载图片：download SVG to png file */
function downloadImage() {
  const svgElement = mindMapRef.value;
  // 将 SVG 渲染到图片对象
  const serializer = new XMLSerializer();
  const source = `<?xml version="1.0" standalone="no"?>\r\n${serializer.serializeToString(svgRef.value!)}`;
  const base64Url = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(source)}`;
  downloadImageByCanvas({
    url: base64Url,
    canvasWidth: svgElement?.offsetWidth,
    canvasHeight: svgElement?.offsetHeight,
    drawWithImageSize: false,
  });
}
defineExpose({
  scrollBottom() {
    mdContainerRef.value?.scrollTo(0, mdContainerRef.value?.scrollHeight);
  },
});
</script>

<template>
  <Card class="my-card flex h-full flex-grow flex-col">
    <template #title>
      <div class="m-0 flex shrink-0 items-center justify-between px-7">
        <h3>思维导图预览</h3>
        <Button type="primary" size="small" class="flex" @click="downloadImage">
          <template #icon>
            <div class="flex items-center justify-center">
              <IconifyIcon icon="lucide:copy" />
            </div>
          </template>
          下载图片
        </Button>
      </div>
    </template>
    <div ref="contentRef" class="hide-scroll-bar box-border h-full">
      <!--展示 markdown 的容器，最终生成的是 html 字符串，直接用 v-html 嵌入-->
      <div
        v-if="isGenerating"
        ref="mdContainerRef"
        class="wh-full overflow-y-auto"
      >
        <div
          class="flex flex-col items-center justify-center"
          v-html="html"
        ></div>
      </div>

      <div ref="mindMapRef" class="wh-full">
        <svg
          ref="svgRef"
          :style="{ height: `${contentAreaHeight}px` }"
          class="w-full"
        />
        <div ref="toolBarRef" class="absolute bottom-2.5 right-5"></div>
      </div>
    </div>
  </Card>
</template>

<style lang="scss" scoped>
// 定义一个 mixin 替代 extend
@mixin hide-scroll-bar {
  -ms-overflow-style: none;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
  }
}

.hide-scroll-bar {
  @include hide-scroll-bar;
}

.my-card {
  :deep(.ant-card-body) {
    box-sizing: border-box;
    flex-grow: 1;
    padding: 0;
    overflow-y: auto;

    @include hide-scroll-bar;
  }
}

// markmap的tool样式覆盖
:deep(.markmap) {
  width: 100%;
}

:deep(.mm-toolbar-brand) {
  display: none;
}

:deep(.mm-toolbar) {
  display: flex;
  flex-direction: row;
}
</style>
