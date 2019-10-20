package soft.znmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import soft.znmd.annotation.internal.ListenerClass;
import soft.znmd.annotation.internal.ListenerMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@ListenerClass(
        targetType = "android.view.View",
        setter = "setOnClickListener",
        type = "View.OnClickListener",
        method = @ListenerMethod(
                name = "onClick"
        )
)
public @interface OnClick {
    int value();
}
