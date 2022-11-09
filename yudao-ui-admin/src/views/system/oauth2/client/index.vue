<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="应用名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入应用名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
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
                   v-hasPermi="['system:oauth2-client:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="客户端编号" align="center" prop="clientId" />
      <el-table-column label="客户端密钥" align="center" prop="secret" />
      <el-table-column label="应用名" align="center" prop="name" />
      <el-table-column label="应用图标" align="center" prop="logo">
        <template slot-scope="scope">
          <img width="40px" height="40px" :src="scope.row.logo">
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="访问令牌的有效期" align="center" prop="accessTokenValiditySeconds">
        <template slot-scope="scope">{{ scope.row.accessTokenValiditySeconds }} 秒</template>
      </el-table-column>
      <el-table-column label="刷新令牌的有效期" align="center" prop="refreshTokenValiditySeconds">
        <template slot-scope="scope">{{ scope.row.refreshTokenValiditySeconds }} 秒</template>
      </el-table-column>
      <el-table-column label="授权类型" align="center" prop="authorizedGrantTypes">
        <template slot-scope="scope">
          <el-tag :disable-transitions="true" :key="index" v-for="(authorizedGrantType, index) in scope.row.authorizedGrantTypes" :index="index">
            {{ authorizedGrantType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:oauth2-client:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:oauth2-client:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="160px">
        <el-form-item label="客户端编号" prop="secret">
          <el-input v-model="form.clientId" placeholder="请输入客户端编号" />
        </el-form-item>
        <el-form-item label="客户端密钥" prop="secret">
          <el-input v-model="form.secret" placeholder="请输入客户端密钥" />
        </el-form-item>
        <el-form-item label="应用名" prop="name">
          <el-input v-model="form.name" placeholder="请输入应用名" />
        </el-form-item>
        <el-form-item label="应用图标">
          <imageUpload v-model="form.logo" :limit="1"/>
        </el-form-item>
        <el-form-item label="应用描述">
          <el-input type="textarea" v-model="form.description" placeholder="请输入应用名" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="访问令牌的有效期" prop="accessTokenValiditySeconds">
          <el-input-number v-model="form.accessTokenValiditySeconds" placeholder="单位：秒" />
        </el-form-item>
        <el-form-item label="刷新令牌的有效期" prop="refreshTokenValiditySeconds">
          <el-input-number v-model="form.refreshTokenValiditySeconds" placeholder="单位：秒" />
        </el-form-item>
        <el-form-item label="授权类型" prop="authorizedGrantTypes">
          <el-select v-model="form.authorizedGrantTypes" multiple filterable placeholder="请输入授权类型" style="width: 500px" >
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="授权范围" prop="scopes">
          <el-select v-model="form.scopes" multiple filterable allow-create placeholder="请输入授权范围" style="width: 500px" >
            <el-option v-for="scope in form.scopes" :key="scope" :label="scope" :value="scope"/>
          </el-select>
        </el-form-item>
        <el-form-item label="自动授权范围" prop="autoApproveScopes">
          <el-select v-model="form.autoApproveScopes" multiple filterable placeholder="请输入授权范围" style="width: 500px" >
            <el-option v-for="scope in form.scopes" :key="scope" :label="scope" :value="scope"/>
          </el-select>
        </el-form-item>
        <el-form-item label="可重定向的 URI 地址" prop="redirectUris">
          <el-select v-model="form.redirectUris" multiple filterable allow-create placeholder="请输入可重定向的 URI 地址" style="width: 500px" >
            <el-option v-for="redirectUri in form.redirectUris" :key="redirectUri" :label="redirectUri" :value="redirectUri"/>
          </el-select>
        </el-form-item>
        <el-form-item label="权限" prop="authorities">
          <el-select v-model="form.authorities" multiple filterable allow-create placeholder="请输入权限" style="width: 500px" >
            <el-option v-for="authority in form.authorities" :key="authority" :label="authority" :value="authority"/>
          </el-select>
        </el-form-item>
        <el-form-item label="资源" prop="resourceIds">
          <el-select v-model="form.resourceIds" multiple filterable allow-create placeholder="请输入资源" style="width: 500px" >
            <el-option v-for="resourceId in form.resourceIds" :key="resourceId" :label="resourceId" :value="resourceId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="附加信息" prop="additionalInformation">
          <el-input type="textarea" v-model="form.additionalInformation" placeholder="请输入附加信息，JSON 格式数据" />
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
import { createOAuth2Client, updateOAuth2Client, deleteOAuth2Client, getOAuth2Client, getOAuth2ClientPage } from "@/api/system/oauth2/oauth2Client";
import ImageUpload from '@/components/ImageUpload';
import Editor from '@/components/Editor';
import {CommonStatusEnum} from "@/utils/constants";
import FileUpload from "@/components/FileUpload";

export default {
  name: "OAuth2Client",
  components: {
    FileUpload,
    ImageUpload,
    Editor,
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // OAuth2 客户端列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        clientId: [{ required: true, message: "客户端编号不能为空", trigger: "blur" }],
        secret: [{ required: true, message: "客户端密钥不能为空", trigger: "blur" }],
        name: [{ required: true, message: "应用名不能为空", trigger: "blur" }],
        logo: [{ required: true, message: "应用图标不能为空", trigger: "blur" }],
        status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
        accessTokenValiditySeconds: [{ required: true, message: "访问令牌的有效期不能为空", trigger: "blur" }],
        refreshTokenValiditySeconds: [{ required: true, message: "刷新令牌的有效期不能为空", trigger: "blur" }],
        redirectUris: [{ required: true, message: "可重定向的 URI 地址不能为空", trigger: "blur" }],
        authorizedGrantTypes: [{ required: true, message: "授权类型不能为空", trigger: "blur" }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getOAuth2ClientPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        clientId: undefined,
        secret: undefined,
        name: undefined,
        logo: undefined,
        description: undefined,
        status: CommonStatusEnum.ENABLE,
        accessTokenValiditySeconds: 30 * 60,
        refreshTokenValiditySeconds: 30 * 24 * 60,
        redirectUris: [],
        authorizedGrantTypes: [],
        scopes: [],
        autoApproveScopes: [],
        authorities: [],
        resourceIds: [],
        additionalInformation: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加 OAuth2 客户端";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getOAuth2Client(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改 OAuth2 客户端";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateOAuth2Client(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createOAuth2Client(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除客户端编号为"' + row.clientId + '"的数据项?').then(function() {
          return deleteOAuth2Client(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    }
  }
};
</script>
