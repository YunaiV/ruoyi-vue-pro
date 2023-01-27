<script setup lang="ts">
import { PropType, nextTick, ref, watch, computed, unref } from 'vue'
import QRCode from 'qrcode'
import { QRCodeRenderersOptions } from 'qrcode'
import { cloneDeep } from 'lodash-es'
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'
import { isString } from '@/utils/is'
import { QrcodeLogo } from '@/types/qrcode'

const props = defineProps({
  // img 或者 canvas,img不支持logo嵌套
  tag: propTypes.string.validate((v: string) => ['canvas', 'img'].includes(v)).def('canvas'),
  // 二维码内容
  text: {
    type: [String, Array] as PropType<string | Recordable[]>,
    default: null
  },
  // qrcode.js配置项
  options: {
    type: Object as PropType<QRCodeRenderersOptions>,
    default: () => ({})
  },
  // 宽度
  width: propTypes.number.def(200),
  // logo
  logo: {
    type: [String, Object] as PropType<Partial<QrcodeLogo> | string>,
    default: ''
  },
  // 是否过期
  disabled: propTypes.bool.def(false),
  // 过期提示内容
  disabledText: propTypes.string.def('')
})

const emit = defineEmits(['done', 'click', 'disabled-click'])

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('qrcode')

const { toCanvas, toDataURL } = QRCode

const loading = ref(true)

const wrapRef = ref<Nullable<HTMLCanvasElement | HTMLImageElement>>(null)

const renderText = computed(() => String(props.text))

const wrapStyle = computed(() => {
  return {
    width: props.width + 'px',
    height: props.width + 'px'
  }
})

const initQrcode = async () => {
  await nextTick()
  const options = cloneDeep(props.options || {})
  if (props.tag === 'canvas') {
    // 容错率，默认对内容少的二维码采用高容错率，内容多的二维码采用低容错率
    options.errorCorrectionLevel =
      options.errorCorrectionLevel || getErrorCorrectionLevel(unref(renderText))
    const _width: number = await getOriginWidth(unref(renderText), options)
    options.scale = props.width === 0 ? undefined : (props.width / _width) * 4
    const canvasRef: HTMLCanvasElement | any = await toCanvas(
      unref(wrapRef) as HTMLCanvasElement,
      unref(renderText),
      options
    )
    if (props.logo) {
      const url = await createLogoCode(canvasRef)
      emit('done', url)
      loading.value = false
    } else {
      emit('done', canvasRef.toDataURL())
      loading.value = false
    }
  } else {
    const url = await toDataURL(renderText.value, {
      errorCorrectionLevel: 'H',
      width: props.width,
      ...options
    })
    ;(unref(wrapRef) as HTMLImageElement).src = url
    emit('done', url)
    loading.value = false
  }
}

watch(
  () => renderText.value,
  (val) => {
    if (!val) return
    initQrcode()
  },
  {
    deep: true,
    immediate: true
  }
)

const createLogoCode = (canvasRef: HTMLCanvasElement) => {
  const canvasWidth = canvasRef.width
  const logoOptions: QrcodeLogo = Object.assign(
    {
      logoSize: 0.15,
      bgColor: '#ffffff',
      borderSize: 0.05,
      crossOrigin: 'anonymous',
      borderRadius: 8,
      logoRadius: 0
    },
    isString(props.logo) ? {} : props.logo
  )
  const {
    logoSize = 0.15,
    bgColor = '#ffffff',
    borderSize = 0.05,
    crossOrigin = 'anonymous',
    borderRadius = 8,
    logoRadius = 0
  } = logoOptions
  const logoSrc = isString(props.logo) ? props.logo : props.logo.src
  const logoWidth = canvasWidth * logoSize
  const logoXY = (canvasWidth * (1 - logoSize)) / 2
  const logoBgWidth = canvasWidth * (logoSize + borderSize)
  const logoBgXY = (canvasWidth * (1 - logoSize - borderSize)) / 2

  const ctx = canvasRef.getContext('2d')
  if (!ctx) return

  // logo 底色
  canvasRoundRect(ctx)(logoBgXY, logoBgXY, logoBgWidth, logoBgWidth, borderRadius)
  ctx.fillStyle = bgColor
  ctx.fill()

  // logo
  const image = new Image()
  if (crossOrigin || logoRadius) {
    image.setAttribute('crossOrigin', crossOrigin)
  }
  ;(image as any).src = logoSrc

  // 使用image绘制可以避免某些跨域情况
  const drawLogoWithImage = (image: HTMLImageElement) => {
    ctx.drawImage(image, logoXY, logoXY, logoWidth, logoWidth)
  }

  // 使用canvas绘制以获得更多的功能
  const drawLogoWithCanvas = (image: HTMLImageElement) => {
    const canvasImage = document.createElement('canvas')
    canvasImage.width = logoXY + logoWidth
    canvasImage.height = logoXY + logoWidth
    const imageCanvas = canvasImage.getContext('2d')
    if (!imageCanvas || !ctx) return
    imageCanvas.drawImage(image, logoXY, logoXY, logoWidth, logoWidth)

    canvasRoundRect(ctx)(logoXY, logoXY, logoWidth, logoWidth, logoRadius)
    if (!ctx) return
    const fillStyle = ctx.createPattern(canvasImage, 'no-repeat')
    if (fillStyle) {
      ctx.fillStyle = fillStyle
      ctx.fill()
    }
  }

  // 将 logo绘制到 canvas上
  return new Promise((resolve: any) => {
    image.onload = () => {
      logoRadius ? drawLogoWithCanvas(image) : drawLogoWithImage(image)
      resolve(canvasRef.toDataURL())
    }
  })
}

// 得到原QrCode的大小，以便缩放得到正确的QrCode大小
const getOriginWidth = async (content: string, options: QRCodeRenderersOptions) => {
  const _canvas = document.createElement('canvas')
  await toCanvas(_canvas, content, options)
  return _canvas.width
}

// 对于内容少的QrCode，增大容错率
const getErrorCorrectionLevel = (content: string) => {
  if (content.length > 36) {
    return 'M'
  } else if (content.length > 16) {
    return 'Q'
  } else {
    return 'H'
  }
}

// copy来的方法，用于绘制圆角
const canvasRoundRect = (ctx: CanvasRenderingContext2D) => {
  return (x: number, y: number, w: number, h: number, r: number) => {
    const minSize = Math.min(w, h)
    if (r > minSize / 2) {
      r = minSize / 2
    }
    ctx.beginPath()
    ctx.moveTo(x + r, y)
    ctx.arcTo(x + w, y, x + w, y + h, r)
    ctx.arcTo(x + w, y + h, x, y + h, r)
    ctx.arcTo(x, y + h, x, y, r)
    ctx.arcTo(x, y, x + w, y, r)
    ctx.closePath()
    return ctx
  }
}

const clickCode = () => {
  emit('click')
}

const disabledClick = () => {
  emit('disabled-click')
}
</script>

<template>
  <div v-loading="loading" :class="[prefixCls, 'relative inline-block']" :style="wrapStyle">
    <component :is="tag" ref="wrapRef" @click="clickCode" />
    <div
      v-if="disabled"
      :class="`${prefixCls}--disabled`"
      class="absolute top-0 left-0 flex w-full h-full items-center justify-center"
      @click="disabledClick"
    >
      <div class="absolute top-[50%] left-[50%] font-bold">
        <Icon icon="ep:refresh-right" :size="30" color="var(--el-color-primary)" />
        <div>{{ disabledText }}</div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-qrcode;

.#{$prefix-cls} {
  &--disabled {
    background: rgba(255, 255, 255, 0.95);

    & > div {
      transform: translate(-50%, -50%);
    }
  }
}
</style>
