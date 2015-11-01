package org.michaelevans.aftermath;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

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
    private final Map<Integer, Map<Integer, MethodBinding>> bindings;

    public BindingClass(String classPackage, String className, String targetClass) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.bindings = new HashMap<>();
    }

    void createAndAddResultBinding(Element element, String annotationClass) {
        MethodBinding binding = MethodBinding.newInstance(element, annotationClass);
        Map<Integer, MethodBinding> methodBindings = bindings.get(binding.type);
        if (methodBindings == null) {
            methodBindings = new HashMap<>();
            bindings.put(binding.type, methodBindings);
        }

        if (methodBindings.containsKey(binding.requestCode)) {
            throw new IllegalStateException(String.format("Duplicate attr assigned for field %s and %s", binding.name,
                    methodBindings.get(binding.requestCode).name));
        } else {
            methodBindings.put(binding.requestCode, binding);
        }
    }

    void writeToFiler(Filer filer) throws IOException {
        ClassName targetClassName = ClassName.get(classPackage, targetClass);
        TypeSpec.Builder aftermath = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", targetClassName))
                .addMethod(generateOnActivityResultMethod())
                .addMethod(generateOnRequestPermissionResultMethod());

        ClassName callback = ClassName.get("org.michaelevans.aftermath", "IAftermathDelegate");
        aftermath.addSuperinterface(ParameterizedTypeName.get(callback,
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
        final Map<Integer, MethodBinding> methodBindings = bindings.get(MethodBinding.onActivityResult);
        if (methodBindings != null && !methodBindings.isEmpty()) {
            boolean first = true;
            for (MethodBinding binding : methodBindings.values()) {
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

    private MethodSpec generateOnRequestPermissionResultMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onRequestPermissionsResult")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(int.class, "requestCode", Modifier.FINAL)
                .addParameter(String[].class, "permissions", Modifier.FINAL)
                .addParameter(int[].class, "grantResults", Modifier.FINAL);

        final Map<Integer, MethodBinding> methodBindings = bindings
                .get(MethodBinding.onPermissionRequestResult);
        if (methodBindings != null && !methodBindings.isEmpty()) {
            boolean first = true;
            for (MethodBinding binding : methodBindings.values()) {
                if (first) {
                    builder.beginControlFlow("if (requestCode == $L)", binding.requestCode);
                    first = false;
                } else {
                    builder.nextControlFlow("else if (requestCode == $L)", binding.requestCode);
                }
                builder.addStatement("target.$L(permissions, grantResults)", binding.name);
            }
            builder.endControlFlow();
        }

        return builder.build();
    }

    private static class MethodBinding {

        public static int onActivityResult = 0;
        public static int onPermissionRequestResult = 1;
        final String name;
        final int requestCode;
        final int type;

        public MethodBinding(Element element, int requestCode, int type) {

            this.requestCode = requestCode;
            ExecutableElement executableElement = (ExecutableElement) element;
            name = executableElement.getSimpleName().toString();
            this.type = type;
        }

        public static MethodBinding newInstance(Element element, String annotationClass) {
            final int requestCode;
            final int type;
            if (annotationClass.equals(OnActivityResult.class.getSimpleName())) {
                final OnActivityResult instance = element.getAnnotation(OnActivityResult.class);
                requestCode = instance.value();
                type = MethodBinding.onActivityResult;
            } else {
                final OnRequestPermissionResult instance = element.getAnnotation(OnRequestPermissionResult.class);
                requestCode = instance.value();
                type = MethodBinding.onPermissionRequestResult;
            }
            return new MethodBinding(element, requestCode, type);
        }
    }

}
