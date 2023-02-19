<template>
  <div class="app-container">
    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">发起订单</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="订单编号" align="center" prop="id" />
      <el-table-column label="用户编号" align="center" prop="userId" />
      <el-table-column label="商品名字" align="center" prop="spuName" />
      <el-table-column label="支付价格" align="center" prop="price">
        <template v-slot="scope">
          <span>￥{{ (scope.row.price / 100.0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款金额" align="center" prop="refundPrice">
        <template v-slot="scope">
          <span>￥{{ (scope.row.refundPrice / 100.0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付单号" align="center" prop="payOrderId" />
      <el-table-column label="是否支付" align="center" prop="payed">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.payed" />
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款时间" align="center" prop="refundTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.refundTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handlePay(scope.row)"
                     v-if="!scope.row.payed">前往支付</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleRefund(scope.row)"
                     v-if="scope.row.payed && !scope.row.payRefundId">发起退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品" prop="spuId">
          <el-select v-model="form.spuId" placeholder="请输入下单商品" clearable size="small" style="width: 380px" >
            <el-option v-for="item in spus" :key="item.id" :label="item.name" :value="item.id">
              <span style="float: left">{{ item.name}}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">￥{{ (item.price / 100.0).toFixed(2) }}</span>
            </el-option>
          </el-select>
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
import {createDemoOrder, getDemoOrderPage, refundDemoOrder} from "@/api/pay/demo";
import {deleteMerchant} from "@/api/pay/merchant";

export default {
  name: "PayDemoOrder",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 示例订单列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        spuId: [{ required: true, message: "商品编号不能为空", trigger: "blur" }],
      },
      // 商品数组
      spus: [{
        id: 1,
        name: '华为手机',
        price: 1,
      }, {
        id: 2,
        name: '小米电视',
        price: 10,
      }, {
        id: 3,
        name: '苹果手表',
        price: 100,
      }, {
        id: 4,
        name: '华硕笔记本',
        price: 1000,
      }, {
        id: 5,
        name: '蔚来汽车',
        price: 200000,
      }]
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
      getDemoOrderPage(this.queryParams).then(response => {
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
        spuId: undefined,
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
      this.title = "发起订单";
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 添加的提交
        createDemoOrder(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 支付按钮操作 */
    handlePay(row) {
      this.$router.push({
          name: 'PayOrderSubmit',
          query:{
            id: row.payOrderId
          }
      })
    },
    /** 退款按钮操作 */
    handleRefund(row) {
      const id = row.id;
      this.$modal.confirm('是否确认退款编号为"' + id + '"的示例订单?').then(function() {
        return refundDemoOrder(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("发起退款成功！");
      }).catch(() => {});
    }
  }
};
</script>
