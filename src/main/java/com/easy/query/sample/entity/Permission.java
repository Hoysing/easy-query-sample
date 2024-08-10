package com.easy.query.sample.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.easy.query.sample.entity.proxy.PermissionProxy;
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
public class Permission implements ProxyEntityAvailable<Permission, PermissionProxy> {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    String name;

    @Navigate(value = RelationTypeEnum.ManyToMany,
            mappingClass = RolePermission.class,
            selfProperty = "id",
            selfMappingProperty = "permissionId",
            targetProperty = "id",
            targetMappingProperty = "roleId")
    private List<Role> roles;
}
