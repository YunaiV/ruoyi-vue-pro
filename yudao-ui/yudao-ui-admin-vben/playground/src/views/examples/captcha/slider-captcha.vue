<script lang="ts" setup>
import type {
  CaptchaVerifyPassingData,
  SliderCaptchaActionType,
} from '@vben/common-ui';

import { ref } from 'vue';

import { Page, SliderCaptcha } from '@vben/common-ui';
import { Bell, Sun } from '@vben/icons';

import { Button, Card, message } from 'ant-design-vue';

function handleSuccess(data: CaptchaVerifyPassingData) {
  const { time } = data;
  message.success(`校验成功,耗时${time}秒`);
}
function handleBtnClick(elRef?: SliderCaptchaActionType) {
  if (!elRef) {
    return;
  }
  elRef.resume();
}

const el1 = ref<SliderCaptchaActionType>();
const el2 = ref<SliderCaptchaActionType>();
const el3 = ref<SliderCaptchaActionType>();
const el4 = ref<SliderCaptchaActionType>();
const el5 = ref<SliderCaptchaActionType>();
const el6 = ref<SliderCaptchaActionType>();
</script>

<template>
  <Page description="用于前端简单的拖动校验场景" title="滑块校验">
    <Card class="mb-5" title="基础示例">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha ref="el1" @success="handleSuccess" />
        <Button class="ml-2" type="primary" @click="handleBtnClick(el1)">
          还原
        </Button>
      </div>
    </Card>
    <Card class="mb-5" title="自定义圆角">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha
          ref="el2"
          class="rounded-full"
          @success="handleSuccess"
        />
        <Button class="ml-2" type="primary" @click="handleBtnClick(el2)">
          还原
        </Button>
      </div>
    </Card>
    <Card class="mb-5" title="自定义背景色">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha
          ref="el3"
          :bar-style="{
            backgroundColor: '#018ffb',
          }"
          success-text="校验成功"
          text="拖动以进行校验"
          @success="handleSuccess"
        />
        <Button class="ml-2" type="primary" @click="handleBtnClick(el3)">
          还原
        </Button>
      </div>
    </Card>
    <Card class="mb-5" title="自定义拖拽图标">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha ref="el4" @success="handleSuccess">
          <template #actionIcon="{ isPassing }">
            <Bell v-if="isPassing" />
            <Sun v-else />
          </template>
        </SliderCaptcha>
        <Button class="ml-2" type="primary" @click="handleBtnClick(el4)">
          还原
        </Button>
      </div>
    </Card>
    <Card class="mb-5" title="自定义文本">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha
          ref="el5"
          success-text="成功"
          text="拖动"
          @success="handleSuccess"
        />
        <Button class="ml-2" type="primary" @click="handleBtnClick(el5)">
          还原
        </Button>
      </div>
    </Card>
    <Card class="mb-5" title="自定义内容(slot)">
      <div class="flex items-center justify-center p-4 px-[30%]">
        <SliderCaptcha ref="el6" @success="handleSuccess">
          <template #text="{ isPassing }">
            <template v-if="isPassing">
              <Bell class="mr-2 size-4" />
              成功
            </template>
            <template v-else>
              拖动
              <Sun class="ml-2 size-4" />
            </template>
          </template>
        </SliderCaptcha>
        <Button class="ml-2" type="primary" @click="handleBtnClick(el6)">
          还原
        </Button>
      </div>
    </Card>
  </Page>
</template>
