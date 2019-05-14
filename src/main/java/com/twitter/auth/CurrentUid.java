package com.twitter.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 用于controller的方法参数注解，鉴权后的接口会自动注入当前用户的uid.
 * 【注意】uid参数类型必须为{@link Integer}，原始类型会出错
 * </pre>
 * @author Jam
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUid {

}
