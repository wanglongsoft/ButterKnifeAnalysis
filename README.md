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
## 注解处理器(APT)有
&emsp;&emsp;自定义注解处理器需要继承AbstractProcessor，该抽象类有４个常用方法　　
init(ProcessingEnvironment processingEnv)　所有的注解处理器类都必须有一个无参构造函数。然而，有一个特殊的方法init()，它会被注解处理工具调
用，以ProcessingEnvironment作为参数，ProcessingEnvironment 提供了一些实用的工具类Elements, Types和Filer，Messager(打印信息)
