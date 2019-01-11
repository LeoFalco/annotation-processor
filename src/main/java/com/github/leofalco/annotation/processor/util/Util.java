package com.github.leofalco.annotation.processor.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static String createColumnDefinitionStament(List<Element> values) {
        return values.stream().map(element -> element.getSimpleName().toString())
                .collect(Collectors.joining("', '", "enum('", "') NOT NULL DEFAULT '" + values.get(0) + "'"));
    }

    public static List<Element> extractEnumConstantsOfElement(Element element) {

        // pega os campos metodos e constantes de element
        List<? extends Element> enclosedElements = element.getEnclosedElements();

        // filtra e retorna somente o nome das enums
        return enclosedElements.stream().filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
                .collect(Collectors.toList());

    }


}
