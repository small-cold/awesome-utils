package com.microcold.hosts.view;
/*
 * Created by MicroCold on 2017/9/4.
 */

import com.google.common.collect.Lists;
import com.microcold.hosts.view.controller.HomeController;
import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorFactory;
import com.microcold.hosts.view.controller.HostProperty;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class HostsHelperApp extends Application {

    private final Label sysMenuLabel = new Label("Using System Menu");

    private static final boolean SHOW_MENU = false;
    private Pane root;
    private Button backButton;
    private Button forwardButton;
    private Button homeButton;
    private ToggleButton listButton;
    private ToggleButton searchButton;
    private MenuBar menuBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        root = new Pane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                final double w = getWidth();
                final double h = getHeight();
                final double menuHeight = SHOW_MENU ? menuBar.prefHeight(w) : 0;
                if (menuBar != null) {
                    menuBar.resize(w, menuHeight);
                }
            }
        };
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();
    }
    public Parent createContent() {
        gotoHome();
        return root;
    }

    private void gotoHome() {
        try {
            HomeController login = (HomeController) replaceSceneContent("Home.fxml");
            // login.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(HostsHelperApp.class.getName()).error(null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception {
        fxml = "views/" + fxml;
        FXMLLoader loader = new FXMLLoader();
        InputStream in = HostsHelperApp.class.getClassLoader().getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(HostsHelperApp.class.getClassLoader().getResource(fxml));
        BorderPane page;
        try {
            page = loader.load(in);
        } finally {
            in.close();
        }
        // root.getChildren().removeAll();
        root.getChildren().addAll(page);
        return (Initializable) loader.getController();
    }

    public Parent createMenu() {
        BorderPane borderPane = new BorderPane();
        //Top comment
        ToolBar toolbar = new ToolBar();
        toolbar.getItems().add(new Button("Home"));
        toolbar.getItems().add(new Button("Options"));
        toolbar.getItems().add(new Button("Help"));
        borderPane.setTop(toolbar);

        //Left comment
        Label label1 = new Label("Left hand");
        Button leftButton = new Button("left");
        VBox leftVbox = new VBox();
        leftVbox.getChildren().addAll(label1, leftButton);
        borderPane.setLeft(leftVbox);

        //Right comment
        Label rightlabel1 = new Label("Right hand");
        Button rightButton = new Button("right");

        // VBox rightVbox = new VBox();
        // rightVbox.getChildren().addAll(rightlabel1, rightButton);
        // borderPane.setRight(rightVbox);

        //Center comment
        Label centerLabel = new Label("Center area.");
        centerLabel.setWrapText(true);
        // ImageView imageView = new ImageView(ICON_48);

        //Using AnchorPane only to position items in the center
        AnchorPane centerAP = new AnchorPane();
        AnchorPane.setTopAnchor(centerLabel, Double.valueOf(50));
        AnchorPane.setLeftAnchor(centerLabel, Double.valueOf(200));
        // AnchorPane.setTopAnchor(imageView, Double.valueOf(40));
        // AnchorPane.setLeftAnchor(imageView, Double.valueOf(30));
        // centerAP.getChildren().addAll(createHostTable());
        borderPane.setCenter(centerAP);

        //Bottom comment
        // Label bottomLabel = new Label("At the bottom.");
        // borderPane.setBottom(bottomLabel);
        borderPane.autosize();
        return borderPane;
    }
}
