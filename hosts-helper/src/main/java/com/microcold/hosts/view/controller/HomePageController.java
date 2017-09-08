package com.microcold.hosts.view.controller;

import com.microcold.hosts.conf.Config;
import com.microcold.hosts.operate.HostsOperatorFactory;
import com.microcold.hosts.view.DialogUtils;
import com.microcold.hosts.view.HostsFileTreeView;
import com.microcold.hosts.view.HostsTableView;
import com.microcold.hosts.view.properties.HostsOperatorProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/*
 * Created by MicroCold on 2017/9/4.
 */
public class HomePageController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(HomePageController.class);

    @FXML
    public Label messageLabel;
    @FXML
    public Label errorMessageLabel;
    @FXML
    public HostsTableView hostsTableView;

    @FXML
    public HostsFileTreeView hostsFileTreeView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // autosize();
        // pane.autosize();
        hostsFileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // 应该在这里调用
            refreshCurrentHostsOperator(observable.getValue().getValue());
        });
        hostsFileTreeView.init();
        hostsTableView.refreshData();
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
                if (!Config.checkAdminPassword()){
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
                        DialogUtils.createExceptionDialog("系统hosts为只读", e);
                    }
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
