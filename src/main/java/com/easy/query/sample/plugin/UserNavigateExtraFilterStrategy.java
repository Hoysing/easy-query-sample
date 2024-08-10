package com.easy.query.sample.plugin;

import com.easy.query.core.basic.extension.navigate.NavigateBuilder;
import com.easy.query.core.basic.extension.navigate.NavigateExtraFilterStrategy;
import com.easy.query.core.expression.lambda.SQLExpression1;
import com.easy.query.core.expression.parser.core.base.WherePredicate;
import com.easy.query.core.metadata.EntityMetadata;
import com.easy.query.sample.entity.Company;

import java.util.Objects;

public class UserNavigateExtraFilterStrategy implements NavigateExtraFilterStrategy {
    @Override
    public SQLExpression1<WherePredicate<?>> getPredicateFilterExpression(NavigateBuilder builder) {
        //parentType
        EntityMetadata entityMetadata = builder.getNavigateOption().getEntityMetadata();
        //导航属性类型
        Class<?> navigatePropertyType = builder.getNavigateOption().getNavigatePropertyType();
        //导航属性名称
        String propertyName = builder.getNavigateOption().getPropertyName();
        if (Objects.equals(Company.class, entityMetadata.getEntityClass())) {
            //关联查询enabledUsers时添加已启用的状态
            if (Objects.equals("enabledUsers", propertyName)) {
                return o -> o.eq("enabled", 1);
            }
        }
        throw new IllegalArgumentException();
    }
}

