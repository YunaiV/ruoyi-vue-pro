<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="优惠券类" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择优惠券类型 reward-满减 discount-折扣 random-随机" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="优惠券名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入优惠券名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态（1进行中2已结束-1已关闭）" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效日期结束时间" prop="endTime">
        <el-date-picker v-model="queryParams.endTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
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
                   v-hasPermi="['CouponTemplete::create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['CouponTemplete::export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">

    <!--      todo 优惠券类型-->
      <el-table-column label="优惠券类型" align="center" prop="type" />
      <el-table-column label="优惠券名称" align="center" prop="name" />
      <el-table-column label="优惠券图片" align="center" prop="image" />
      <el-table-column label="发放数量" align="center" prop="count" />
      <el-table-column label="已领取数量" align="center" prop="leadCount" />
      <el-table-column label="已使用数量" align="center" prop="usedCount" />
<!--            todo 适用商品类型-->
<!--      <el-table-column label="适用商品类型" align="center" prop="goodsType" />-->
      <!--      todo 使用门槛0-无门槛 1-有门槛-->

      <el-table-column label="使用门槛" align="center" prop="hasUseLimit" />
<!--      <el-table-column label="满多少元使用 0代表无限制" align="center" prop="atLeast" />-->
      <!--      todo 发放面额 折扣-->
      <el-table-column label="优惠金额/折扣" align="center" prop="money" />
<!--      <el-table-column label="1 =< 折扣 <= 9.9 当type为discount时需要添加" align="center" prop="discount" />-->
<!--      <el-table-column label="最多折扣金额 当type为discount时可选择性添加" align="center" prop="discountLimit" />-->
<!--      <el-table-column label="最低金额 当type为radom时需要添加" align="center" prop="minMoney" />-->
<!--      <el-table-column label="最大金额 当type为radom时需要添加" align="center" prop="maxMoney" />-->
      <!-- todo 1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期 -->
      <el-table-column label="有效期限" align="center" prop="validityType" />
<!--      <el-table-column label="使用开始日期 过期类型1时必填" align="center" prop="startUseTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.startUseTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="使用结束日期 过期类型1时必填" align="center" prop="endUseTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.endUseTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="有效日期结束时间" align="center" prop="endTime" width="180">-->
<!--        <template slot-scope="scope">-->
<!--          <span>{{ parseTime(scope.row.endTime) }}</span>-->
<!--        </template>-->
<!--      </el-table-column>-->
<!--      <el-table-column label="当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效" align="center" prop="fixedTerm" />-->
<!--      <el-table-column label="是否无限制0-否 1是" align="center" prop="whetherLimitless" />-->
      <el-table-column label="领取上限" align="center" prop="maxFetch" />
      <!--   todo     0-不限制 1- 优惠券仅原价购买商品时可用-->
      <el-table-column label="优惠叠加" align="center" prop="whetherForbidPreference" />
      <el-table-column label="是否显示" align="center" prop="whetherShow" />
      <el-table-column label="订单的优惠总金额" align="center" prop="discountOrderMoney" />
      <el-table-column label="用券总成交额" align="center" prop="orderMoney" />
      <el-table-column label="是否禁止发放" align="center" prop="whetherForbidden" />
      <el-table-column label="使用优惠券购买的商品数量" align="center" prop="orderGoodsNum" />
      <el-table-column label="状态" align="center" prop="status" />

      <el-table-column label="备注" align="center" prop="couponNameRemark" />

      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">

        <template #scope>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['CouponTemplete::update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['CouponTemplete::delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择优惠券类型 reward-满减 discount-折扣 random-随机">
            <el-option label="请选择字典生成" value="" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="名称备注" prop="couponNameRemark">
          <el-input v-model="form.couponNameRemark" placeholder="请输入名称备注" />
        </el-form-item>
        <el-form-item label="发放数量" prop="count">
          <el-input v-model="form.count" placeholder="请输入发放数量" />
        </el-form-item>
        <el-form-item label="适用商品类型" prop="goodsType">
          <el-radio-group v-model="form.goodsType">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
      <!--        todo 请输入适用商品id-->
        <el-form-item label="适用商品id" prop="productIds">
          <el-input v-model="form.productIds" placeholder="请输入适用商品id" />
        </el-form-item>
        <el-form-item label="使用门槛" prop="hasUseLimit">
          <el-radio-group v-model="form.hasUseLimit">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="满多少元使用" prop="atLeast">
          <el-input v-model="form.atLeast" placeholder="请输入满多少元使用 0代表无限制" />
        </el-form-item>
        <el-form-item label="发放面额" prop="money">
          <el-input v-model="form.money" placeholder="请输入发放面额 当type为reward时需要添加" />
        </el-form-item>
        <el-form-item label="1 =< 折扣 <= 9.9" prop="discount">
          <el-input v-model="form.discount" placeholder="请输入1 =< 折扣 <= 9.9 当type为discount时需要添加" />
        </el-form-item>
        <el-form-item label="最多折扣金额" prop="discountLimit">
          <el-input v-model="form.discountLimit" placeholder="请输入最多折扣金额 当type为discount时可选择性添加" />
        </el-form-item>
        <el-form-item label="最低金额" prop="minMoney">
          <el-input v-model="form.minMoney" placeholder="请输入最低金额 当type为radom时需要添加" />
        </el-form-item>
        <el-form-item label="最大金额" prop="maxMoney">
          <el-input v-model="form.maxMoney" placeholder="请输入最大金额 当type为radom时需要添加" />
        </el-form-item>
        <el-form-item label="过期类型" prop="validityType">
          <el-radio-group v-model="form.validityType">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用开始日期 过期类型1时必填" prop="startUseTime">
          <el-date-picker clearable v-model="form.startUseTime" type="date" value-format="timestamp" placeholder="选择使用开始日期 过期类型1时必填" />
        </el-form-item>
        <el-form-item label="使用结束日期 过期类型1时必填" prop="endUseTime">
          <el-date-picker clearable v-model="form.endUseTime" type="date" value-format="timestamp" placeholder="选择使用结束日期 过期类型1时必填" />
        </el-form-item>
        <el-form-item label="当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效" prop="fixedTerm">
          <el-input v-model="form.fixedTerm" placeholder="请输入当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效" />
        </el-form-item>
        <el-form-item label="是否无限制0-否 1是" prop="whetherLimitless">
          <el-radio-group v-model="form.whetherLimitless">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="每人最大领取个数" prop="maxFetch">
          <el-input v-model="form.maxFetch" placeholder="请输入每人最大领取个数" />
        </el-form-item>
        <el-form-item label="是否开启过期提醒0-不开启 1-开启" prop="whetherExpireNotice">
          <el-radio-group v-model="form.whetherExpireNotice">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="过期前N天提醒" prop="expireNoticeFixedTerm">
          <el-input v-model="form.expireNoticeFixedTerm" placeholder="请输入过期前N天提醒" />
        </el-form-item>
        <el-form-item label="优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用" prop="whetherForbidPreference">
          <el-radio-group v-model="form.whetherForbidPreference">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否显示" prop="whetherShow">
          <el-input v-model="form.whetherShow" placeholder="请输入是否显示" />
        </el-form-item>
        <el-form-item label="订单的优惠总金额" prop="discountOrderMoney">
          <el-input v-model="form.discountOrderMoney" placeholder="请输入订单的优惠总金额" />
        </el-form-item>
        <el-form-item label="用券总成交额" prop="orderMoney">
          <el-input v-model="form.orderMoney" placeholder="请输入用券总成交额" />
        </el-form-item>
        <el-form-item label="是否禁止发放0-否 1-是" prop="whetherForbidden">
          <el-radio-group v-model="form.whetherForbidden">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用优惠券购买的商品数量" prop="orderGoodsNum">
          <el-input v-model="form.orderGoodsNum" placeholder="请输入使用优惠券购买的商品数量" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="1">请选择字典生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效日期结束时间" prop="endTime">
          <el-date-picker clearable v-model="form.endTime" type="date" value-format="timestamp" placeholder="选择有效日期结束时间" />
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
import { create, update, deleteCouponTemplete, get, getPage, exportExcel } from "@/api/mall/CouponTemplete/CouponTemplete.js";
import {getDictDatas} from "@/utils/dict";

export default {
  name: "",
  components: {
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
      // 优惠券模板列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        type: null,
        name: null,
        couponNameRemark: null,
        image: null,
        count: null,
        leadCount: null,
        usedCount: null,
        goodsType: null,
        productIds: null,
        hasUseLimit: null,
        atLeast: null,
        money: null,
        discount: null,
        discountLimit: null,
        minMoney: null,
        maxMoney: null,
        validityType: null,
        startUseTime: [],
        endUseTime: [],
        fixedTerm: null,
        whetherLimitless: null,
        maxFetch: null,
        whetherExpireNotice: null,
        expireNoticeFixedTerm: null,
        whetherForbidPreference: null,
        whetherShow: null,
        discountOrderMoney: null,
        orderMoney: null,
        whetherForbidden: null,
        orderGoodsNum: null,
        status: null,
        endTime: [],
        createTime: [],
      },
      //数据字典
      datas:getDictDatas(),

      // 表单参数
      form: {},
      // 表单校验
      rules: {
        type: [{ required: true, message: "优惠券类型 reward-满减 discount-折扣 random-随机不能为空", trigger: "change" }],
        name: [{ required: true, message: "优惠券名称不能为空", trigger: "blur" }],
        count: [{ required: true, message: "发放数量不能为空", trigger: "blur" }],
        leadCount: [{ required: true, message: "已领取数量不能为空", trigger: "blur" }],
        usedCount: [{ required: true, message: "已使用数量不能为空", trigger: "blur" }],
        goodsType: [{ required: true, message: "适用商品类型1-全部商品可用；2-指定商品可用；3-指定商品不可用不能为空", trigger: "blur" }],
        hasUseLimit: [{ required: true, message: "使用门槛0-无门槛 1-有门槛不能为空", trigger: "blur" }],
        atLeast: [{ required: true, message: "满多少元使用 0代表无限制不能为空", trigger: "blur" }],
        money: [{ required: true, message: "发放面额 当type为reward时需要添加不能为空", trigger: "blur" }],
        discount: [{ required: true, message: "1 =< 折扣 <= 9.9 当type为discount时需要添加不能为空", trigger: "blur" }],
        discountLimit: [{ required: true, message: "最多折扣金额 当type为discount时可选择性添加不能为空", trigger: "blur" }],
        minMoney: [{ required: true, message: "最低金额 当type为radom时需要添加不能为空", trigger: "blur" }],
        maxMoney: [{ required: true, message: "最大金额 当type为radom时需要添加不能为空", trigger: "blur" }],
        validityType: [{ required: true, message: "过期类型1-时间范围过期 2-领取之日固定日期后过期 3-领取次日固定日期后过期不能为空", trigger: "blur" }],
        fixedTerm: [{ required: true, message: "当validity_type为2或者3时需要添加 领取之日起或者次日N天内有效不能为空", trigger: "blur" }],
        whetherLimitless: [{ required: true, message: "是否无限制0-否 1是不能为空", trigger: "blur" }],
        maxFetch: [{ required: true, message: "每人最大领取个数不能为空", trigger: "blur" }],
        whetherExpireNotice: [{ required: true, message: "是否开启过期提醒0-不开启 1-开启不能为空", trigger: "blur" }],
        expireNoticeFixedTerm: [{ required: true, message: "过期前N天提醒不能为空", trigger: "blur" }],
        whetherForbidPreference: [{ required: true, message: "优惠叠加 0-不限制 1- 优惠券仅原价购买商品时可用不能为空", trigger: "blur" }],
        whetherShow: [{ required: true, message: "是否显示不能为空", trigger: "blur" }],
        discountOrderMoney: [{ required: true, message: "订单的优惠总金额不能为空", trigger: "blur" }],
        orderMoney: [{ required: true, message: "用券总成交额不能为空", trigger: "blur" }],
        whetherForbidden: [{ required: true, message: "是否禁止发放0-否 1-是不能为空", trigger: "blur" }],
        orderGoodsNum: [{ required: true, message: "使用优惠券购买的商品数量不能为空", trigger: "blur" }],
        status: [{ required: true, message: "状态（1进行中2已结束-1已关闭）不能为空", trigger: "blur" }],
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
      getPage(this.queryParams).then(response => {
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
        type: undefined,
        name: undefined,
        couponNameRemark: undefined,
        image: undefined,
        count: undefined,
        leadCount: undefined,
        usedCount: undefined,
        goodsType: undefined,
        productIds: undefined,
        hasUseLimit: undefined,
        atLeast: undefined,
        money: undefined,
        discount: undefined,
        discountLimit: undefined,
        minMoney: undefined,
        maxMoney: undefined,
        validityType: undefined,
        startUseTime: undefined,
        endUseTime: undefined,
        fixedTerm: undefined,
        whetherLimitless: undefined,
        maxFetch: undefined,
        whetherExpireNotice: undefined,
        expireNoticeFixedTerm: undefined,
        whetherForbidPreference: undefined,
        whetherShow: undefined,
        discountOrderMoney: undefined,
        orderMoney: undefined,
        whetherForbidden: undefined,
        orderGoodsNum: undefined,
        status: undefined,
        endTime: undefined,
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
      this.title = "添加优惠券模板";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      get(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改优惠券模板";
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
          update(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        create(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除优惠券模板编号为"' + id + '"的数据项?').then(function() {
          return deleteCouponTemplete(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.$modal.confirm('是否确认导出所有优惠券模板数据项?').then(() => {
          this.exportLoading = true;
          return exportExcel(params);
        }).then(response => {
          this.$download.excel(response, '优惠券模板.xls');
          this.exportLoading = false;
        }).catch(() => {});
    }
  }
};
</script>
