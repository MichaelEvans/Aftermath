package org.michaelevans.aftermath;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

final class BindingClass {

    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final Map<String, OnActivityResultBinding> activityResultBindings;

    public BindingClass(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.activityResultBindings = new HashMap<>();
    }

    void createAndAddResultBinding(Element element) {
        OnActivityResultBinding binding = new OnActivityResultBinding(element);
        if (activityResultBindings.containsKey(binding.name)) {
            throw new IllegalStateException(String.format("Duplicate attr assigned for field %s and %s", binding.name,
                    activityResultBindings.get(binding.name)));
        } else {
            activityResultBindings.put(binding.name, binding);
        }
    }

    void writeToFiler(Filer filer) throws IOException {
        ClassName targetClassName = ClassName.get(classPackage, targetClass);
        TypeSpec.Builder aftermath = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", targetClassName))
                .addMethod(generateOnActivityResultMethod());

        aftermath.addSuperinterface(ParameterizedTypeName.get(ClassName.get(Aftermath.IOnActivityForResult.class),
                TypeVariableName.get("T")));

        JavaFile javaFile = JavaFile.builder(classPackage, aftermath.build()).build();
        javaFile.writeTo(filer);
    }

    private MethodSpec generateOnActivityResultMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(int.class, "requestCode", Modifier.FINAL)
                .addParameter(int.class, "resultCode", Modifier.FINAL)
                .addParameter(ClassName.get("android.content", "Intent"), "data", Modifier.FINAL);

        if (!activityResultBindings.isEmpty()) {
            for (OnActivityResultBinding binding : activityResultBindings.values()) {
                builder.beginControlFlow("if ((requestCode == $L) && (resultCode == $L))",
                        binding.requestCode, binding.resultCode);

                if (binding.hasIntentParam()) {
                    builder.addStatement("target.$L(data)", binding.name);
                } else {
                    builder.addStatement("target.$L()", binding.name);
                }

                builder.endControlFlow();
            }
        }

        return builder.build();
    }

    private class OnActivityResultBinding {

        final String name;
        final int requestCode;
        final int resultCode;
        final List<? extends VariableElement> methodParams;

        public OnActivityResultBinding(Element element) {
            OnActivityResult instance = element.getAnnotation(OnActivityResult.class);
            this.requestCode = instance.value();
            this.resultCode = instance.resultCode();

            ExecutableElement executableElement = (ExecutableElement) element;
            name = executableElement.getSimpleName().toString();

            methodParams = executableElement.getParameters();
        }

        public boolean hasIntentParam() {
            for (VariableElement element : methodParams) {
                if ("android.content.Intent".equals(element.asType().toString())) {
                    return true;
                }
            }
            return false;
        }
    }
}
