package org.michaelevans.aftermath;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.Collections;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public final class AftermathProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    public Elements elementUtils;
    private boolean aftermathHelperGenerated;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(OnActivityResult.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BindingClass> targetClassMap = new LinkedHashMap<TypeElement, BindingClass>();
        Set<String> erasedTargetNames = new LinkedHashSet<String>();

        for (Element element : roundEnv.getElementsAnnotatedWith(OnActivityResult.class)) {
            try {
                if (element.getKind() != ElementKind.METHOD) {
                    error(element, "OnActivityResult annotations can only be applied to methods!");
                    return false;
                }
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                BindingClass bindingClass = getOrCreateAftermath(targetClassMap, enclosingElement, erasedTargetNames);
                bindingClass.createAndAddResultBinding(element);
            } catch (Exception e) {
                error(element, "Unable to generate activity result binder.\n\n%s", e.getMessage());
            }
        }

        for (BindingClass bindingClass : targetClassMap.values()) {
            try {
                bindingClass.writeToFiler(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        if(!aftermathHelperGenerated){
            AftermathHelperGenerator aftermathHelperGenerator = new AftermathHelperGenerator(targetClassMap);
            try {
                aftermathHelperGenerator.writeToFiler(aftermathHelperGenerator.createAftermathHelper(), filer);
                aftermathHelperGenerated= true;
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    private BindingClass getOrCreateAftermath(Map<TypeElement, BindingClass> targetClassMap,
                                              TypeElement enclosingElement, Set<String> erasedTargetNames) {
        BindingClass bindingClass = targetClassMap.get(enclosingElement);
        if (bindingClass == null) {
            String targetType = enclosingElement.getQualifiedName().toString();
            String classPackage = getPackageName(enclosingElement);
            String className = getClassName(enclosingElement, classPackage) + "$$Aftermath";
            bindingClass = new BindingClass(classPackage, className, targetType);
            targetClassMap.put(enclosingElement, bindingClass);
            erasedTargetNames.add(enclosingElement.toString());
        }

        return bindingClass;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }
}
