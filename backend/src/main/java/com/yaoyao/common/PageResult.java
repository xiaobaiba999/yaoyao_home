package com.yaoyao.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private List<T> data;
    private long total;
    private int page;
    private boolean hasMore;

    public PageResult() {}

    public PageResult(List<T> data, long total, int page, int limit) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.hasMore = (long) page * limit < total;
    }
}
