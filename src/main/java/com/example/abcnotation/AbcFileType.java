package com.example.abcnotation;

import com.example.abcnotation.AbcLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

public class AbcFileType extends LanguageFileType {
    public static final AbcFileType INSTANCE = new AbcFileType();

    private AbcFileType() {
        super(AbcLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "ABC Notation File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "ABC music notation file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "abc";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/abc_icon.svg", AbcFileType.class);
    }
}