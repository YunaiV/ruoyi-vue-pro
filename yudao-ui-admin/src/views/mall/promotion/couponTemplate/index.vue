<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="优惠券名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入优惠劵名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="优惠券类型" prop="discountType">
        <el-select v-model="queryParams.discountType" placeholder="请选择优惠券类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_DISCOUNT_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="优惠券状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择优惠券状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
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
                   v-hasPermi="['promotion:coupon-template:create']">新增</el-button>
        <el-button type="info" plain icon="el-icon-s-operation" size="mini"
                   @click="() => this.$router.push('/promotion/coupon')"
                   v-hasPermi="['promotion:coupon:query']">会员优惠劵</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="优惠券名称" align="center" prop="name" />
      <el-table-column label="优惠券类型" align="center" prop="discountType">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.PROMOTION_DISCOUNT_TYPE" :value="scope.row.discountType" />
        </template>
      </el-table-column>
      <el-table-column label="优惠金额 / 折扣" align="center" prop="discount" :formatter="discountFormat" />
      <el-table-column label="发放数量" align="center" prop="totalCount" />
      <el-table-column label="剩余数量" align="center" prop="totalCount" :formatter="row => (row.totalCount - row.takeCount)" />
      <el-table-column label="领取上限" align="center" prop="takeLimitCount" :formatter="takeLimitCountFormat" />
      <el-table-column label="有效期限" align="center" prop="validityType" width="180" :formatter="validityTypeFormat" />
      <el-table-column label="状态" align="center" prop="status">
        <template v-slot="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1" @change="handleStatusChange(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['promotion:coupon-template:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['promotion:coupon-template:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="600px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_DISCOUNT_TYPE)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.discountType === PromotionDiscountTypeEnum.PRICE.type" label="优惠券面额" prop="discountPrice">
          <el-input-number v-model="form.discountPrice" placeholder="请输入优惠金额，单位：元"
                           style="width: 400px" :precision="2" :min="0" /> 元
        </el-form-item>
        <el-form-item v-if="form.discountType === PromotionDiscountTypeEnum.PERCENT.type" label="优惠券折扣" prop="discountPercent">
          <el-input-number v-model="form.discountPercent" placeholder="优惠券折扣不能小于 1 折，且不可大于 9.9 折"
                           style="width: 400px" :precision="1" :min="1" :max="9.9" /> 折
        </el-form-item>
        <el-form-item v-if="form.discountType === PromotionDiscountTypeEnum.PERCENT.type" label="最多优惠" prop="discountLimitPrice">
          <el-input-number v-model="form.discountLimitPrice" placeholder="请输入最多优惠"
                           style="width: 400px" :precision="2" :min="0" /> 元
        </el-form-item>
        <el-form-item label="满多少元可以使用" prop="usePrice">
          <el-input-number v-model="form.usePrice" placeholder="无门槛请设为 0"
                    style="width: 400px" :precision="2" :min="0" /> 元
        </el-form-item>
        <el-form-item label="领取方式" prop="takeType">
          <el-radio-group v-model="form.takeType">
            <el-radio :key="1" :label="1">直接领取</el-radio>
            <el-radio :key="2" :label="2">指定发放</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.takeType === 1" label="发放数量" prop="totalCount">
          <el-input-number v-model="form.totalCount" placeholder="发放数量，没有之后不能领取或发放，-1 为不限制"
                           style="width: 400px" :precision="0" :min="-1" /> 张
        </el-form-item>
        <el-form-item v-if="form.takeType === 1" label="每人限领个数" prop="takeLimitCount">
          <el-input-number v-model="form.takeLimitCount" placeholder="设置为 -1 时，可无限领取"
                    style="width: 400px" :precision="0" :min="-1" /> 张
        </el-form-item>
        <el-form-item label="有效期类型" prop="validityType">
          <el-radio-group v-model="form.validityType">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_COUPON_TEMPLATE_VALIDITY_TYPE)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.validityType === CouponTemplateValidityTypeEnum.DATE.type" label="固定日期" prop="validTimes">
          <el-date-picker v-model="form.validTimes" style="width: 240px"  value-format="yyyy-MM-dd HH:mm:ss" type="datetimerange"
                          :default-time="['00:00:00', '23:59:59']" />
        </el-form-item>
        <el-form-item v-if="form.validityType === CouponTemplateValidityTypeEnum.TERM.type" label="领取日期" prop="fixedStartTerm">
          第 <el-input-number v-model="form.fixedStartTerm" placeholder="0 为今天生效"
                              style="width: 165px" :precision="0" :min="0"/> 至
          <el-input-number v-model="form.fixedEndTerm" placeholder="请输入结束天数"
                           style="width: 165px" :precision="0" :min="0"/> 天有效
        </el-form-item>
        <el-form-item label="活动商品" prop="productScope">
          <el-radio-group v-model="form.productScope">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_PRODUCT_SCOPE)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.productScope === PromotionProductScopeEnum.SPU.scope" prop="productSpuIds">
          <el-select v-model="form.productSpuIds" placeholder="请选择活动商品" clearable size="small"
                     multiple filterable style="width: 400px">
            <el-option v-for="item in productSpus" :key="item.id" :label="item.name" :value="item.id">
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">￥{{ (item.minPrice / 100.0).toFixed(2) }}</span>
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
import {
  createCouponTemplate,
  updateCouponTemplate,
  deleteCouponTemplate,
  getCouponTemplate,
  getCouponTemplatePage,
  updateCouponTemplateStatus
} from "@/api/mall/promotion/couponTemplate";
import {
  CommonStatusEnum,
  CouponTemplateValidityTypeEnum,
  PromotionDiscountTypeEnum,
  PromotionProductScopeEnum
} from "@/utils/constants";
import { getSpuSimpleList } from "@/api/mall/product/spu";
import { parseTime } from "@/utils/ruoyi";
import {changeRoleStatus} from "@/api/system/role";

export default {
  name: "CouponTemplate",
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
      // 优惠劵列表
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
        type: null,
        createTime: [],
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "优惠券名称不能为空", trigger: "blur" }],
        discountType: [{ required: true, message: "优惠券类型不能为空", trigger: "change" }],
        discountPrice: [{ required: true, message: "优惠券面额不能为空", trigger: "blur" }],
        discountPercent: [{ required: true, message: "优惠券折扣不能为空", trigger: "blur" }],
        discountLimitPrice: [{ required: true, message: "最多优惠不能为空", trigger: "blur" }],
        usePrice: [{ required: true, message: "满多少元可以使用不能为空", trigger: "blur" }],
        takeType: [{ required: true, message: "领取方式不能为空", trigger: "change" }],
        totalCount: [{ required: true, message: "发放数量不能为空", trigger: "blur" }],
        takeLimitCount: [{ required: true, message: "每人限领个数不能为空", trigger: "blur" }],
        validityType: [{ required: true, message: "有效期类型不能为空", trigger: "change" }],
        validTimes: [{ required: true, message: "固定日期不能为空", trigger: "change" }],
        fixedStartTerm: [{ required: true, message: "开始领取天数不能为空", trigger: "blur" }],
        fixedEndTerm: [{ required: true, message: "开始领取天数不能为空", trigger: "blur" }],
        productScope: [{ required: true, message: "商品范围不能为空", trigger: "blur" }],
        productSpuIds: [{ required: true, message: "商品范围不能为空", trigger: "blur" }],
      },
      // 商品列表
      productSpus: [],
      // 如下的变量，主要为了 v-if 判断可以使用到
      PromotionProductScopeEnum: PromotionProductScopeEnum,
      CouponTemplateValidityTypeEnum: CouponTemplateValidityTypeEnum,
      PromotionDiscountTypeEnum: PromotionDiscountTypeEnum,
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
      getCouponTemplatePage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
      // 查询商品列表
      getSpuSimpleList().then(response => {
        this.productSpus = response.data
      })
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
        name: undefined,
        discountType: PromotionDiscountTypeEnum.PRICE.type,
        discountPrice: undefined,
        discountPercent: undefined,
        discountLimitPrice: undefined,
        usePrice: undefined,
        takeType: 1,
        totalCount: undefined,
        takeLimitCount: undefined,
        validityType: CouponTemplateValidityTypeEnum.DATE.type,
        validTimes: [],
        validStartTime: undefined,
        validEndTime: undefined,
        fixedStartTerm: undefined,
        fixedEndTerm: undefined,
        productScope: PromotionProductScopeEnum.ALL.scope,
        productSpuIds: [],
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
      this.title = "添加优惠劵";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getCouponTemplate(id).then(response => {
        this.form = {
          ...response.data,
          discountPrice: response.data.discountPrice !== undefined ? response.data.discountPrice / 100.0 : undefined,
          discountPercent: response.data.discountPercent !== undefined ? response.data.discountPercent / 10.0 : undefined,
          discountLimitPrice: response.data.discountLimitPrice !== undefined ? response.data.discountLimitPrice / 100.0 : undefined,
          usePrice: response.data.usePrice !== undefined ? response.data.usePrice / 100.0 : undefined,
          validTimes: [response.data.validStartTime, response.data.validEndTime]
        }
        this.open = true;
        this.title = "修改优惠劵";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 金额相关字段的缩放
        let data = {
          ...this.form,
          discountPrice: this.form.discountPrice !== undefined ? this.form.discountPrice * 100 : undefined,
          discountPercent: this.form.discountPercent !== undefined ? this.form.discountPercent * 10 : undefined,
          discountLimitPrice: this.form.discountLimitPrice !== undefined ? this.form.discountLimitPrice * 100 : undefined,
          usePrice: this.form.usePrice !== undefined ? this.form.usePrice * 100 : undefined,
          validStartTime: this.form.validTimes && this.form.validTimes.length === 2 ? this.form.validTimes[0] : undefined,
          validEndTime: this.form.validTimes && this.form.validTimes.length === 2 ? this.form.validTimes[1] : undefined,
        }
        // 修改的提交
        if (this.form.id != null) {
          updateCouponTemplate(data).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createCouponTemplate(data).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 优惠劵模板状态修改 */
    handleStatusChange(row) {
      // 此时，row 已经变成目标状态了，所以可以直接提交请求和提示
      let text = row.status === CommonStatusEnum.ENABLE ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.name + '"优惠劵吗?').then(function() {
        return updateCouponTemplateStatus(row.id, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        // 异常时，需要将 row.status 状态重置回之前的
        row.status = row.status === CommonStatusEnum.ENABLE ? CommonStatusEnum.DISABLE
            : CommonStatusEnum.ENABLE;
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除优惠劵编号为"' + id + '"的数据项?').then(function() {
          return deleteCouponTemplate(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    // 格式化【优惠金额/折扣】
    discountFormat(row, column) {
      if (row.discountType === PromotionDiscountTypeEnum.PRICE.type) {
        return `￥${(row.discountPrice / 100.0).toFixed(2)}`;
      }
      if (row.discountType === PromotionDiscountTypeEnum.PERCENT.type) {
        return `￥${(row.discountPrice / 100.0).toFixed(2)}`;
      }
      return '未知【' + row.discountType + '】';
    },
    // 格式化【领取上限】
    takeLimitCountFormat(row, column) {
      if (row.takeLimitCount === -1) {
        return '无领取限制';
      }
      return `${row.takeLimitCount} 张/人`
    },
    // 格式化【有效期限】
    validityTypeFormat(row, column) {
      if (row.validityType === CouponTemplateValidityTypeEnum.DATE.type) {
        return `${parseTime(row.validStartTime)} 至 ${parseTime(row.validEndTime)}`
      }
      if (row.validityType === CouponTemplateValidityTypeEnum.TERM.type) {
        return `领取后第 ${row.fixedStartTerm} - ${row.fixedEndTerm} 天内可用`
      }
      return '未知【' + row.validityType + '】';
    }
  }
};
</script>
