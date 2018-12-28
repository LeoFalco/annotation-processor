package com.github.leofalco.annotation.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.persistence.Enumerated;
import javax.tools.Diagnostic.Kind;

import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("javax.persistence.Enumerated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ColumnDefinitionValidator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (annotations.isEmpty()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Enumerated.class)) {
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();

            }

        }

        return false;
    }

    
  
}
