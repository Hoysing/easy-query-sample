package com.easy.query.sample.vo;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityFileProxy;
import com.easy.query.core.annotation.EntityProxy;
import lombok.Data;

/**
 * @author Hoysing
 * @date 2024-07-19 09:05
 * @since 1.0.0
 */
@EntityProxy
@Data
public class UserDetailVo {
    @Column(primaryKey = true, generatedKey = true)
    Integer id;

    String name;

    String signature;
}
