package com.github.leofalco.annotation.processor;

import com.github.leofalco.annotation.processor.util.MensagemHelper;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.leofalco.annotation.processor.util.Util.createColumnDefinitionStament;
import static com.github.leofalco.annotation.processor.util.Util.extractEnumConstantsOfElement;
import static javax.lang.model.element.Modifier.*;

@SupportedAnnotationTypes("javax.persistence.Enumerated")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class) // create a service registration on META-INF
public class ColumnDefinitionGenerator extends AbstractProcessor {
    private MensagemHelper log;

    // elemento  e definicao
    private Map<Element, FieldSpec> fields = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.log = new MensagemHelper(processingEnv.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty())
            return false;

        for (Element element : roundEnv.getElementsAnnotatedWith(Enumerated.class)) {

            // classe que envolve o elemento
            Element classe = element.getEnclosingElement();
            log.info("Processando elemento: " + classe + "." + element);

            // typeMirror aponta para o tipo de element
            TypeMirror typeMirror = extractEnumTypeOfElement(element);

            // element aponta para a definicao da classe
            element = processingEnv.getTypeUtils().asElement(typeMirror);
            log.info("Tipo descoberto: " + element);


            // filtra e retorna somente o nome das enums
            List<Element> enumConstants = extractEnumConstantsOfElement(element);

            String columnDefinition = createColumnDefinitionStament(enumConstants);
            log.info("definicao gerada: " + columnDefinition);

            fields.computeIfAbsent(element, e -> createField(e, columnDefinition));


        }

        JavaFile file = JavaFile
                .builder("column", TypeSpec.classBuilder("Def").addModifiers(PUBLIC, FINAL).addFields(fields.values()).build())
                .build();

        try {
            file.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            log.fatalError(e.getMessage());
        }


        System.out.println(processingEnv.getOptions());

        if (Boolean.parseBoolean(processingEnv.getOptions().get("lint"))) {
            for (Element element : roundEnv.getElementsAnnotatedWith(Enumerated.class)) {


                // valida enumerated
                Enumerated enumerated = element.getAnnotation(Enumerated.class);
                if (enumerated == null) {
                    log.error(element, "Necessário anotação @Enumerated em " + element);
                } else if (!EnumType.STRING.equals(enumerated.value())) {
                    log.error(element, "Necessário anotação @Enumerated(EnumType.STRING) em" + element);
                }

                Column column = element.getAnnotation(Column.class);
                if (column == null) {
                    log.error(element, "Necessário anotação @Column em " + element);
                } else {

                    String s = column.columnDefinition();

                    log.info(s);

                    // checar column definition
                    //System.out.println(annotationMirror.getElementValues());
                }


            }
        }
        return false;
    }

    private FieldSpec createField(Element element, String columnDefinition) {
        String fieldName = processingEnv.getElementUtils().getPackageOf(element).toString().replace(".", "_") + "_"
                + element.getSimpleName();

        return FieldSpec.builder(String.class, fieldName, PUBLIC, STATIC, FINAL)
                .initializer("\"" + columnDefinition + "\"").build();
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
                log.error(element, "@Enumerated não permitido em %s", element.getKind());
                return null;
        }

        DeclaredType genericCheck = (DeclaredType) typeMirror;

        if (!genericCheck.getTypeArguments().isEmpty()) {
            typeMirror = genericCheck.getTypeArguments().get(0);
        }

        return typeMirror;
    }


}