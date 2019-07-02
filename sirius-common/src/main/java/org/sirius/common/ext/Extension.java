package org.sirius.common.ext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展点
 *
 * @see Extensible
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Extension {
    /**
     * 扩展点名字
     *
     * @return 扩展点名字
     */
    String value();

    /**
     * 扩展类是否使用单例，默认使用
     *
     * @return 是否使用单例
     */
    boolean singleton() default true;
    
    /**
     * 扩展点编码，默认不需要，当接口需要编码的时候需要
     *
     * @return 扩展点编码
     * @see Extensible#coded()
     */
    byte code() default -1;

    /**
     * 优先级排序，默认不需要，大的优先级高
     *
     * @return 排序
     */
    int order() default 0;

    /**
     * 是否覆盖其它低{@link #order()}的同名扩展
     *
     * @return 是否覆盖其它低排序的同名扩展
     */
    boolean override() default false;

    /**
     * 排斥其它扩展，可以排斥掉其它低{@link #order()}的扩展
     *
     * @return 排斥其它扩展
     */
    String[] rejection() default {};
}
