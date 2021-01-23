package com.ruoyi.framework.web.domain.server;

import com.ruoyi.common.utils.Arith;

/**
 * 內存相关信息
 *
 * @author ruoyi
 */
public class Mem {
    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    public double getTotal() {
        return Arith.div(total, (1024 * 1024 * 1024), 2);
    }

    public double getUsed() {
        return Arith.div(used, (1024 * 1024 * 1024), 2);
    }

    public double getFree() {
        return Arith.div(free, (1024 * 1024 * 1024), 2);
    }

    public double getUsage() {
        return Arith.mul(Arith.div(used, total, 4), 100);
    }

}
