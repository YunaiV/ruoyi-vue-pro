<template>
  <view>
    <text v-for="(item, index) in textArray" :key="index" :style="{ 'font-size': (index === 1 ? integerSize : size) + 'px', 'color': color }">
      {{ item }}
    </text>
  </view>
</template>

<script>
/**
 * 此组件简单的显示特定样式的(人名币)价格数字
 */
export default {
  name: 'custom-text-price',
  components: {},
  props: {
    price: {
      type: [String, Number],
      default: '0.00'
    },
    color: {
      type: String,
      default: '#333333'
    },
    //字体大小
    size: {
      type: [String, Number],
      default: 15
    },
    //整形部分字体大小可单独定义
    intSize: {
      type: [String, Number],
      default: 15
    }
  },
  computed: {
    textArray() {
      let array = ['￥']
      if (!/^\d+(\.\d+)?$/.test(this.price)) {
        console.error('组件<custom-text-price :text="???" 此处参数应为金额数字')
      } else {
        let arr = parseFloat(this.price).toFixed(2).split('.')
        array.push(arr[0])
        array.push('.' + arr[1])
      }
      return array
    },
    integerSize() {
      return this.intSize ? this.intSize : this.size
    }
  }
}
</script>
<style scoped></style>
