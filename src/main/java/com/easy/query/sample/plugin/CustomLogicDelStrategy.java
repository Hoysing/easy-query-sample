package com.easy.query.sample.plugin;

import com.easy.query.core.basic.extension.logicdel.LogicDeleteBuilder;
import com.easy.query.core.basic.extension.logicdel.abstraction.AbstractLogicDeleteStrategy;
import com.easy.query.core.expression.lambda.SQLExpression1;
import com.easy.query.core.expression.parser.core.base.ColumnSetter;
import com.easy.query.core.expression.parser.core.base.WherePredicate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomLogicDelStrategy extends AbstractLogicDeleteStrategy {
    @Override
    protected SQLExpression1<WherePredicate<Object>> getPredicateFilterExpression(LogicDeleteBuilder builder, String propertyName) {
        return o -> o.isNull(propertyName);
    }

    @Override
    protected SQLExpression1<ColumnSetter<Object>> getDeletedSQLExpression(LogicDeleteBuilder builder, String propertyName) {
        return o -> o.set(propertyName, LocalDateTime.now())
                .set("deletedUserId", 1);
    }

    @Override
    public String getStrategy() {
        return "CustomLogicDelStrategy";
    }

    @Override
    public Set<Class<?>> allowedPropertyTypes() {
        return new HashSet<>(Arrays.asList(LocalDateTime.class));
    }
}