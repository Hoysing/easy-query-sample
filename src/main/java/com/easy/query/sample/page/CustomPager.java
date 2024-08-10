package com.easy.query.sample.page;

import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.api.pagination.Pager;
import com.easy.query.core.basic.api.select.executor.PageAble;

/**
 * @author Hoysing
 * @date 2024-07-15 17:01
 * @since 1.0.0
 */
public class CustomPager<TEntity> implements Pager<TEntity, PageResult<TEntity>> {
    private final long pageIndex;
    private final long pageSize;
    private final long pageTotal;

    public CustomPager(long pageIndex, long pageSize) {
        this(pageIndex, pageSize, -1);
    }

    public CustomPager(long pageIndex, long pageSize, long pageTotal) {

        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.pageTotal = pageTotal;
    }

    @Override
    public PageResult<TEntity> toResult(PageAble<TEntity> query) {
        EasyPageResult<TEntity> pageResult = query.toPageResult(pageIndex, pageSize, pageTotal);
        return new CustomPageResult<>(pageResult.getTotal(), pageResult.getData());
    }
}
