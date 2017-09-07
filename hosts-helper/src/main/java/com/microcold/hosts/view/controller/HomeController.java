package com.microcold.hosts.view.controller;

import com.microcold.hosts.conf.Config;
import com.microcold.hosts.conf.ConfigBean;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
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
    public Label messageLabel;
    @FXML
    public HostsTableView hostsTableView;

    /**
     * 自动备份菜单
     */
    @FXML
    public CheckMenuItem autoBackupMenuItem;

    @FXML
    public HostsFileTreeView hostsFileTreeView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initMenu(location, resources);
        hostsFileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // 应该在这里调用
            refreshCurrentHostsOperator(observable.getValue().getValue());
        });
        hostsFileTreeView.init();
        hostsTableView.refreshData();
    }

    private void initMenu(URL location, ResourceBundle resources) {
        try {
            ConfigBean configBean = Config.getConfigBean();
            autoBackupMenuItem.setSelected(BooleanUtils.isTrue(configBean.getAutoBackup()));
        } catch (Exception e) {
            DialogUtils.createExceptionDialog("读取配置文件错误", e);
            messageLabel.setText("读取配置文件错误");
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
                messageLabel.setVisible((menuBar.getHeight() == 0));
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
        // 单击，刷新table
        if (mouseEvent.getSource() == hostsFileTreeView) {
            if (hostsFileTreeView.getSelectionModel().getSelectedItem() == null) {
                return;
            }
            HostsOperatorProperty hostsOperatorProperty = hostsFileTreeView.getSelectionModel().getSelectedItem()
                    .getValue();
            if (mouseEvent.getClickCount() == 2 && hostsOperatorProperty.getHostsOperator() != null) {
                // 检查管理员密码
                if (!Config.checkAdminPassword()) {
                    DialogUtils.createAdminDialog();
                }
                try {
                    HostsOperatorFactory.getSystemHostsOperator().switchTo(
                            HostsOperatorFactory.getCommonHostsOperator(),
                            hostsOperatorProperty.getHostsOperator());
                    // TODO 自动备份
                    if (HostsOperatorFactory.getSystemHostsOperator().isChanged()) {
                        HostsOperatorFactory.getSystemHostsOperator().flush();
                        messageLabel.setText("当前使用【" + hostsOperatorProperty.getHostsOperator().getName() + "】");
                        hostsFileTreeView.selectSysHostsItem(() -> {
                            hostsTableView.setHostsOperator(HostsOperatorFactory.getSystemHostsOperator());
                            hostsTableView.refreshData();
                            return HostsOperatorFactory.getSystemHostsOperator();
                        });
                    }
                } catch (IOException e) {
                    DialogUtils.createDialogCheckPermission(e);
                }
            }
        }
    }

    public void refreshCurrentHostsOperator(HostsOperatorProperty hostsOperatorProperty) {
        if (hostsOperatorProperty.getHostsOperator() != null) {
            hostsTableView.setHostsOperator(hostsOperatorProperty.getHostsOperator());
            hostsTableView.refreshData();
            hostsTableView.setEditable(!hostsOperatorProperty.getHostsOperator().isOnlyRead());
        }
    }
}
