<template>
  <view class="container">
    <view class="address-box">
      <u--form labelPosition="left" :model="formData" :rules="rules" ref="form">
        <u-form-item label="收件人名称" prop="name" labelWidth="90" borderBottom ref="item-name">
          <u-input type="text" maxlength="11" v-model="formData.name" clearable placeholder="请填写收件人名称" border="none"></u-input>
        </u-form-item>

        <u-form-item label="手机号" prop="mobile" labelWidth="90" borderBottom ref="item-mobile">
          <u-input type="number" maxlength="11" v-model="formData.mobile" clearable placeholder="请填写手机号" border="none"></u-input>
        </u-form-item>

        <u-form-item label="省市地区" prop="areaText" labelWidth="90" borderBottom @click=" regionVisible = true; hideKeyboard()" ref="item-areaText">
          <u--input v-model="formData.areaText" disabled disabledColor="#ffffff" placeholder="请选择省市地区" border="none"></u--input>
          <u-icon slot="right" name="arrow-right"></u-icon>
          <w-picker :visible.sync="regionVisible" mode="region" :value="defaultRegion" default-type="value" :hide-area="false" @confirm="onConfirm($event, 'region')" @cancel="onCancel" ref="region"></w-picker>
        </u-form-item>

        <u-form-item label="详细地址" prop="detail" labelWidth="90" borderBottom ref="item-detail">
          <u--textarea placeholder="请输入街道门牌号不低于6个字" v-model="formData.detail" count></u--textarea>
        </u-form-item>

        <u-form-item label="默认地址" prop="type" labelWidth="90" borderBottom ref="item-type">
          <u-radio-group v-model="formData.type">
            <u-radio :customStyle="{ marginRight: '16px' }" v-for="(item, index) in typeList" :key="index" :label="item.name" :name="item.value"></u-radio>
          </u-radio-group>
        </u-form-item>

        <view class="btn-group">
          <u-button type="primary" text="更新地址" customStyle="margin-top: 50px" @click="handleSubmit"></u-button>
        </view>
      </u--form>
    </view>
  </view>
</template>

<script>
import { getAddressById, updateAddress } from '../../api/address'

export default {
  data() {
    return {
      id: '',
      regionVisible: false,
      defaultRegion: ['110000', '110100', '110101'],
      typeList: [
        {
          name: '是',
          value: 1
        },
        {
          name: '否',
          value: 2
        }
      ],
      formData: {
        id: '',
        name: '',
        mobile: '',
        areaText: '',
        areaCode: '',
        detail: '',
        detailAddress: '',
        type: 1
      },
      rules: {
        name: [
          {
            type: 'string',
            min: 2,
            max: 12,
            required: true,
            message: '请填写收件人名称',
            trigger: ['blur', 'change']
          },
          {
            message: '收件人名称不能为空',
            // 触发器可以同时用blur和change
            trigger: ['change', 'blur']
          }
        ],
        mobile: [
          {
            type: 'integer',
            required: true,
            message: '请填写手机号',
            trigger: ['blur', 'change']
          },
          {
            // 自定义验证函数，见上说明
            validator: (rule, value, callback) => {
              // 上面有说，返回true表示校验通过，返回false表示不通过
              // uni.$u.test.mobile()就是返回true或者false的
              return uni.$u.test.mobile(value)
            },
            message: '手机号码不正确',
            // 触发器可以同时用blur和change
            trigger: ['change', 'blur']
          }
        ],
        areaText: {
          type: 'string',
          required: true,
          message: '请选择省市地区',
          trigger: ['blur', 'change']
        },
        detailAddress: {
          type: 'string',
          min: 6,
          max: 30,
          required: true,
          message: '请填写详细地址',
          trigger: ['blur', 'change']
        }
      }
    }
  },
  onLoad(e) {
    if (!e.addressId) {
      uni.$u.toast('请求参数错误')
    } else {
      this.id = e.addressId
      this.loadAddressData()
    }
  },
  methods: {
    loadAddressData() {
      getAddressById({ id: this.id }).then(res => {
        this.formData = res.data
        this.initRegionData()
      })
    },
    initRegionData(){
      //回显 【省市地区】 和 【详细地址】 信息
      if (this.formData.areaCode) {
        const areaCode = this.formData.areaCode + ''
        //通过地区code反解析出【省-市-地区】code
        this.defaultRegion.splice(0, 3, areaCode.substring(0,2).padEnd(6, '0'), areaCode.substring(0,4).padEnd(6, '0'), areaCode)
        this.$nextTick(res => {
          let areaText = this.$refs.region._data.result.result
          this.formData.areaText = areaText
          //通过从完整详细地址除去【省-市-地区】得到后半段地址信息
          this.formData.detail = this.formData.detailAddress.replace(areaText, '')
          this.$forceUpdate();
        })
      }
    },
    onConfirm(res) {
      this.formData.areaText = res.result
      this.formData.areaCode = res.value[2]
    },
    onCancel() {},
    hideKeyboard() {
      uni.hideKeyboard()
    },
    handleSubmit() {
      this.$refs.form.validate().then(res => {
        this.formData.detailAddress = this.formData.areaText + this.formData.detail
        console.log(this.formData)
        updateAddress(this.formData).then(res => {
          uni.$u.toast('地址已更新')
          setTimeout(() => {
            uni.navigateBack()
          }, 300)
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.address-box {
  width: 690rpx;
  margin: 0 auto;
  padding-top: 30rpx;
}
</style>
