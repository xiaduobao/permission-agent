package com.permission.agent.common;

import lombok.Data;

@Data
public class PageResult<T> {
    private long total;
    private java.util.List<T> records;

    public PageResult(long total, java.util.List<T> records) {
        this.total = total;
        this.records = records;
    }
}
