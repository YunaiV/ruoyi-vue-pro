package com.somle.home24.model.resp;

import lombok.Data;

import java.util.List;

@Data
public class Home24CommonRespInvoices<T> {
    Integer total_count;
    private List<T> invoices;
}
