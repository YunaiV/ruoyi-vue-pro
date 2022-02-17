<template>
  <div>
    <el-dialog :visible.sync="transferParam.aliPayOpen" :title="title" @closed="close" append-to-body width="800px">
      <el-form ref="aliPayForm" :model="form" :rules="rules" size="medium" label-width="100px"
               v-loading="transferParam.loading">
        <el-form-item label-width="180px" label="渠道费率" prop="feeRate">
          <el-input v-model="form.feeRate" placeholder="请输入渠道费率" clearable :style="{width: '100%'}">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="开放平台APPID" prop="aliPayConfig.appId">
          <el-input v-model="form.aliPayConfig.appId" placeholder="请输入开放平台APPID" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>
        <el-form-item label-width="180px" label="渠道状态" prop="status">
          <el-radio-group v-model="form.status" size="medium">
            <el-radio v-for="dict in statusDictDatas" :key="parseInt(dict.value)" :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="网关地址" prop="aliPayConfig.serverUrl">
          <el-radio-group v-model="form.aliPayConfig.serverUrl" size="medium">
            <el-radio v-for="dict in aliPayServerDatas" :key="dict.value" :label="dict.value">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="算法类型" prop="aliPayConfig.signType">
          <el-radio-group v-model="form.aliPayConfig.signType" size="medium">
            <el-radio v-for="dict in aliPaySignTypeDatas" :key="dict.value" :label="dict.value">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="180px" label="公钥类型" prop="aliPayConfig.mode">
          <el-radio-group v-model="form.aliPayConfig.mode" size="medium">
            <el-radio v-for="dict in aliPayModeDatas" :key="parseInt(dict.value)" :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="form.aliPayConfig.mode === 1">
          <el-form-item label-width="180px" label="商户私钥" prop="aliPayConfig.privateKey">
            <el-input type="textarea" :autosize="{minRows: 8, maxRows: 8}" v-model="form.aliPayConfig.privateKey"
                      placeholder="请输入商户私钥" clearable :style="{width: '100%'}">
            </el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="支付宝公钥字符串" prop="aliPayConfig.alipayPublicKey">
            <el-input
              type="textarea"
              :autosize="{minRows: 8, maxRows: 8}"
              v-model="form.aliPayConfig.alipayPublicKey"
              placeholder="请输入支付宝公钥字符串" clearable
              :style="{width: '100%'}">
            </el-input>
          </el-form-item>
        </div>
        <div v-if="form.aliPayConfig.mode === 2">
          <el-form-item label-width="180px" label="商户公钥应用证书" prop="aliPayConfig.appCertContent">
            <el-input v-model="form.aliPayConfig.appCertContent" type="textarea"
                      placeholder="请上传商户公钥应用证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              action=""
              ref="privateKeyContentFile"
              :limit="1"
              :accept="fileAccept"
              :http-request="appCertUpload"
              :before-upload="fileBeforeUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="支付宝公钥证书" prop="aliPayConfig.alipayPublicCertContent">
            <el-input v-model="form.aliPayConfig.alipayPublicCertContent" type="textarea"
                      placeholder="请上传支付宝公钥证书"
                      readonly :autosize="{minRows: 8, maxRows: 8}" :style="{width: '100%'}"></el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              ref="privateCertContentFile"
              action=""
              :limit="1"
              :accept="fileAccept"
              :before-upload="fileBeforeUpload"
              :http-request="alipayPublicCertUpload">
              <el-button size="small" type="primary" icon="el-icon-upload">点击上传</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label-width="180px" label="根证书" prop="aliPayConfig.rootCertContent">
            <el-input
              v-model="form.aliPayConfig.rootCertContent"
              type="textarea"
              placeholder="请上传根证书"
              readonly :autosize="{minRows: 8, maxRows: 8}"
              :style="{width: '100%'}">
            </el-input>
          </el-form-item>
          <el-form-item label-width="180px" label="">
            <el-upload
              ref="privateCertContentFile"
              :limit="1"
              :accept="fileAccept"
              action=""
              :before-upload="fileBeforeUpload"
              :http-request="rootCertUpload">
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
  aliPayConfig: {
    appId: '',
    serverUrl: null,
    signType: '',
    mode: null,
    privateKey: '',
    alipayPublicKey: '',
    appCertContent: '',
    alipayPublicCertContent: '',
    rootCertContent: ''
  }
};

export default {
  name: "aliPayChannelForm",
  components: {},
  props: {
    // 传输的参数
    transferParam: {
      // 加载动画
      "loading": false,
      // 是否修改
      "edit": false,
      // 是否显示
      "aliPayOpen": false,
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
        'aliPayConfig.appId': [{
          required: true,
          message: '请输入开放平台上创建的应用的 ID',
          trigger: 'blur'
        }],
        status: [{
          required: true,
          message: '渠道状态不能为空',
          trigger: 'blur'
        }],
        'aliPayConfig.serverUrl': [{
          required: true,
          message: '请传入网关地址',
          trigger: 'blur'
        }],
        'aliPayConfig.signType': [{
          required: true,
          message: '请传入签名算法类型',
          trigger: 'blur'
        }],
        'aliPayConfig.mode': [{
          required: true,
          message: '公钥类型不能为空',
          trigger: 'blur'
        }],
        'aliPayConfig.privateKey': [{
          required: true,
          message: '请输入商户私钥',
          trigger: 'blur'
        }],
        'aliPayConfig.alipayPublicKey': [{
          required: true,
          message: '请输入支付宝公钥字符串',
          trigger: 'blur'
        }],
        'aliPayConfig.appCertContent': [{
          required: true,
          message: '请上传商户公钥应用证书',
          trigger: 'blur'
        }],
        'aliPayConfig.alipayPublicCertContent': [{
          required: true,
          message: '请上传支付宝公钥证书',
          trigger: 'blur'
        }],
        'aliPayConfig.rootCertContent': [{
          required: true,
          message: '请上传指定根证书',
          trigger: 'blur'
        }],
      },
      fileAccept: ".crt",
      // 渠道状态 数据字典
      statusDictDatas: getDictDatas(DICT_TYPE.COMMON_STATUS),
      // 支付宝加密方式
      aliPaySignTypeDatas: getDictDatas(DICT_TYPE.PAY_CHANNEL_ALIPAY_SIGN_TYPE),
      // 版本状态 数据字典
      aliPayModeDatas: getDictDatas(DICT_TYPE.PAY_CHANNEL_ALIPAY_MODE),
      // 支付宝网关地址
      aliPayServerDatas: getDictDatas(DICT_TYPE.PAY_CHANNEL_ALIPAY_SERVER_TYPE),
    }
  },
  watch: {
    transferParam: {
      deep: true,  // 深度监听
      handler(newVal) {
        if (newVal.aliPayOpen) {
          this.form.code = newVal.payCode;
          this.form.appId = newVal.appId;
          this.form.merchantId = newVal.payMerchant.id;
          // 只有在初次进来为编辑 并且为加载中的时候才回去请求数据
          if (newVal.edit === true && newVal.loading) {
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
          this.form.status = response.data.status;
          this.form.remark = response.data.remark;

          let config = JSON.parse(response.data.config);
          this.form.aliPayConfig.appId = config.appId;
          this.form.aliPayConfig.serverUrl = config.serverUrl;
          this.form.aliPayConfig.signType = config.signType;
          this.form.aliPayConfig.mode = config.mode;
          this.form.aliPayConfig.privateKey = config.privateKey;
          this.form.aliPayConfig.alipayPublicKey = config.alipayPublicKey;
          this.form.aliPayConfig.appCertContent = config.appCertContent;
          this.form.aliPayConfig.alipayPublicCertContent = config.alipayPublicCertContent;
          this.form.aliPayConfig.rootCertContent = config.rootCertContent;
          this.transferParam.loading = false;
        })
    },
    close() {
      this.transferParam.aliPayOpen = false;
      this.form = JSON.parse(JSON.stringify(defaultForm));
    },
    handleConfirm() {
      this.$refs['aliPayForm'].validate(valid => {
        if (!valid) {
          return
        }
        let data = this.form;
        data.config = JSON.stringify(this.form.aliPayConfig);
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
    fileBeforeUpload(file) {
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
    appCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.form.aliPayConfig.appCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    alipayPublicCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.form.aliPayConfig.alipayPublicCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },
    rootCertUpload(event) {
      const readFile = new FileReader()
      readFile.onload = (e) => {
        this.form.aliPayConfig.rootCertContent = e.target.result
      }
      readFile.readAsText(event.file);
    },

  }
}

</script>
<style scoped>

</style>
