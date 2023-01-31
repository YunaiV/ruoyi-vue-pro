<template>
  <div class="app-container">
    <doc-alert title="公众号接入" url="https://doc.iocoder.cn/mp/account/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入名称" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['mp:account:create']">新增
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="名称" align="center" prop="name"/>
      <el-table-column label="微信号" align="center" prop="account" width="180"/>
      <el-table-column label="appId" align="center" prop="appId" width="180"/>
<!--      <el-table-column label="appSecret" align="center" prop="appSecret" width="180"/>-->
<!--      <el-table-column label="token" align="center" prop="token"/>-->
<!--      <el-table-column label="消息加解密密钥" align="center" prop="aesKey"/>-->
      <el-table-column label="服务器地址(URL)" align="center" prop="appId" width="360">
        <template v-slot="scope">
          {{ 'http://服务端地址/mp/open/' + scope.row.appId }}
        </template>
      </el-table-column>
      <el-table-column label="二维码" align="center" prop="qrCodeUrl">
        <template v-slot="scope">
          <img v-if="scope.row.qrCodeUrl" :src="scope.row.qrCodeUrl" alt="二维码" style="height: 100px;" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark"/>
<!--      <el-table-column label="创建时间" align="center" prop="createTime" width="180">-->
<!--        <template v-slot="scope">-->
<!--          <span>{{ parseTime(scope.row.createTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['mp:account:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['mp:account:delete']">删除
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-refresh" @click="handleGenerateQrCode(scope.row)"
                     v-hasPermi="['mp:account:qr-code']">生成二维码
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-share" @click="handleCleanQuota(scope.row)"
                     v-hasPermi="['mp:account:clear-quota']">清空 API 配额
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item label="微信号" prop="account">
         <span slot="label">
           <el-tooltip content="在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 账号详情] 中能找到「微信号」" placement="top">
              <i class="el-icon-question" />
           </el-tooltip>
           微信号
          </span>
          <el-input v-model="form.account" placeholder="请输入微信号"/>
        </el-form-item>
        <el-form-item label="appId" prop="appId">
          <span slot="label">
            <el-tooltip content="在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 基本设置] 中能找到「开发者ID(AppID)」" placement="top">
              <i class="el-icon-question" />
            </el-tooltip>
            appId
          </span>
          <el-input v-model="form.appId" placeholder="请输入公众号 appId"/>
        </el-form-item>
        <el-form-item label="appSecret" prop="appSecret">
          <span slot="label">
            <el-tooltip content="在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 基本设置] 中能找到「开发者密码(AppSecret)」" placement="top">
              <i class="el-icon-question" />
            </el-tooltip>
            appSecret
          </span>
          <el-input v-model="form.appSecret" placeholder="请输入公众号 appSecret"/>
        </el-form-item>
        <el-form-item label="token" prop="token">
          <el-input v-model="form.token" placeholder="请输入公众号token"/>
        </el-form-item>
        <el-form-item label="消息加解密密钥" prop="aesKey">
          <el-input v-model="form.aesKey" placeholder="请输入消息加解密密钥"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  clearAccountQuota,
  createAccount,
  deleteAccount,
  generateAccountQrCode,
  getAccount,
  getAccountPage,
  updateAccount
} from '@/api/mp/account'

export default {
  name: 'mpAccount',
  components: {},
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 公众号账号列表
      list: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        account: null,
        appId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{required: true, message: '名称不能为空', trigger: 'blur'}],
        account: [{required: true, message: '公众号账号不能为空', trigger: 'blur'}],
        appId: [{required: true, message: '公众号 appId 不能为空', trigger: 'blur'}],
        appSecret: [{required: true, message: '公众号密钥不能为空', trigger: 'blur'}],
        token: [{required: true, message: '公众号 token 不能为空', trigger: 'blur'}],
      },
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      // 处理查询参数
      let params = {...this.queryParams}
      // 执行查询
      getAccountPage(params).then(response => {
        this.list = response.data.list
        this.total = response.data.total
        this.loading = false
      })
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        account: undefined,
        appId: undefined,
        appSecret: undefined,
        token: undefined,
        aesKey: undefined,
        remark: undefined,
      }
      this.resetForm('form')
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加公众号账号'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id
      getAccount(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改公众号账号'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        // 修改的提交
        if (this.form.id != null) {
          updateAccount(this.form).then(response => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        // 添加的提交
        createAccount(this.form).then(response => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id
      this.$modal.confirm('是否确认删除公众号账号编号为"' + row.name + '"的数据项?').then(function () {
        return deleteAccount(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {
      })
    },
    /** 生成二维码的按钮操作 */
    handleGenerateQrCode(row) {
      const id = row.id
      this.$modal.confirm('是否确认生成公众号账号编号为"' + row.name + '"的二维码?').then(function () {
        return generateAccountQrCode(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('生成二维码成功')
      }).catch(() => {
      })
    },
    /** 清空二维码 API 配额的按钮操作 */
    handleCleanQuota(row) {
      const id = row.id
      this.$modal.confirm('是否确认清空生成公众号账号编号为"' + row.name + '"的 API 配额?').then(function () {
        return clearAccountQuota(id)
      }).then(() => {
        this.$modal.msgSuccess('清空 API 配额成功')
      }).catch(() => {
      })
    },
  }
}
</script>
