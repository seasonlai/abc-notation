package com.example.abcnotation;

import com.example.abcnotation.utils.FileUtils;
import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AbcPreviewToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 检查 JCEF 是否支持当前环境
        if (!JBCefApp.isSupported()) {
            showWarnTips(project, "JCEF 不支持", "当前环境不支持 JCEF，尝试使用替代方案预览。");
            return;
        }

        // 创建刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refresh(project, toolWindow));

        // 创建面板，用于放置按钮和浏览器组件
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(refreshButton, BorderLayout.NORTH);

        JBCefBrowser browser = new JBCefBrowser();

        Disposer.register(project, browser);

        browser.openDevtools();
//        JBScrollPane scrollPane = new JBScrollPane(browser.getComponent());
        // 创建一个新的选项卡来显示 DevTools
//        JBCefBrowser devToolsBrowser = JBCefBrowser.createBuilder()
//                .setCefBrowser(browser.getCefBrowser().getDevTools())
//                .setClient(browser.getJBCefClient())
//                .build();
//        devToolsBrowser.openDevtools();
//        JBScrollPane devToolsScrollPane = new JBScrollPane(devToolsBrowser.getComponent());
        panel.add(browser.getComponent(), BorderLayout.CENTER);

        ContentFactory contentFactoryInstance = ContentFactory.getInstance();

        Content panelContent = contentFactoryInstance.createContent(panel, "", false);
        Key<JBCefBrowser> ABC_BROWSER_KEY = Key.create("ABC_BROWSER");
        panelContent.putUserData(ABC_BROWSER_KEY, browser);
        toolWindow.getContentManager().addContent(panelContent);

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            showWarnTips(project, "editor为空", "......");
            return;
        }

        Document document = editor.getDocument();
//        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        VirtualFile file = editor.getVirtualFile();
        if (file == null || !"abc".equalsIgnoreCase(file.getExtension())) {
            showWarnTips(project, "文件为空或不是abc文件", "......");
            return;
        }


        String abcContent = document.getText();
//        System.out.println("文件内容: " + abcContent);

        String html = FileUtils.loadTextFromResource("web/player.html");
        String jsContent = FileUtils.loadTextFromResource("web/js/abcjs-basic-min.js");
        String screenCssContent = FileUtils.loadTextFromResource("web/css/preview-styles.css");
        String audioCssContent = FileUtils.loadTextFromResource("web/css/abcjs-audio.css");
        html = html.replace("{abc_content}", abcContent) //.replace("\n", "\\n")
                .replace("{script_placeholder}", jsContent)
                .replace("{style_screen_placeholder}", screenCssContent)
                .replace("{style_audio_placeholder}", audioCssContent);

        browser.loadHTML(html);
        FileUtils.dumpHtml(project, html);
//        browser.loadURL("https://editor.drawthedots.com/");

        showTips(project, "刷新完成", "");
    }

    private void refresh(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 移除旧内容
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content[] contents = toolWindow.getContentManager().getContents();
        for (Content content : contents) {
            toolWindow.getContentManager().removeContent(content, true);
        }
        // 重新创建内容
        createToolWindowContent(project, toolWindow);
    }

    private static void showWarnTips(@NotNull Project project, String title, String msg) {
        showTips(project, title, msg, NotificationType.WARNING);
    }

    private static void showTips(@NotNull Project project, String title, String msg) {
        showTips(project, title, msg, NotificationType.INFORMATION);
    }

    private static void showTips(@NotNull Project project, String title, String msg, NotificationType notificationType) {
        // 显示通知提示用户
        Notification notification = new Notification(
                "ABC NOTATION",
                title,
                msg,
                notificationType
        );
        Notifications.Bus.notify(notification, project);
    }

}
