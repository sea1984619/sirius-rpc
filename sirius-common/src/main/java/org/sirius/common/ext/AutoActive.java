package org.sirius.common.ext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 此注解表示被注解的类自动加载到应用中
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface AutoActive {

	/**
     * 服务端侧是否开启此功能
     *
     * @return is provider side auto active
     */
    boolean providerSide() default false;

    /**
     * 客户端侧是否开启此功能
     *
     * @return is consumer side auto active
     */
    boolean consumerSide() default false;
}
