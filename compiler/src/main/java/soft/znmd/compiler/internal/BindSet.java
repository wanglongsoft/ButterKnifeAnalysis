package soft.znmd.compiler.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import soft.znmd.annotation.OnClick;
import soft.znmd.annotation.internal.ListenerClass;
import soft.znmd.annotation.internal.ListenerMethod;

public class BindSet {

    private final TypeElement enclosingElement;
    private final TypeName targetTypeName;
    private final Messager messager;

    private Map<Integer, VariableElement> bindViewMap = new HashMap<>();
    private Map<Integer, ExecutableElement> bindOnClickMap = new HashMap<>();

    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName BUTTON = ClassName.get("android.widget", "Button");

    public BindSet(TypeElement enclosingElement, TypeName targetTypeName, Messager messager) {
        this.enclosingElement = enclosingElement;
        this.targetTypeName = targetTypeName;
        this.messager = messager;
    }

    public JavaFile brewJava() {
        TypeSpec bindingConfiguration = createType();
        return JavaFile.builder(getPackageName(), bindingConfiguration)
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        TypeSpec.Builder result = TypeSpec.classBuilder(getBindClassName())
                .addModifiers(Modifier.PUBLIC);

        result.addField(targetTypeName, "target", Modifier.PRIVATE);
        result.addMethod(createBindingConstructor());
        result.addMethod(createBindingConstructorForView());

        return result.build();
    }

    public String getPackageName() {
        String simpleName = enclosingElement.getSimpleName().toString();
        String enclosingQualifiedName = enclosingElement.getQualifiedName().toString();
        int index = enclosingQualifiedName.indexOf(simpleName);
        return enclosingQualifiedName.substring(0, index - 1);
    }

    public String getBindClassName() {
        return enclosingElement.getSimpleName().toString() + "_ViewBinding";
    }

    private MethodSpec createBindingConstructor() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetTypeName, "target");
        builder.addStatement("this(target, target.getWindow().getDecorView())");

        return builder.build();
    }

    private MethodSpec createBindingConstructorForView() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetTypeName, "target", Modifier.FINAL)
                .addParameter(VIEW, "source");

        builder.addStatement("this.target = target");
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (Map.Entry<Integer, VariableElement> entry : bindViewMap.entrySet()) {
            int resID = entry.getKey();
            VariableElement variableElement = entry.getValue();
            String simpleName = variableElement.getSimpleName().toString();
            codeBlock.add("target.$L = ", simpleName);
            codeBlock.add("($T) source.findViewById($L)",  TypeName.get(variableElement.asType()), resID);
            builder.addStatement("$L", codeBlock.build());
            if(bindOnClickMap.containsKey(resID)) {
                addBindOnClickListener(builder, resID, simpleName);
            }
        }
        for (Map.Entry<Integer, ExecutableElement> entry : bindOnClickMap.entrySet()) {
            int resID = entry.getKey();
            ExecutableElement executableElement = entry.getValue();
            if(!bindViewMap.containsKey(resID)) {
                addOnClickListener(builder, resID, executableElement);
            }
        }
        return builder.build();
    }

    public void dealBindView(int resID, VariableElement variableElement) {
        bindViewMap.put(resID, variableElement);
    }

    public void dealonClick(int resID, ExecutableElement executableElement) {
        bindOnClickMap.put(resID, executableElement);
    }

    public void addBindOnClickListener(MethodSpec.Builder builder, int resID, String simpleName) {
        printNoteMessege("addOnClickListener == start");
        ExecutableElement executableElement = bindOnClickMap.get(resID);
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("target.$L.setOnClickListener(new View.OnClickListener() {", simpleName)
                .add("\n")
                .add("@Override")
                .add("\n")
                .add("public void onClick(View view) {")
                .add("\n")
                .add("target.$L();", executableElement.getSimpleName().toString())
                .add("\n")
                .add("}")
                .add("\n")
                .add("})");
        builder.addStatement("$L", codeBlock.build());
        printNoteMessege("addOnClickListener == end");
    }

    public void addOnClickListener(MethodSpec.Builder builder, int resID, ExecutableElement executableElement) {
        Integer scrID = new Integer(resID);
        String buttonName = "mButton" + scrID.toString();
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("Button $L;", buttonName)
                .add("\n");
        codeBlock.add("$L = ($T) source.findViewById($L);", buttonName, BUTTON, resID)
                .add("\n");
        codeBlock.add("$L.setOnClickListener(new View.OnClickListener() {", buttonName)
                .add("\n")
                .add("@Override")
                .add("\n")
                .add("public void onClick(View view) {")
                .add("\n")
                .add("target.$L();", executableElement.getSimpleName().toString())
                .add("\n")
                .add("}")
                .add("\n")
                .add("})");
        builder.addStatement("$L", codeBlock.build());
    }

    public void printNoteMessege(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
