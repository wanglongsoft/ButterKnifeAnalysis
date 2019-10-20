package soft.znmd.annotation.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)//必须是RUNTIME，否则注解嵌套时，该注解引用失败
public @interface ListenerMethod {
    String name();
    String parameters() default "";
    String returnType() default "void";
    String defaultReturn() default "null";
}
