package com.github.thrift.consumer;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.FIELD,java.lang.annotation.ElementType.METHOD })
public @interface ThriftReference 
{
	String version() default "1.0.0";
}
