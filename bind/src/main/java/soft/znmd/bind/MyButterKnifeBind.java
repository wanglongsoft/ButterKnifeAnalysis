package soft.znmd.bind;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class MyButterKnifeBind {
    public static void bind(Activity target) {
        View sourceView = target.getWindow().getDecorView();
        bind(target, sourceView);
    }

    public static void bind( Activity target, View source) {
        Class<?> targetClass = target.getClass();
        String clsName = targetClass.getName();
        Class<?> bindingClass = null;
        try {
            bindingClass = targetClass.getClassLoader().loadClass(clsName + "_ViewBinding");
            if(null != bindingClass) {
                try {
                    Constructor<?> constructor = bindingClass.getConstructor(targetClass, View.class);
                    if(null != constructor) {
                        try {
                            constructor.newInstance(target, source);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
