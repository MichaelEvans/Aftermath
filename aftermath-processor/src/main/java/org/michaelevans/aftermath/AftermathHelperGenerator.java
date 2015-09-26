package org.michaelevans.aftermath;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class AftermathHelperGenerator {

    private Map<TypeElement, BindingClass> targetClassMap;
    private final ClassName className = ClassName.get("org.michaelevans.aftermath", "Aftermath");

    public AftermathHelperGenerator(Map<TypeElement, BindingClass> targetClassMap) {
        this.targetClassMap = targetClassMap;
    }

    public TypeSpec createAftermathHelper() {
        TypeSpec.Builder aftermathBuilder = TypeSpec.classBuilder(className.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(generateOnActivityResultMethod());

        return aftermathBuilder.build();
    }

    public void writeToFiler(TypeSpec aftermathTypeSpec, Filer filer) throws IOException {
        JavaFile.builder(className.packageName(), aftermathTypeSpec).build().writeTo(filer);
    }

    private MethodSpec generateOnActivityResultMethod() {
        ClassName intentClass = ClassName.get("android.content", "Intent");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityResult")
                                               .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                               .returns(void.class)
                                               .addParameter(ParameterSpec.builder(Object.class,
                                                                                   "target",
                                                                                   Modifier.FINAL)
                                                                          .build())
                                               .addParameter(ParameterSpec.builder(int.class,
                                                                                   "requestCode",
                                                                                   Modifier.FINAL)
                                                                          .build())
                                               .addParameter(ParameterSpec.builder(int.class,
                                                                                   "resultCode",
                                                                                   Modifier.FINAL)
                                                                          .build())
                                               .addParameter(ParameterSpec.builder(intentClass,
                                                                                   "data",
                                                                                   Modifier.FINAL)
                                                                          .build());

        for (TypeElement type : targetClassMap.keySet()) {
            ClassName activityClass = ClassName.get(type);
            builder.beginControlFlow("if(target instanceof $T)", activityClass);
            BindingClass bindingClass = targetClassMap.get(type);
            ClassName generatedAftermathClass = ClassName.get(bindingClass.getClassPackage(),
                                                              bindingClass.getClassName());
            builder.addStatement("$T.onActivityResult(($T) target, requestCode, resultCode, data)",
                                 generatedAftermathClass, activityClass);
            builder.endControlFlow();
        }
        return builder.build();
    }
}
