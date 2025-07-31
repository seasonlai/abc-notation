package com.example.abcnotation;

import com.intellij.lang.Language;

public class AbcLanguage extends Language {
    public static final AbcLanguage INSTANCE = new AbcLanguage();

    private AbcLanguage() {
        super("ABC");
    }
}