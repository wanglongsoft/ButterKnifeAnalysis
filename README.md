# ButterKnifeAnalysis
&emsp;&emsp;ButterKnife原理解析以及APT简单使用
## ButterKnife简介
&emsp;&emsp;ButterKnife是一个专注于Android系统的View注入框架,使用相关注解可以自动实现View的绑定和事件的注入，注解一般分为两类，运行时注解(反射)，编译时注
解(APT)，运行时注解由于消耗一部分性能，一直为人诟病，编译时注解运用APT技术解析注解，编译完成时已经完成View的绑定和事件的注入，不会影响性能，使用
较为广泛
## 本工程简介
&emsp;&emsp;该Demo原理与ButterKnife原理相同，可以看做是ButterKnife的一个简易模型，由于ButterKnife功能很多，源码量较大，该Deom仅仅实现View的绑定和onClick
事件的注入，如果想了解全部功能，可参照源码：[ButterKnife](https://github.com/JakeWharton/butterknife)，使用较为简单，工程引入该框架的方法较为简单，在工程
的build.gradle添加依赖
```java
  implementation 'com.jakewharton:butterknife:10.2.0'
  annotationProcessor 'com.jakewharton:butterknife-compiler:10.2.0'
```
&emsp;&emsp;ButterKnife完成View的绑定，有三个重要组成部分: 注解处理(annotationProcessor)，注解定义，注入注解(ButterKnife.bind(this);),该
Demo分别新建三个Module完成这三个功能，annotation(java-library, 定义注解)，compiler(java-library，注解处理器)，bind(android-library，注入
注解)，并在工程中调用这些模块
## 注解处理器(APT)
&emsp;&emsp;自定义注解处理器需要继承AbstractProcessor，该抽象类有４个常用方法　　
1. init(ProcessingEnvironment processingEnv)　所有的注解处理器类都必须有一个无参构造函数。然而，有一个特殊的方法init()，它会被注解处理工具调
用，以ProcessingEnvironment作为参数，ProcessingEnvironment 提供了一些实用的工具类Elements, Types和Filer，Messager(打印信息)
2. getSupportedAnnotationTypes()　配置需要处理的注解，只有配置了的注解才会走 process() 方法
3. getSupportedSourceVersion() 配置支持的JDK版本
4. process() 进行解析注解，创建文件操作，相当于程序的主函数
####  1. Module依赖配置
```java
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    compileOnly 'com.google.auto.service:auto-service:1.0-rc6'
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'
    compile 'com.squareup:javapoet:1.11.1'
    implementation project(path: ':annotation')
}
```
####  2. BindView注解解析
```java
void parseBindView(RoundEnvironment roundEnv,  Map<TypeElement, BindSet> builderMap) {
    printNoteMessege("parseBindView ======  Start");
    for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        VariableElement variableElement = (VariableElement) element;
        int valueID = element.getAnnotation(BindView.class).value();
        TypeMirror elementType = enclosingElement.asType();
        TypeName typeName = TypeName.get(elementType);
        if (!builderMap.containsKey(enclosingElement)) {
            builderMap.put(enclosingElement, new BindSet(enclosingElement, typeName, messager));
        }
        BindSet bindSet = builderMap.get(enclosingElement);
        bindSet.dealBindView(valueID, variableElement);
    }
    printNoteMessege("parseBindView ======  End");
}
####  3. OnClickw注解解析
```java
void parseOnClick(RoundEnvironment roundEnv, Map<TypeElement, BindSet> builderMap) {
    printNoteMessege("parseOnClick ======  Start");
    for (Element element : roundEnv.getElementsAnnotatedWith(OnClick.class)) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        ExecutableElement executableElement = (ExecutableElement) element;
        int valueID = executableElement.getAnnotation(OnClick.class).value();
        TypeMirror elementType = enclosingElement.asType();
        TypeName typeName = TypeName.get(elementType);
        if (!builderMap.containsKey(enclosingElement)) {
            builderMap.put(enclosingElement, new BindSet(enclosingElement, typeName, messager));
        }
        BindSet bindSet = builderMap.get(enclosingElement);
        bindSet.dealonClick(valueID, executableElement);
    }
    printNoteMessege("parseOnClick ======  End");
}
```
####  4. 文件生成
```java
@Override
public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    printNoteMessege("process ======  Start");
    Map<TypeElement, BindSet> builderMap = findAndParseTargets(roundEnv);
    for (Map.Entry<TypeElement, BindSet> entry : builderMap.entrySet()) {
        BindSet binding = entry.getValue();
        JavaFile javaFile = binding.brewJava();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    printNoteMessege("process ======  End");
    return false;
}
private Map<TypeElement, BindSet> findAndParseTargets(RoundEnvironment env) {
    printNoteMessege("findAndParseTargets ======  Start");
    Map<TypeElement, BindSet> builderMap = new LinkedHashMap<>();
    parseBindView(env, builderMap);
    parseOnClick(env, builderMap);
    printNoteMessege("findAndParseTargets ======  End");
    return builderMap;
}
```
####  5. APT注解解析常用知识介绍
* PackageElement表示一个包程序元素，提供对有关包及其成员的信息的访问
* ExecutableElement表示某个类或接口的方法、构造函数或者初始化程序
* TypeElement表示一个类或者接口的程序元素，提供对有关类型及其成员的信息的访问。
* VariableElement表示一个字段、enum常量、方法或者构造方法参数、局部变量等等
* getEnclosedElements() 返回该元素直接包含的子元素
* getEnclosingElement() 返回包含该element的父element，与上一个方法相反
* getkind() 返回该element的类型，判断是哪种element
* getModifiers() 返回修饰的关键字，如：public，static，final等等
* getSimpleName() 返回名字，不带包名
* getQualifiedName() 获取全名，如果是类，包含完整的包名路径
* getReturnType() 获取方法元素的返回值
* getParameters() 获取方法的参数元素，每一个元素都是VariableElement
## 注解定义
#### BindView注解
```java
@Retention(RetentionPolicy.CLASS)　 //编译期注解
@Target(ElementType.FIELD)　　//作用于变量
public @interface BindView {
    int value();
}
```
#### OnClick注解
```java
@Target(ElementType.METHOD)　　//作用于方法
@Retention(RetentionPolicy.CLASS)　 //编译期注解
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
```
ListenerClass注解实现
```java
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)//必须是RUNTIME，否则注解嵌套时，该注解引用失败
public @interface ListenerClass {
    String targetType();
    String setter();
    String type();
    ListenerMethod method();
}
```
ListenerMethod注解实现
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)//必须是RUNTIME，否则注解嵌套时，该注解引用失败
public @interface ListenerMethod {
    String name();
    String parameters() default "";
    String returnType() default "void";
    String defaultReturn() default "null";
}
```
## 注入注解bind
#### bind函数实现
```java
public static void bind(Activity target) { //参数是Activity，可通过Activity引用画面的变量和方法
    View sourceView = target.getWindow().getDecorView();
    bind(target, sourceView);
}    
public static void bind( Activity target, View source) {
    Class<?> targetClass = target.getClass();
    String clsName = targetClass.getName();
    Class<?> bindingClass = null;
    try {
        bindingClass = targetClass.getClassLoader().loadClass(clsName + "_ViewBinding");
        //加载clsName + "_ViewBinding　类，该类由注解处理器自动生成
        //自动生成类路径：app⁩/build/generated/⁨source⁩/apt⁩/debug/⁨soft⁩/znmd/butterknifeanalysis⁩
        //自动生成类名称： MainActivity_ViewBinding.java
        if(null != bindingClass) {
            try {
            　　//获取类的构造方法
                Constructor<?> constructor = bindingClass.getConstructor(targetClass, View.class);
                if(null != constructor) {
                    try {
                        constructor.newInstance(target, source);//新建一个实例
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
```
## 画面调用
主Activity
```java
    @BindView(R.id.button)
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnifeBind.bind(this); //画面注入
    }

    @OnClick(R.id.button)
    void doClick() {
        Log.d(TAG, "doClick: mButton");
    }

    @OnClick(R.id.button_view)
    void doButtonClick() {
        Log.d(TAG, "doButtonClick: ");
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SecondActivity.class);
        startActivity(intent);
    }
```
自动生成的MainActivity_ViewBinding.java
```java
public MainActivity_ViewBinding(final MainActivity target, View source) {
  this.target = target;
  target.mButton = (Button) source.findViewById(2131165250);
  target.mButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
      target.doClick();
      }
      });
  Button mButton2131165253;
      mButton2131165253 = (Button) source.findViewById(2131165253);
      mButton2131165253.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
      target.doButtonClick();
      }
      });
}
```
