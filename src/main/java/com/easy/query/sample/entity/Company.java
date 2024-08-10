package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.LogicDelete;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.CompanyProxy;
import com.easy.query.sample.plugin.UserNavigateExtraFilterStrategy;
import lombok.Data;

import java.util.List;

/**
 * @author Hoysing
 * @date 2024-07-08 15:30
 * @since 1.0.0
 */
@Table
@EntityProxy
@Data
public class Company implements ProxyEntityAvailable<Company, CompanyProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    String name;

    Integer parentId;

    @LogicDelete(strategy = LogicDeleteStrategyEnum.BOOLEAN)
    private Boolean deleted;

    @Navigate(value = RelationTypeEnum.OneToOne, selfProperty = "id", targetProperty = "companyId")
    private CompanyDetail companyDetail;

    @Navigate(value = RelationTypeEnum.OneToMany, selfProperty = "id", targetProperty = "companyId")
    private List<User> users;

    @Navigate(value = RelationTypeEnum.OneToMany, selfProperty = "id", targetProperty = "companyId", extraFilter = UserNavigateExtraFilterStrategy.class)
    private List<User> enabledUsers;
}
