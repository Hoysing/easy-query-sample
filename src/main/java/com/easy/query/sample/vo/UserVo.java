package com.easy.query.sample.vo;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityFileProxy;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.sample.entity.Company;
import com.easy.query.sample.entity.Role;
import com.easy.query.sample.entity.UserDetail;
import com.easy.query.sample.entity.UserRole;
import lombok.Data;

import java.util.List;

/**
 * @author Hoysing
 * @date 2024-07-11 17:17
 * @since 1.0.0
 */
@EntityProxy
@Data
public class UserVo {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    private String name;

    private String companyName;

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
