package com.easy.query.sample.page;

import java.util.List;


/**
 * @author Hoysing
 * @date 2024-07-15 17:01
 * @since 1.0.0
 */
public class CustomPageResult<TEntity> implements PageResult<TEntity> {
    private final long total;
    private final List<TEntity> list;

    public CustomPageResult(long total, List<TEntity> list) {
        this.total = total;
        this.list = list;
    }

    @Override
    public long getTotalCount() {
        return total;
    }

    @Override
    public List<TEntity> getList() {
        return list;
    }
}