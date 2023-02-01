<template>
  <div class="app-container">
    <doc-alert title="OAuth 2.0（SSO 单点登录)" url="https://doc.iocoder.cn/oauth2/" />
    <doc-alert title="用户体系" url="https://doc.iocoder.cn/user-center/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="用户编号" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="客户端编号" prop="clientId">
        <el-input v-model="queryParams.clientId" placeholder="请输入客户端编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="queryParams.userType" placeholder="请选择用户类型" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.USER_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table v-loading="loading" :data="list" style="width: 100%;">
      <el-table-column label="访问令牌" align="center" prop="accessToken" width="300" />
      <el-table-column label="刷新令牌" align="center" prop="refreshToken" width="300" />
      <el-table-column label="用户编号" align="center" prop="userId" />
      <el-table-column label="用户类型" align="center" prop="userType" width="100">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.USER_TYPE" :value="scope.row.userType"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="过期时间" align="center" prop="expiresTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.expiresTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleForceLogout(scope.row)"
            v-hasPermi="['system:oauth2-token:delete']">强退</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { getAccessTokenPage, deleteAccessToken } from "@/api/system/oauth2/oauth2Token";

export default {
  name: "OAuth2Token",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        userId: undefined,
        userType: undefined,
        clientId: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      getAccessTokenPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 强退按钮操作 */
    handleForceLogout(row) {
      this.$modal.confirm('是否确认强退令牌为"' + row.accessToken + '"的数据项?').then(function() {
          return deleteAccessToken(row.accessToken);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("强退成功");
      }).catch(() => {});
    }
  }
};
</script>

