<script lang="ts" setup>
import type { ComponentInternalInstance } from 'vue';

import type { VerificationProps } from './typing';

import {
  getCurrentInstance,
  nextTick,
  onMounted,
  reactive,
  ref,
  toRefs,
} from 'vue';

import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { AES } from '@vben-core/shared/utils';

import { resetSize } from './utils/util';

/**
 * VerifyPoints
 * @description 点选
 */

defineOptions({
  name: 'VerifyPoints',
});

const props = withDefaults(defineProps<VerificationProps>(), {
  barSize: () => ({
    height: '40px',
    width: '310px',
  }),
  captchaType: 'clickWord',
  imgSize: () => ({
    height: '155px',
    width: '310px',
  }),
  mode: 'fixed',
  space: 5,
});

const emit = defineEmits(['onSuccess', 'onError', 'onClose', 'onReady']);

const { captchaType, mode, checkCaptchaApi, getCaptchaApi } = toRefs(props);
const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const secretKey = ref(); // 后端返回的ase加密秘钥
const checkNum = ref(3); // 默认需要点击的字数
const fontPos = reactive<any[]>([]); // 选中的坐标信息
const checkPosArr = reactive<any[]>([]); // 用户点击的坐标
const num = ref(1); // 点击的记数
const pointBackImgBase = ref(); // 后端获取到的背景图片
const poinTextList = ref<any[]>([]); // 后端返回的点击字体顺序
const backToken = ref(); // 后端返回的token值
const setSize = reactive({
  barHeight: 0,
  barWidth: 0,
  imgHeight: 0,
  imgWidth: 0,
});
const tempPoints = reactive<any[]>([]);
const text = ref();
const barAreaColor = ref();
const barAreaBorderColor = ref();
const showRefresh = ref(true);
const bindingClick = ref(true);

function init() {
  // 加载页面
  fontPos.splice(0);
  checkPosArr.splice(0);
  num.value = 1;
  getPictrue();
  nextTick(() => {
    const { barHeight, barWidth, imgHeight, imgWidth } = resetSize(proxy);
    setSize.imgHeight = imgHeight;
    setSize.imgWidth = imgWidth;
    setSize.barHeight = barHeight;
    setSize.barWidth = barWidth;
    emit('onReady', proxy);
  });
}

onMounted(() => {
  // 禁止拖拽
  init();
  proxy?.$el?.addEventListener('selectstart', () => {
    return false;
  });
});
const canvas = ref(null);

// 获取坐标
const getMousePos = function (_obj: any, e: any) {
  const x = e.offsetX;
  const y = e.offsetY;
  return { x, y };
};
// 创建坐标点
const createPoint = function (pos: any) {
  tempPoints.push(Object.assign({}, pos));
  return num.value + 1;
};

// 坐标转换函数
const pointTransfrom = function (pointArr: any, imgSize: any) {
  const newPointArr = pointArr.map((p: any) => {
    const x = Math.round((310 * p.x) / Number.parseInt(imgSize.imgWidth));
    const y = Math.round((155 * p.y) / Number.parseInt(imgSize.imgHeight));
    return { x, y };
  });
  return newPointArr;
};

const refresh = async function () {
  tempPoints.splice(0);
  barAreaColor.value = '#000';
  barAreaBorderColor.value = '#ddd';
  bindingClick.value = true;
  fontPos.splice(0);
  checkPosArr.splice(0);
  num.value = 1;
  await getPictrue();
  showRefresh.value = true;
};

function canvasClick(e: any) {
  checkPosArr.push(getMousePos(canvas, e));
  if (num.value === checkNum.value) {
    num.value = createPoint(getMousePos(canvas, e));
    // 按比例转换坐标值
    const arr = pointTransfrom(checkPosArr, setSize);
    checkPosArr.length = 0;
    checkPosArr.push(...arr);
    // 等创建坐标执行完
    setTimeout(() => {
      // var flag = this.comparePos(this.fontPos, this.checkPosArr);
      // 发送后端请求
      const captchaVerification = secretKey.value
        ? AES.encrypt(
            `${backToken.value}---${JSON.stringify(checkPosArr)}`,
            secretKey.value,
          )
        : `${backToken.value}---${JSON.stringify(checkPosArr)}`;
      const data = {
        captchaType: captchaType.value,
        pointJson: secretKey.value
          ? AES.encrypt(JSON.stringify(checkPosArr), secretKey.value)
          : JSON.stringify(checkPosArr),
        token: backToken.value,
      };
      checkCaptchaApi?.value?.(data).then((response: any) => {
        const res = response.data;
        if (res.repCode === '0000') {
          barAreaColor.value = '#4cae4c';
          barAreaBorderColor.value = '#5cb85c';
          text.value = $t('ui.captcha.sliderSuccessText');
          bindingClick.value = false;
          if (mode.value === 'pop') {
            setTimeout(() => {
              emit('onClose');
              refresh();
            }, 1500);
          }
          emit('onSuccess', { captchaVerification });
        } else {
          emit('onError', proxy);
          barAreaColor.value = '#d9534f';
          barAreaBorderColor.value = '#d9534f';
          text.value = $t('ui.captcha.sliderRotateFailTip');
          setTimeout(() => {
            refresh();
          }, 700);
        }
      });
    }, 400);
  }
  if (num.value < checkNum.value)
    num.value = createPoint(getMousePos(canvas, e));
}

// 请求背景图片和验证图片
async function getPictrue() {
  const data = {
    captchaType: captchaType.value,
  };
  const res = await getCaptchaApi?.value?.(data);

  if (res?.data?.repCode === '0000') {
    pointBackImgBase.value = `data:image/png;base64,${res?.data?.repData?.originalImageBase64}`;
    backToken.value = res.data.repData.token;
    secretKey.value = res.data.repData.secretKey;
    poinTextList.value = res.data.repData.wordList;
    text.value = `${$t('ui.captcha.clickInOrder')}【${poinTextList.value.join(',')}】`;
  } else {
    text.value = res?.data?.repMsg;
  }
}
defineExpose({
  init,
  refresh,
});
</script>

<template>
  <div style="position: relative">
    <div class="verify-img-out">
      <div
        :style="{
          width: setSize.imgWidth,
          height: setSize.imgHeight,
          'background-size': `${setSize.imgWidth} ${setSize.imgHeight}`,
          'margin-bottom': `${space}px`,
        }"
        class="verify-img-panel"
      >
        <div
          v-show="showRefresh"
          class="verify-refresh"
          style="z-index: 3"
          @click="refresh"
        >
          <IconifyIcon icon="lucide:refresh-ccw" class="mr-2 size-5" />
        </div>
        <img
          ref="canvas"
          :src="pointBackImgBase"
          alt=""
          style="display: block; width: 100%; height: 100%"
          @click="bindingClick ? canvasClick($event) : undefined"
        />

        <div
          v-for="(tempPoint, index) in tempPoints"
          :key="index"
          :style="{
            'background-color': '#1abd6c',
            color: '#fff',
            'z-index': 9999,
            width: '20px',
            height: '20px',
            'text-align': 'center',
            'line-height': '20px',
            'border-radius': '50%',
            position: 'absolute',
            top: `${tempPoint.y - 10}px`,
            left: `${tempPoint.x - 10}px`,
          }"
          class="point-area"
        >
          {{ index + 1 }}
        </div>
      </div>
    </div>
    <!-- 'height': this.barSize.height, -->
    <div
      :style="{
        width: setSize.imgWidth,
        color: barAreaColor,
        'border-color': barAreaBorderColor,
        'line-height': barSize.height,
      }"
      class="verify-bar-area"
    >
      <span class="verify-msg">{{ text }}</span>
    </div>
  </div>
</template>
