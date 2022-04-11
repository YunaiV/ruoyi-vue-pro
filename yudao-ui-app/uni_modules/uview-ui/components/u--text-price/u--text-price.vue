<template>
  <view class="u-text-price-wrap">
    <text
        v-for="(item,index) in textArray"
        :key="index"
        :style="{'font-size': (index === 1 ? integerSize : size) + 'px', 'color': color}"
    >
      {{item}}
    </text>
  </view>
</template>

<script>
/**
 * 此组件存在只为简单的显示特定样式的(人名币)价格数字
 */
export default {
  name: "u--text-price",
  components: {
  },
  props: {
    text: {
      type: String,
      default: '0.00'
    },
    color: {
      type: String,
      default: '#333333'
    },
    //字体大小
    size: {
      type: [String, Number],
      default: uni.$u.props.text.size
    },
    //整形部分字体大小可单独定义
    intSize: {
      type: [String, Number],
      default: uni.$u.props.text.size
    }
  },
  computed: {
    textArray() {
      let array = ['￥'];
      if (!/^\d+(\.\d+)?$/.test(this.text)) {
        uni.$u.error('组件<u--text-price :text="???" 此处参数应为金额数字');
      } else {
        let arr = parseFloat(this.text).toFixed(2).split('.');
        array.push(arr[0]);
        array.push('.' + arr[1]);
      }
      return array;
    },
    integerSize() {
      return this.intSize ? this.intSize : this.size
    }
  }
};
</script>
<style>
</style>