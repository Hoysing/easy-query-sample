package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.LogicDelete;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.UserProxy;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Hoysing
 * @date 2024-07-08 15:30
 * @since 1.0.0
 */
@Table
@EntityProxy
@Data
public class User implements ProxyEntityAvailable<User, UserProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    String name;

    Date createTime;

    Date updateTime;

    BigDecimal balance;

    Integer version;

    Boolean enabled;

    @LogicDelete(strategy = LogicDeleteStrategyEnum.BOOLEAN)
    private Boolean deleted;

    Integer companyId;

    @Navigate(value = RelationTypeEnum.ManyToOne, selfProperty = "companyId", targetProperty = "id")
    private Company company;

    @Navigate(value = RelationTypeEnum.OneToOne, selfProperty = "id", targetProperty = "userId")
    private UserDetail userDetail;

    @Navigate(value = RelationTypeEnum.ManyToMany,
            mappingClass = UserRole.class,
            selfProperty = "id",
            selfMappingProperty = "userId",
            targetProperty = "id",
            targetMappingProperty = "roleId")
    private List<Role> roles;
}
