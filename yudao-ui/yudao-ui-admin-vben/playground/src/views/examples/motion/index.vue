<script lang="ts" setup>
import { reactive } from 'vue';

import { Page } from '@vben/common-ui';
import { Motion, MotionGroup, MotionPresets } from '@vben/plugins/motion';

import { refAutoReset, watchDebounced } from '@vueuse/core';
import {
  Button,
  Card,
  Col,
  Form,
  FormItem,
  InputNumber,
  Row,
  Select,
} from 'ant-design-vue';
// 本例子用不到visible类型的动画。带有VisibleOnce和Visible的类型会在组件进入视口被显示时执行动画，
const presets = MotionPresets.filter((v) => !v.includes('Visible'));
const showCard1 = refAutoReset(true, 100);
const showCard2 = refAutoReset(true, 100);
const showCard3 = refAutoReset(true, 100);
const motionProps = reactive({
  delay: 0,
  duration: 300,
  enter: { scale: 1 },
  hovered: { scale: 1.1, transition: { delay: 0, duration: 50 } },
  preset: 'fade',
  tapped: { scale: 0.9, transition: { delay: 0, duration: 50 } },
});

const motionGroupProps = reactive({
  delay: 0,
  duration: 300,
  enter: { scale: 1 },
  hovered: { scale: 1.1, transition: { delay: 0, duration: 50 } },
  preset: 'fade',
  tapped: { scale: 0.9, transition: { delay: 0, duration: 50 } },
});

watchDebounced(
  motionProps,
  () => {
    showCard2.value = false;
  },
  { debounce: 200, deep: true },
);

watchDebounced(
  motionGroupProps,
  () => {
    showCard3.value = false;
  },
  { debounce: 200, deep: true },
);

function openDocPage() {
  window.open('https://motion.vueuse.org/', '_blank');
}
</script>
<template>
  <Page title="Motion">
    <template #description>
      <span>一个易于使用的为其它组件赋予动画效果的组件。</span>
      <Button type="link" @click="openDocPage">查看文档</Button>
    </template>
    <Card title="使用指令" :body-style="{ minHeight: '5rem' }">
      <template #extra>
        <Button type="primary" @click="showCard1 = false">重载</Button>
      </template>
      <div>
        <div class="relative flex gap-2 overflow-hidden" v-if="showCard1">
          <Button v-motion-fade-visible>fade</Button>
          <Button v-motion-pop-visible :duration="500">pop</Button>
          <Button v-motion-slide-left>slide-left</Button>
          <Button v-motion-slide-right>slide-right</Button>
          <Button v-motion-slide-bottom>slide-bottom</Button>
          <Button v-motion-slide-top>slide-top</Button>
        </div>
      </div>
    </Card>
    <Card
      class="mt-2"
      title="使用组件（将内部作为一个整体添加动画）"
      :body-style="{ padding: 0 }"
    >
      <div
        class="relative flex min-h-32 items-center justify-center gap-2 overflow-hidden"
      >
        <Motion
          v-bind="motionProps"
          v-if="showCard2"
          class="flex items-center gap-2"
        >
          <Button size="large">这个按钮在显示时会有动画效果</Button>
          <span>附属组件，会作为整体处理动画</span>
        </Motion>
      </div>
      <div
        class="relative flex min-h-32 items-center justify-center gap-2 overflow-hidden"
      >
        <div v-if="showCard2" class="flex items-center gap-2">
          <span>顺序延迟</span>
          <Motion
            v-bind="{
              ...motionProps,
              delay: motionProps.delay + 100 * i,
            }"
            v-for="i in 5"
            :key="i"
          >
            <Button size="large">按钮{{ i }}</Button>
          </Motion>
        </div>
      </div>
      <div>
        <Form :model="motionProps" :label-col="{ span: 10 }">
          <Row>
            <Col :span="8">
              <FormItem prop="preset" label="动画效果">
                <Select v-model:value="motionProps.preset">
                  <Select.Option
                    :value="preset"
                    v-for="preset in presets"
                    :key="preset"
                  >
                    {{ preset }}
                  </Select.Option>
                </Select>
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="duration" label="持续时间">
                <InputNumber v-model:value="motionProps.duration" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="delay" label="延迟动画">
                <InputNumber v-model:value="motionProps.delay" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="hovered.scale" label="Hover缩放">
                <InputNumber v-model:value="motionProps.hovered.scale" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="hovered.tapped" label="按下时缩放">
                <InputNumber v-model:value="motionProps.tapped.scale" />
              </FormItem>
            </Col>
          </Row>
        </Form>
      </div>
    </Card>
    <Card
      class="mt-2"
      title="分组动画（每个子元素都会应用相同的独立动画）"
      :body-style="{ padding: 0 }"
    >
      <div
        class="relative flex min-h-32 items-center justify-center gap-2 overflow-hidden"
      >
        <MotionGroup v-bind="motionGroupProps" v-if="showCard3">
          <Button size="large">按钮1</Button>
          <Button size="large">按钮2</Button>
          <Button size="large">按钮3</Button>
          <Button size="large">按钮4</Button>
          <Button size="large">按钮5</Button>
        </MotionGroup>
      </div>
      <div>
        <Form :model="motionGroupProps" :label-col="{ span: 10 }">
          <Row>
            <Col :span="8">
              <FormItem prop="preset" label="动画效果">
                <Select v-model:value="motionGroupProps.preset">
                  <Select.Option
                    :value="preset"
                    v-for="preset in presets"
                    :key="preset"
                  >
                    {{ preset }}
                  </Select.Option>
                </Select>
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="duration" label="持续时间">
                <InputNumber v-model:value="motionGroupProps.duration" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="delay" label="延迟动画">
                <InputNumber v-model:value="motionGroupProps.delay" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="hovered.scale" label="Hover缩放">
                <InputNumber v-model:value="motionGroupProps.hovered.scale" />
              </FormItem>
            </Col>
            <Col :span="8">
              <FormItem prop="hovered.tapped" label="按下时缩放">
                <InputNumber v-model:value="motionGroupProps.tapped.scale" />
              </FormItem>
            </Col>
          </Row>
        </Form>
      </div>
    </Card>
  </Page>
</template>
