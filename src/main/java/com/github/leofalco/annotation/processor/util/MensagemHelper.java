package com.github.leofalco.annotation.processor.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public final class MensagemHelper {

    private final Messager messager;

    public MensagemHelper(Messager messager) {
        this.messager = messager;
    }

    public void fatalError(String msg, Object ... args) {
        messager.printMessage(Kind.ERROR, String.format("FATAL ERROR: " + msg, args));
    }

    public void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Kind.ERROR,
                String.format(msg, args),
                e);
    }

    public void info(String msg, Object... args) {
        messager.printMessage(
                Kind.NOTE,
                String.format(msg, args));
    }


}
