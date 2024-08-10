package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.LogicDelete;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.ProductProxy;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hoysing
 * @date 2024-07-08 15:30
 * @since 1.0.0
 */
@EntityProxy
@Table
@Data
public class Product implements ProxyEntityAvailable<Product, ProductProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    String name;

    @LogicDelete(strategy = LogicDeleteStrategyEnum.CUSTOM, strategyName = "CustomLogicDelStrategy")
    LocalDateTime deletedTime;

    Integer deletedUserId;
}
