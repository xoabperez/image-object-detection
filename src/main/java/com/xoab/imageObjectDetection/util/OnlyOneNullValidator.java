package com.xoab.imageObjectDetection.util;

import java.util.Arrays;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
public class OnlyOneNullValidator implements ConstraintValidator<OnlyOneNull, Object> {
    private String[] fields;

    @Override
    public void initialize(final OnlyOneNull combinedNotNull) {
        fields = combinedNotNull.fields();
    }

    @Override
    public boolean isValid(final Object obj, final ConstraintValidatorContext context) {
        final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(obj);

        return Arrays.stream(fields)
                .map(beanWrapper::getPropertyValue)
                .filter(Objects::isNull)
                .count()
                == 1;
    }
}