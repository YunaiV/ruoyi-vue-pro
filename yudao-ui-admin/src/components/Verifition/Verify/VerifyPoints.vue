<template>
  <div style="position: relative" >
    <div class="verify-img-out">
      <div
        class="verify-img-panel"
        :style="{'width': setSize.imgWidth,
                 'height': setSize.imgHeight,
                 'background-size' : setSize.imgWidth + ' '+ setSize.imgHeight,
                 'margin-bottom': vSpace + 'px'}"
      >
        <div v-show="showRefresh" class="verify-refresh" style="z-index:3" @click="refresh">
          <i class="iconfont icon-refresh" />
        </div>
        <img
          ref="canvas"
          :src="pointBackImgBase?('data:image/png;base64,'+pointBackImgBase):defaultImg"
          alt=""
          style="width:100%;height:100%;display:block"
          @click="bindingClick?canvasClick($event):undefined"
        >

        <div
          v-for="(tempPoint, index) in tempPoints"
          :key="index"
          class="point-area"
          :style="{
            'background-color':'#1abd6c',
            color:'#fff',
            'z-index':9999,
            width:'20px',
            height:'20px',
            'text-align':'center',
            'line-height':'20px',
            'border-radius': '50%',
            position:'absolute',
            top:parseInt(tempPoint.y-10) + 'px',
            left:parseInt(tempPoint.x-10) + 'px'
          }"
        >
          {{ index + 1 }}
        </div>
      </div>
    </div>
    <!-- 'height': this.barSize.height, -->
    <div
      class="verify-bar-area"
      :style="{'width': setSize.imgWidth,
               'color': this.barAreaColor,
               'border-color': this.barAreaBorderColor,
               'line-height':this.barSize.height}"
    >
      <span class="verify-msg">{{ text }}</span>
    </div>
  </div>
</template>
<script type="text/babel">
/**
 * VerifyPoints
 * @description 点选
 * */
import { resetSize } from './../utils/util'
import { aesEncrypt } from '@/utils/ase'
import { reqGet, reqCheck } from '@/api/login'

export default {
  name: 'VerifyPoints',
  props: {
    // 弹出式pop，固定fixed
    mode: {
      type: String,
      default: 'fixed'
    },
    captchaType: {
      type: String,
    },
    // 间隔
    vSpace: {
      type: Number,
      default: 5
    },
    imgSize: {
      type: Object,
      default() {
        return {
          width: '310px',
          height: '155px'
        }
      }
    },
    barSize: {
      type: Object,
      default() {
        return {
          width: '310px',
          height: '40px'
        }
      }
    },
    defaultImg: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      secretKey: '', // 后端返回的ase加密秘钥
      checkNum: 3, // 默认需要点击的字数
      fontPos: [], // 选中的坐标信息
      checkPosArr: [], // 用户点击的坐标
      num: 1, // 点击的记数
      pointBackImgBase: '', // 后端获取到的背景图片
      poinTextList: [], // 后端返回的点击字体顺序
      backToken: '', // 后端返回的token值
      setSize: {
        imgHeight: 0,
        imgWidth: 0,
        barHeight: 0,
        barWidth: 0
      },
      tempPoints: [],
      text: '',
      barAreaColor: undefined,
      barAreaBorderColor: undefined,
      showRefresh: true,
      bindingClick: true
    }
  },
  computed: {
    resetSize() {
      return resetSize
    }
  },
  watch: {
    // type变化则全面刷新
    type: {
      immediate: true,
      handler() {
        this.init()
      }
    }
  },
  mounted() {
    // 禁止拖拽
    this.$el.onselectstart = function() {
      return false
    }
  },
  methods: {
    init() {
      // 加载页面
      this.fontPos.splice(0, this.fontPos.length)
      this.checkPosArr.splice(0, this.checkPosArr.length)
      this.num = 1
      this.getPictrue()
      this.$nextTick(() => {
        this.setSize = this.resetSize(this)	// 重新设置宽度高度
        this.$parent.$emit('ready', this)
      })
    },
    canvasClick(e) {
      this.checkPosArr.push(this.getMousePos(this.$refs.canvas, e))
      if (this.num === this.checkNum) {
        this.num = this.createPoint(this.getMousePos(this.$refs.canvas, e))
        // 按比例转换坐标值
        this.checkPosArr = this.pointTransfrom(this.checkPosArr, this.setSize)
        // 等创建坐标执行完
        setTimeout(() => {
          // var flag = this.comparePos(this.fontPos, this.checkPosArr);
          // 发送后端请求
          const captchaVerification = this.secretKey ? aesEncrypt(this.backToken + '---' + JSON.stringify(this.checkPosArr), this.secretKey) : this.backToken + '---' + JSON.stringify(this.checkPosArr)
          const data = {
            captchaType: this.captchaType,
            'pointJson': this.secretKey ? aesEncrypt(JSON.stringify(this.checkPosArr), this.secretKey) : JSON.stringify(this.checkPosArr),
            'token': this.backToken
          }
          reqCheck(data).then(res => {
            if (res.repCode === '0000') {
              this.barAreaColor = '#4cae4c'
              this.barAreaBorderColor = '#5cb85c'
              this.text = '验证成功'
              this.bindingClick = false
              if (this.mode === 'pop') {
                setTimeout(() => {
                  this.$parent.clickShow = false
                  this.refresh()
                }, 1500)
              }
              this.$parent.$emit('success', { captchaVerification })
            } else {
              this.$parent.$emit('error', this)
              this.barAreaColor = '#d9534f'
              this.barAreaBorderColor = '#d9534f'
              this.text = '验证失败'
              setTimeout(() => {
                this.refresh()
              }, 700)
            }
          })
        }, 400)
      }
      if (this.num < this.checkNum) {
        this.num = this.createPoint(this.getMousePos(this.$refs.canvas, e))
      }
    },

    // 获取坐标
    getMousePos: function(obj, e) {
      const x = e.offsetX
      const y = e.offsetY
      return { x, y }
    },
    // 创建坐标点
    createPoint: function(pos) {
      this.tempPoints.push(Object.assign({}, pos))
      return ++this.num
    },
    refresh: function() {
      this.tempPoints.splice(0, this.tempPoints.length)
      this.barAreaColor = '#000'
      this.barAreaBorderColor = '#ddd'
      this.bindingClick = true
      this.fontPos.splice(0, this.fontPos.length)
      this.checkPosArr.splice(0, this.checkPosArr.length)
      this.num = 1
      this.getPictrue()
      this.showRefresh = true
    },

    // 请求背景图片和验证图片
    getPictrue() {
      const data = {
        captchaType: this.captchaType,
        clientUid: localStorage.getItem('point'),
        ts: Date.now(), // 现在的时间戳
      }
      reqGet(data).then(res => {
        if (res.repCode === '0000') {
          this.pointBackImgBase = res.repData.originalImageBase64
          this.backToken = res.repData.token
          this.secretKey = res.repData.secretKey
          this.poinTextList = res.repData.wordList
          this.text = '请依次点击【' + this.poinTextList.join(',') + '】'
        } else {
          this.text = res.repMsg
        }

        // 判断接口请求次数是否失效
        if (res.repCode === '6201') {
          this.pointBackImgBase = null
        }
      })
    },
    // 坐标转换函数
    pointTransfrom(pointArr, imgSize) {
      const newPointArr = pointArr.map(p => {
        const x = Math.round(310 * p.x / parseInt(imgSize.imgWidth))
        const y = Math.round(155 * p.y / parseInt(imgSize.imgHeight))
        return { x, y }
      })
      // console.log(newPointArr,"newPointArr");
      return newPointArr
    }
  },
}
</script>
