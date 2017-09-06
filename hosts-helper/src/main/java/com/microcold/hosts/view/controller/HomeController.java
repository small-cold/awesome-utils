package com.microcold.hosts.view.controller;

import com.microcold.hosts.conf.Config;
import com.microcold.hosts.conf.ConfigBean;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorFactory;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class HomeController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class);
    private static final String os = System.getProperty("os.name");

    @FXML
    public MenuBar menuBar;
    @FXML
    public Label errorMessage;
    @FXML
    public HostTableView hostsTableView;

    /**
     * 自动备份菜单
     */
    @FXML
    public CheckMenuItem autoBackupMenuItem;

    @FXML
    public TreeView<HostsOperatorProperty> hostsFileTreeView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMenu(location, resources);
        initHostsOperatorTree(location, resources);
    }

    private void initHostsOperatorTree(URL location, ResourceBundle resources) {
        final TreeItem<HostsOperatorProperty> treeRoot = new TreeItem<>();
        hostsFileTreeView.setRoot(treeRoot);
        hostsFileTreeView.setShowRoot(false);
        treeRoot.setExpanded(true);
        try {
            HostsOperator sysHostsOperator = HostsOperatorFactory.getSystemHostsOperator();
            HostsOperator commonHostsOperator = HostsOperatorFactory.getCommonHostsOperator();
            treeRoot.getChildren().addAll(Arrays.asList(
                    new TreeItem<>(new HostsOperatorProperty().setShowName("当前配置")
                            .setHostsOperator(sysHostsOperator)),
                    new TreeItem<>(new HostsOperatorProperty().setShowName("通用配置")
                            .setHostsOperator(commonHostsOperator)),
                    new TreeItem<>(new HostsOperatorProperty().setShowName("用户配置")))
            );
            Map<String, HostsOperator> userHostsOperatorMap = HostsOperatorFactory.getUserHostsOperatorMap();
            if (MapUtils.isNotEmpty(userHostsOperatorMap)) {
                for (HostsOperator hostsOperator : userHostsOperatorMap.values()) {
                    treeRoot.getChildren().get(2).getChildren().add(
                            new TreeItem<>(new HostsOperatorProperty()
                                    .setShowName(hostsOperator.getName())
                                    .setHostsOperator(hostsOperator)));
                }
            }
        } catch (IOException e) {
            LOGGER.error("加载Hosts文件异常", e);
            DialogUtils.createExceptionDialog("加载Hosts文件异常", e);
            errorMessage.setText("加载Hosts文件异常");
        }

    }

    private void initMenu(URL location, ResourceBundle resources) {
        try {
            ConfigBean configBean = Config.getConfigBean();
            autoBackupMenuItem.setSelected(BooleanUtils.isTrue(configBean.getAutoBackup()));
        } catch (Exception e) {
            LOGGER.error("读取配置文件错误", e);
            DialogUtils.createExceptionDialog("读取配置文件错误", e);
            errorMessage.setText("读取配置文件错误");
        }

        if (os != null && os.startsWith("Mac")) {
            Menu systemMenuBarMenu = new Menu("其他");

            final CheckMenuItem useSystemMenuBarCB = new CheckMenuItem("使用系统菜单栏");
            useSystemMenuBarCB.setSelected(true);
            menuBar.useSystemMenuBarProperty().bind(useSystemMenuBarCB.selectedProperty());
            systemMenuBarMenu.getItems().add(useSystemMenuBarCB);

            menuBar.getMenus().add(systemMenuBarMenu);

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            menuBar.heightProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
                errorMessage.setVisible((menuBar.getHeight() == 0));
            });
        }
    }

    @FXML
    public void setAutoBackup(ActionEvent actionEvent) {
        ConfigBean configBean = Config.getConfigBean();
        configBean.setAutoBackup(autoBackupMenuItem.isSelected());
        Config.saveConfig(configBean);
    }

    @FXML
    public void refreshHostsTable(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && mouseEvent.getSource() instanceof TreeView) {
            TreeView<HostsOperatorProperty> treeView = (TreeView<HostsOperatorProperty>) mouseEvent.getSource();
            HostsOperatorProperty hostsOperatorProperty = treeView.getSelectionModel().getSelectedItem().getValue();
            hostsTableView.setHostsOperator(hostsOperatorProperty.getHostsOperator());
            try {
                hostsTableView.refreshData();
            } catch (FileNotFoundException e) {
                DialogUtils.createExceptionDialog("读取文件异常", e);
                hostsOperatorProperty.setEnable(false);
                treeView.refresh();
            }
        }
    }
}
