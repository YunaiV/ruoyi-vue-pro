<script lang="ts" setup>
import { useVbenModal, VbenButton } from '@vben/common-ui';

const [SlideModal, slideModalApi] = useVbenModal({
  animationType: 'slide',
});

const [ScaleModal, scaleModalApi] = useVbenModal({
  animationType: 'scale',
});

function openSlideModal() {
  slideModalApi.open();
}

function openScaleModal() {
  scaleModalApi.open();
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex gap-4">
      <VbenButton @click="openSlideModal">滑动动画</VbenButton>
      <VbenButton @click="openScaleModal">缩放动画</VbenButton>
    </div>

    <SlideModal title="滑动动画示例" class="w-[500px]">
      <p>这是使用滑动动画的弹窗，从顶部向下滑动进入。</p>
    </SlideModal>

    <ScaleModal title="缩放动画示例" class="w-[500px]">
      <p>这是使用缩放动画的弹窗，以缩放淡入淡出的方式显示。</p>
    </ScaleModal>
  </div>
</template>
