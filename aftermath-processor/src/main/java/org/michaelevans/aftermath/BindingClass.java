package org.michaelevans.aftermath;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

final class BindingClass {

    private final String classPackage;
    private final String className;
    private final String targetClass;
    private final Map<Integer, OnActivityResultBinding> activityResultBindings;

    public BindingClass(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.activityResultBindings = new HashMap<>();
    }

    void createAndAddResultBinding(Element element) {
        OnActivityResultBinding binding = new OnActivityResultBinding(element);
        if (activityResultBindings.containsKey(binding.requestCode)) {
            throw new IllegalStateException(String.format("Duplicate attr assigned for field %s and %s", binding.name,
                    activityResultBindings.get(binding.requestCode).name));
        } else {
            activityResultBindings.put(binding.requestCode, binding);
        }
    }

    void writeToFiler(Filer filer) throws IOException {
        ClassName targetClassName = ClassName.get(classPackage, targetClass);
        TypeSpec.Builder aftermath = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(generateOnActivityResultMethod(targetClassName));

        JavaFile javaFile = JavaFile.builder(classPackage, aftermath.build()).build();
        javaFile.writeTo(filer);
    }

    private MethodSpec generateOnActivityResultMethod(ClassName targetClassName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ParameterSpec.builder(targetClassName, "target", Modifier.FINAL).build())
                .addParameter(int.class, "requestCode", Modifier.FINAL)
                .addParameter(int.class, "resultCode", Modifier.FINAL)
                .addParameter(ClassName.get("android.content", "Intent"), "data", Modifier.FINAL);

        if (!activityResultBindings.isEmpty()) {
            boolean first = true;
            for (OnActivityResultBinding binding : activityResultBindings.values()) {
                if (first) {
                    builder.beginControlFlow("if (requestCode == $L)", binding.requestCode);
                    first = false;
                } else {
                    builder.nextControlFlow("else if (requestCode == $L)", binding.requestCode);
                }
                builder.addStatement("target.$L(resultCode, data)", binding.name);
            }
            builder.endControlFlow();
        }

        return builder.build();
    }

    public String getClassPackage() {
        return classPackage;
    }

    public String getClassName() {
        return className;
    }

    private class OnActivityResultBinding {

        final String name;
        final int requestCode;

        public OnActivityResultBinding(Element element) {
            OnActivityResult instance = element.getAnnotation(OnActivityResult.class);
            this.requestCode = instance.value();

            ExecutableElement executableElement = (ExecutableElement) element;
            name = executableElement.getSimpleName().toString();
        }
    }
}
