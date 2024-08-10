package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.UserRoleProxy;
import lombok.Data;

/**
 * @author Hoysing
 * @date 2024-07-08 15:30
 * @since 1.0.0
 */
@Table
@EntityProxy
@Data
public class UserRole implements ProxyEntityAvailable<UserRole, UserRoleProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    Integer userId;

    Integer roleId;
}
