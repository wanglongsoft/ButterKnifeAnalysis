package soft.znmd.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import soft.znmd.annotation.BindView;
import soft.znmd.annotation.OnClick;
import soft.znmd.annotation.internal.ListenerClass;
import soft.znmd.annotation.internal.ListenerMethod;
import soft.znmd.compiler.internal.BindSet;

@AutoService(Processor.class)
public class ComplierCustom extends AbstractProcessor {

    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    private Messager messager;//打印信息
    private Filer filer = null;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        printNoteMessege("init ======  Start");
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        printNoteMessege("init ======  End");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

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

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }

    public void printNoteMessege(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

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

    private Map<TypeElement, BindSet> findAndParseTargets(RoundEnvironment env) {
        printNoteMessege("findAndParseTargets ======  Start");
        Map<TypeElement, BindSet> builderMap = new LinkedHashMap<>();
        parseBindView(env, builderMap);
        parseOnClick(env, builderMap);
        printNoteMessege("findAndParseTargets ======  End");
        return builderMap;
    }
}
