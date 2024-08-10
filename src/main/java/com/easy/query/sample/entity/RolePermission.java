package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.RolePermissionProxy;
import lombok.Data;

/**
 * @author Hoysing
 * @date 2024-07-08 15:30
 * @since 1.0.0
 */
@Table
@EntityProxy
@Data
public class RolePermission implements ProxyEntityAvailable<RolePermission, RolePermissionProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    Integer roleId;

    Integer permissionId;
}
