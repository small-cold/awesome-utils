package com.microcold.hosts.view.controller;

import com.microcold.hosts.conf.Config;
import com.microcold.hosts.exception.PermissionIOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/*
 * Created by MicroCold on 2017/9/5.
 */
public class DialogUtils {

    private static Logger logger = Logger.getLogger(DialogUtils.class);
    private static Stage stage;

    public static Dialog<ButtonType> createExceptionDialog(String title, Throwable th) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);

        final DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContentText("详情:");
        dialogPane.getButtonTypes().addAll(ButtonType.OK);
        dialogPane.setContentText(th.getMessage());
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label("Exception stacktrace:");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        pw.close();

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane root = new GridPane();
        root.setVisible(false);
        root.setMaxWidth(Double.MAX_VALUE);
        root.add(label, 0, 0);
        root.add(textArea, 0, 1);
        dialogPane.setExpandableContent(root);
        dialog.show();
        dialog.setOnHidden(event -> {
            logger.info(title, th);
        });
        return dialog;
    }

    public static Dialog createAdminDialog() {
        TextInputDialog textInput = new TextInputDialog("");
        textInput.setTitle("请输入管理员密码（" + System.getProperty("user.name") + ")");
        textInput.getDialogPane().setContentText("密码:");
        textInput.show();
        textInput.setOnHidden(event -> {
            if (textInput.getResult().isEmpty()) {
                createAlert("管理员密码不能为空", Alert.AlertType.WARNING);
            } else {
                Config.setAdminPassword(textInput.getResult());
            }
        });
        return textInput;
    }

    public static Dialog createDialogCheckPermission(Throwable th) {
        if (th instanceof PermissionIOException) {
            return createAdminDialog();
        } else {
            return createExceptionDialog("未知错误", th);
        }
    }

    public static void setStage(Stage stage) {
        DialogUtils.stage = stage;
    }

    public static Alert createAlert(String contentText, Alert.AlertType type) {
        Alert alert = new Alert(type, contentText);
        alert.initModality(Modality.APPLICATION_MODAL);
        if (stage != null) {
            alert.initOwner(stage);
        }
        alert.getDialogPane().setContentText(type + " text.");
        alert.getDialogPane().setHeaderText(null);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> System.out.println("The alert was approved"));
        return alert;
    }
}
