<template>
  <div>
    <el-dialog :visible.sync="transferParam.wechatOpen" :title="title" @close="close" append-to-body width="800px">
      <el-form ref="wechatJsApiForm" :model="form" :rules="rules" size="medium" label-width="100px"
               v-loading="transferParam.loading">
        <el-form-item label-width="180px" label="渠道费率" prop="feeRate">
          <el-input v-model="form.feeRate" placeholder="请输入渠道费率" clearable :style="{width: '100%'}">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="公众号APPID" prop="weChatConfig.appId">
          <el-input v-model="form.weChatConfig.appId" placeholder="请输入公众号APPID" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="商户号" prop="weChatConfig.mchId">
          <el-input v-model="form.weChatConfig.mchId" :style="{width: '100%'}"></el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="渠道状态" prop="status">
          <el-radio-group v-model="form.status" size="medium">
            <el-radio v-for="dict in statusDictDatas" :key="parseInt(dict.value)" :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="API 版本" prop="weChatConfig.apiVersion">
          <el-radio-group v-model="form.weChatConfig.apiVersion" size="medium">
            <el-radio v-for="dict in versionDictDatas" :key="dict.value" :label="dict.value">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="商户密钥" prop="weChatConfig.mchKey"
                      v-if="form.weChatConfig.apiVersion === 'v2'">
          <el-input v-model="form.weChatConfig.mchKey" placeholder="请输入商户密钥" clearable
                    :style="{width: '100%'}" type="textarea" :autosize="{minRows: 8, maxRows: 8}"></el-input>
        </el-form-item>
        <div v-if="form.weChatConfig.apiVersion === 'v3'">
          <el-form-item label-width="180px" label="API V3密钥" prop="weChatConfig.apiV3Key">
            <el-input v-model="form.weChatConfig.apiV3Key" placeholder="请输入API V3密钥" clearable
                      :style="{width: '100%'}" type="textarea" :autosize="{minRows: 8, maxRows: 8}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="apiclient_key.perm证书" prop="weChatConfig.privateKeyContent">
            <el-input v-model="form.weChatConfig.privateKeyContent" type="textarea"
                      placeholder="请上传apiclient_key.perm证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="" prop="privateKeyContentFile">
            <el-upload ref="privateKeyContentFile"
                       :limit="1"
                       :accept="fileAccept"
                       :headers="header"
                       action=""
                       :before-upload="pemFileBeforeUpload"
                       :http-request="privateKeyUpload"
            >
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="apiclient_cert.perm证书" prop="weChatConfig.privateCertContent">
            <el-input v-model="form.weChatConfig.privateCertContent" type="textarea"
                      placeholder="请上传apiclient_cert.perm证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="" prop="privateCertContentFile">
            <el-upload ref="privateCertContentFile"
                       :limit="1"
                       :accept="fileAccept"
                       :headers="header"
                       action=""
                       :before-upload="pemFileBeforeUpload"
                       :http-request="privateCertUpload"
            >
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item label-width="180px" label="备注" prop="remark">
          <el-input v-model="form.remark" :style="{width: '100%'}"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {createChannel, getChannel, updateChannel} from "@/api/pay/channel";

const defaultForm = {
  code: '',
  status: null,
  remark: '',
  feeRate: null,
  appId: '',
  merchantId: null,
  weChatConfig: {
    appId: '',
    mchId: '',
    apiVersion: '',
    mchKey: '',
    privateKeyContent: '',
    privateCertContent: '',
    apiV3Key:'',
  }
}

export default {
  name: "wechatChannelForm",
  components: {},
  props: {
    // 传输的参数
    transferParam: {
      // 加载动画
      "loading": false,
      // 是否修改
      "edit": false,
      // 是否显示
      "wechatOpen": false,
      // 应用ID
      "appId": null,
      // 渠道编码
      "payCode": null,
      // 商户对象
      "payMerchant": {
        // 编号
        "id": null,
        // 名称
        "name": null
      },
    }
  },
  data() {
    return {
      title:'',
      form: JSON.parse(JSON.stringify(defaultForm)),
      rules: {
        feeRate: [{
          required: true,
          message: '请输入渠道费率',
          trigger: 'blur'
        }],
        'weChatConfig.mchId': [{
          required: true,
          message: '请传入商户号',
          trigger: 'blur'
        }],
        'weChatConfig.appId': [{
          required: true,
          message: '请输入公众号APPID',
          trigger: 'blur'
        }],
        status: [{
          required: true,
          message: '渠道状态不能为空',
          trigger: 'blur'
        }],
        'weChatConfig.apiVersion': [{
          required: true,
          message: 'API版本不能为空',
          trigger: 'blur'
        }],
        'weChatConfig.mchKey': [{
          required: true,
          message: '请输入商户密钥',
          trigger: 'blur'
        }],
        'weChatConfig.privateKeyContent': [{
          required: true,
          message: '请上传apiclient_key.perm证书',
          trigger: 'blur'
        }],
        'weChatConfig.privateCertContent': [{
          required: true,
          message: '请上传apiclient_cert.perm证书',
          trigger: 'blur'
        }],
        'weChatConfig.apiV3Key': [{
          required: true,
          message: '请上传apiV3密钥值',
          trigger: 'blur'
        }],
      },
      // 文件上传的header
      header: {
        "Authorization": null
      },
      fileAccept: ".pem",
      // 渠道状态 数据字典
      statusDictDatas: getDictDatas(DICT_TYPE.COMMON_STATUS),
      versionDictDatas: getDictDatas(DICT_TYPE.PAY_CHANNEL_WECHAT_VERSION),
    }
  },
  watch: {
    transferParam: {
      deep: true,  // 深度监听
      handler(newVal) {
        if (newVal.wechatOpen) {
          this.form.code = newVal.payCode;
          this.form.appId = newVal.appId;
          this.form.merchantId = newVal.payMerchant.id;
          // 只有在初次进来为编辑 并且为加载中的时候才回去请求数据
          if (newVal.edit && newVal.loading) {
            this.title = "编辑支付渠道";
            this.init();
          } else {
            this.title = "创建支付渠道";
          }
        }
      }
    }
  },
  methods: {
    init() {
      getChannel(this.transferParam.payMerchant.id, this.transferParam.appId, this.transferParam.payCode)
        .then(response => {
          this.form.id = response.data.id;
          this.form.feeRate = response.data.feeRate;
          this.form.appId = response.data.appId;
          this.form.status = response.data.status;
          this.form.remark = response.data.remark;

          let config = JSON.parse(response.data.config);
          this.form.weChatConfig.appId = config.appId;
          this.form.weChatConfig.apiVersion = config.apiVersion;
          this.form.weChatConfig.mchId = config.mchId;
          this.form.weChatConfig.mchKey = config.mchKey;
          this.form.weChatConfig.privateKeyContent = config.privateKeyContent;
          this.form.weChatConfig.privateCertContent = config.privateCertContent;
          this.form.weChatConfig.apiV3Key = config.apiV3Key;
          this.transferParam.loading = false;
        })
    },
    close() {
      this.transferParam.wechatOpen = false;
      this.form = JSON.parse(JSON.stringify(defaultForm));
    },
    handleConfirm() {
      this.$refs['wechatJsApiForm'].validate(valid => {
        if (!valid) {
          return
        }
        let data = this.form;
        data.config = JSON.stringify(this.form.weChatConfig);
        if (this.transferParam.edit) {
          updateChannel(data).then(response => {
            if (response.code === 0) {
              this.$modal.msgSuccess("修改成功");
              this.close();
            }

          })
        } else {

          createChannel(data).then(response => {
            if (response.code === 0) {
              this.$modal.msgSuccess("新增成功");
              this.$parent.refreshTable();
              this.close();
            }
          });
        }
      });
    },
    pemFileBeforeUpload(file) {
      let format = '.' + file.name.split(".")[1];
      if (format !== this.fileAccept) {
        this.$message.error('请上传指定格式"' + this.fileAccept + '"文件');
        return false;
      }
      let isRightSize = file.size / 1024 / 1024 < 2
      if (!isRightSize) {
        this.$message.error('文件大小超过 2MB')
      }
      return isRightSize
    },
    privateKeyUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.form.weChatConfig.privateKeyContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    privateCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.form.weChatConfig.privateCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    }
  }
}

</script>
<style scoped>

</style>
