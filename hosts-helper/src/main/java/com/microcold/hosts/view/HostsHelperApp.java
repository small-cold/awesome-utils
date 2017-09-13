package com.microcold.hosts.view;
/*
 * Created by MicroCold on 2017/9/4.
 */

import com.microcold.hosts.conf.Config;
import com.microcold.hosts.conf.ConfigBean;
import com.microcold.hosts.exception.PermissionIOException;
import com.microcold.hosts.view.controller.HomePageController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

public class HostsHelperApp extends Application {

    private static final int TOOL_BAR_BUTTON_SIZE = 30;
    private static final String HOME_PAGE_FXML = "views/Home.fxml";

    private Scene scene;
    private Pane root;
    private TitledToolBar toolBar;

    private MenuBar menuBar;
    private final SearchBox searchBox = new SearchBox();
    private SearchPopover searchPopover;

    private AnchorPane homePane;
    private HomePageController homePageController;
    private PasswordDialog passwordDialog;

    private ObjectProperty<Callback<Throwable, Integer>> callBack;

    public Callback<Throwable, Integer> getCallBack() {
        return callBack.get();
    }

    public ObjectProperty<Callback<Throwable, Integer>> callBackProperty() {
        if (callBack == null) {
            callBack = new SimpleObjectProperty<>();
            callBack.setValue(th -> {
                if (th == null || th instanceof PermissionIOException) {
                    passwordDialog.show();
                    return 1;
                } else {
                    DialogUtils.createExceptionDialog("未知错误", th);
                    return 0;
                }
            });
        }
        return callBack;
    }

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
                final double menuHeight = menuBar.prefHeight(w);
                final double toolBarHeight = toolBar.prefHeight(w);
                if (menuBar != null) {
                    menuBar.resize(w, menuHeight);
                }
                toolBar.resizeRelocate(0, menuHeight, w, toolBarHeight);
                homePane.setLayoutY(toolBarHeight + menuHeight + 5);
                // homePane.resize(w, h - toolBarHeight);
                homePane.resize(w, h - toolBarHeight - menuHeight);
                homePane.resizeRelocate(0, toolBarHeight + menuHeight + 5, w, h - toolBarHeight - menuHeight);

                Point2D searchBoxBottomCenter = searchBox.localToScene(searchBox.getWidth() / 2, searchBox.getHeight());
                searchPopover.setLayoutX(
                        (int) searchBoxBottomCenter.getX() - searchPopover.getLayoutBounds().getWidth() + 50);
                searchPopover.setLayoutY((int) searchBoxBottomCenter.getY() + 20);
            }
        };
        root.setMinHeight(720);
        root.setMinHeight(480);
        initSysMenu();
        initToolBar();
        initHomePage();

        searchPopover = new SearchPopover(searchBox, homePageController);
        root.getChildren().add(searchPopover);
    }

    private void initAdminPasswordDialog() {
        passwordDialog = new PasswordDialog();
        passwordDialog.setTitle("请输入管理员密码（" + System.getProperty("user.name") + ")");
        passwordDialog.getDialogPane().setContentText("密码:");
        // passwordDialog.show();
        passwordDialog.setOnHidden(event -> {
            // if (!event.isConsumed()){
            //     createAlert("管理员密码",
            //             "系统Hosts为只读状态，双击不能快速切换系统hosts", Alert.AlertType.WARNING);
            // }else
            if (StringUtils.isBlank(passwordDialog.getResult())) {
                DialogUtils.createAlert("管理员密码为空",
                        "系统Hosts为只读状态，双击不能快速切换系统hosts", Alert.AlertType.WARNING);
            } else {
                boolean result = Config.setAdminPassword(passwordDialog.getResult());
                if (!result) {
                    passwordDialog.setTitle("密码错误（" + System.getProperty("user.name") + ")");
                    passwordDialog.show();
                }
            }
        });
    }

    private void initToolBar() {
        // CREATE TOOLBAR
        toolBar = new TitledToolBar();
        root.getChildren().add(toolBar);
        Button backButton = new Button();
        backButton.setId("back");
        backButton.getStyleClass().add("left-pill");
        backButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        Button forwardButton = new Button();
        forwardButton.setId("forward");
        forwardButton.getStyleClass().add("center-pill");
        forwardButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        Button homeButton = new Button();
        homeButton.setId("home");
        homeButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        homeButton.getStyleClass().add("right-pill");
        HBox navButtons = new HBox(0, backButton, forwardButton, homeButton);
        ToggleButton listButton = new ToggleButton();
        listButton.setId("list");
        listButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        HBox.setMargin(listButton, new Insets(0, 0, 0, 7));
        ToggleButton searchButton = new ToggleButton();
        searchButton.setId("search");
        searchButton.setPrefSize(TOOL_BAR_BUTTON_SIZE, TOOL_BAR_BUTTON_SIZE);
        searchBox.setPrefWidth(200);
        backButton.setGraphic(new Region());
        forwardButton.setGraphic(new Region());
        homeButton.setGraphic(new Region());
        listButton.setGraphic(new Region());
        searchButton.setGraphic(new Region());
        toolBar.addLeftItems(navButtons, listButton);
        toolBar.addRightItems(searchBox);

    }

    private void initSysMenu() {
        menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        Menu fileMenu = new Menu("文件");
        fileMenu.getItems().addAll(
                new MenuItem("新增"),
                new MenuItem("保存"),
                new SeparatorMenuItem(),
                new MenuItem("另存为"),
                new SeparatorMenuItem(),
                new MenuItem("导出配置"),
                new SeparatorMenuItem(),
                new MenuItem("退出")
        );
        menuBar.getMenus().add(fileMenu);
        Menu optionsMenu = new Menu("配置");
        CheckMenuItem autoBackupMenuItem = new CheckMenuItem("自动备份");
        autoBackupMenuItem.setSelected(BooleanUtils.isTrue(Config.getConfigBean().getAutoBackup()));
        autoBackupMenuItem.setOnAction(event -> {
            ConfigBean configBean = Config.getConfigBean();
            configBean.setAutoBackup(autoBackupMenuItem.isSelected());
            Config.saveConfig(configBean);
        });
        optionsMenu.getItems().addAll(
                autoBackupMenuItem,
                new MenuItem("系统配置")
        );
        menuBar.getMenus().add(optionsMenu);
        root.getChildren().add(menuBar);
    }

    private void initHomePage() {
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(HostsHelperApp.class.getClassLoader().getResource(HOME_PAGE_FXML));
        try (InputStream in = HostsHelperApp.class.getClassLoader().getResourceAsStream(HOME_PAGE_FXML)) {
            homePane = loader.load(in);
            homePageController = loader.getController();
            homePageController.setCallbackObjectProperty(callBackProperty());
        } catch (Exception e) {
            DialogUtils.createExceptionDialog("加载主页异常", e);
        }
        root.getChildren().add(0, homePane);
    }

    @Override
    public void start(Stage stage) {
        scene = new Scene(root, 720, 480, Color.BLACK);
        setStylesheets();
        stage.setScene(scene);
        // stage.setResizable(true);
        stage.setTitle("Hosts 助手");
        stage.show();
        initAdminPasswordDialog();
    }

    private void setStylesheets() {
        final String EXTERNAL_STYLESHEET = "http://fonts.googleapis.com/css?family=Source+Sans+Pro:200,300,400,600";
        scene.getStylesheets().setAll("/css/HostsHelper.css");
        Thread backgroundThread = new Thread(() -> {
            try {
                URL url = new URL(EXTERNAL_STYLESHEET);
                try (
                        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                        Reader newReader = Channels.newReader(rbc, "UTF-8");
                        BufferedReader bufferedReader = new BufferedReader(newReader)
                ) {
                    // Checking whether we can read a line from this url
                    // without exception
                    bufferedReader.readLine();
                }
                Platform.runLater(() -> {
                    // when succeeded switchTo this stylesheet to the scene
                    scene.getStylesheets().add(EXTERNAL_STYLESHEET);
                });
            } catch (MalformedURLException ex) {
                java.util.logging.Logger
                        .getLogger(HostsHelperApp.class.getName())
                        .log(Level.FINE, "Failed to load external stylesheet", ex);
            } catch (IOException ex) {
                java.util.logging.Logger
                        .getLogger(HostsHelperApp.class.getName())
                        .log(Level.FINE, "Failed to load external stylesheet", ex);
            }
        }, "Trying to reach external styleshet");
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

}
