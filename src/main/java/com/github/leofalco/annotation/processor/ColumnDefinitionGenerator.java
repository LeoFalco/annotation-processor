package com.github.leofalco.annotation.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;


@SupportedAnnotationTypes("javax.persistence.Enumerated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class) // create a service registration on META-INF
public class ColumnDefinitionGenerator extends AbstractProcessor {
    private static final Logger log = Logger.getLogger(ColumnDefinitionGenerator.class.getName());

    private List<FieldSpec> fields = new ArrayList<>();


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return false;

        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement("javax.persistence.Enumerated");
        Set<? extends Element> elementsAnnotatedWithEnumerated = roundEnv.getElementsAnnotatedWith(typeElement);

        for (Element element : elementsAnnotatedWithEnumerated) {

            // classe que envolve o elemento
            Element classe = element.getEnclosingElement();
            System.out.println("[INFO] Processando elemento: " + classe + "." + element);

            // typeMirror aponta para o tipo de element
            TypeMirror typeMirror = extractEnumTypeOfElement(element);

            // element aponta para a definicao da classe
            element = processingEnv.getTypeUtils().asElement(typeMirror);
            System.out.println("[INFO] tipo descoberto: " + element);

            // filtra e retorna somente o nome das enums
            List<Element> enumConstants = extractEnumConstantsOfElement(element);

            String columnDefinition = createColumnDefinitionStament(enumConstants);
            System.out.println("[INFO] columnDefinition: " + columnDefinition);


            fields.add(createField(element, columnDefinition));

        }


        JavaFile file = JavaFile.builder(
                "column",
                TypeSpec.classBuilder("Def")
                        .addModifiers(PUBLIC, FINAL)
                        .addFields(fields)
                        .build())
                .build();

        try {
            file.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error(null, "Erro: %s", e.getMessage());
        }


        return false;
    }

    private FieldSpec createField(Element element, String columnDefinition) {
        String fieldName = processingEnv.getElementUtils().getPackageOf(element).toString().replace(".", "_") + "_" + element.getSimpleName();

        return FieldSpec
                .builder(String.class, fieldName,
                        PUBLIC,
                        STATIC,
                        FINAL)
                .initializer("\"" + columnDefinition + "\"").build();
    }

    private List<Element> extractEnumConstantsOfElement(Element element) {


        // pega os campos metodos e constantes de element
        List<? extends Element> enclosedElements = element.getEnclosedElements();

        // filtra e retorna somente o nome das enums
        return enclosedElements.stream()
                .filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                .collect(Collectors.toList());

    }


    private TypeMirror extractEnumTypeOfElement(Element element) {
        TypeMirror typeMirror = null;
        switch (element.getKind()) {
            case FIELD:
                typeMirror = element.asType();
                break;

            case METHOD:
                ExecutableElement getter = (ExecutableElement) element;
                typeMirror = getter.getReturnType();
                break;

            default:
                error(element, "@Enumerated não permitido em %s", element.getKind());
                return null;
        }

        DeclaredType genericCheck = (DeclaredType) typeMirror;

        if (!genericCheck.getTypeArguments().isEmpty()) {
            typeMirror = genericCheck.getTypeArguments().get(0);
        }

        return typeMirror;
    }

    private static String createColumnDefinitionStament(List<Element> values) {
        return values.stream()
                .map(element -> element.getSimpleName().toString())
                .collect(Collectors.joining(
                        "', '", "enum('", "') NOT NULL DEFAULT '" + values.get(0) + "'"));
    }

    private void error(Element e, String msg, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}