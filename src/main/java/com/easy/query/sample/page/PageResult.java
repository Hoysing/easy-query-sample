package com.easy.query.sample.page;

import java.util.List;


/**
 * @author Hoysing
 * @date 2024-07-15 17:01
 * @since 1.0.0
 */
public interface PageResult<T> {
    /**
     * 返回总数
     *
     * @return
     */
    long getTotalCount();

    /**
     * 结果内容
     *
     * @return
     */
    List<T> getList();
}