package com.microcold.hosts.view.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microcold.hosts.conf.Config;
import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorCategory;
import com.microcold.hosts.operate.HostsOperatorFactory;
import com.microcold.hosts.view.DialogUtils;
import com.microcold.hosts.view.properties.HostProperty;
import com.microcold.hosts.view.properties.HostsOperatorProperty;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

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
    public TableView<HostProperty> hostsTableView;

    @FXML
    public TableColumn<HostsOperatorProperty, Boolean> enableCol;

    @FXML
    public TableColumn<HostsOperatorProperty, String> ipCol;
    @FXML
    public TableColumn<HostsOperatorProperty, String> domainCol;
    @FXML
    public TableColumn<HostsOperatorProperty, String> commentCol;

    @FXML
    public TreeView<HostsOperatorProperty> hostsFileTreeView;

    @FXML
    public TreeItem<HostsOperatorProperty> sysHostsOperatorTreeItem;
    @FXML
    public TreeItem<HostsOperatorProperty> rootTreeItem;

    /**
     * 当前hosts操作类
     */
    @Setter
    private HostsOperator hostsOperator;

    private List<HostProperty> hostList;

    @Getter
    @Setter
    private ObjectProperty<Callback<Throwable, Integer>> callbackObjectProperty;

    public HostsOperator getHostsOperator() {
        if (hostsOperator == null) {
            hostsOperator = HostsOperatorFactory.getSystemHostsOperator();
        }
        return hostsOperator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enableCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
            if (CollectionUtils.isEmpty(hostList)) {
                return null;
            }
            HostProperty hostProperty = hostList.get(param);
            try {
                hostsOperator.enable(hostProperty.idProperty().get(), hostProperty.enableProperty().getValue());
                if (hostsOperator.isChanged()) {
                    hostsOperator.flush();
                }
            } catch (IOException e) {
                System.exit(callbackObjectProperty.getValue().call(e));
                LOGGER.error("保存hosts状态失败", e);
            }
            return hostProperty.enableProperty();
        }));
        StringConverter<String> sc = new StringConverter<String>() {
            @Override
            public String toString(String t) {
                return t == null ? null : t.toString();
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };
        ipCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        domainCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        commentCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));
        refreshData();
        initHostsOperatorTree();
    }

    private void initHostsOperatorTree() {
        hostsFileTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sysHostsOperatorTreeItem.setValue(new HostsOperatorProperty()
                .setHostsOperator(HostsOperatorFactory.getSystemHostsOperator()));

        hostsFileTreeView.setShowRoot(false);
        rootTreeItem.setExpanded(true);
        try {
            addTreeItem(rootTreeItem, HostsOperatorFactory.getUserHostsOperatorCategory());
        } catch (IOException e) {
            DialogUtils.createExceptionDialog("加载Hosts文件异常", e);
        }
        selectSysHostsItem(null);
        hostsFileTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // 应该在这里调用
            refreshCurrentHostsOperator(observable.getValue().getValue());
        });
    }

    private void addTreeItem(TreeItem<HostsOperatorProperty> parentItem, HostsOperatorCategory hostsOperatorCategory) {
        if (CollectionUtils.isNotEmpty(hostsOperatorCategory.getHostsOperatorList())) {
            for (HostsOperator hostsOperator : hostsOperatorCategory.getHostsOperatorList()) {
                parentItem.getChildren().add(
                        new TreeItem<>(new HostsOperatorProperty()
                                .setHostsOperator(hostsOperator)));
            }
        }
        if (CollectionUtils.isNotEmpty(hostsOperatorCategory.getSubCategoryList())) {
            for (HostsOperatorCategory operatorCategory : hostsOperatorCategory.getSubCategoryList()) {
                TreeItem<HostsOperatorProperty> subItem = new TreeItem<>(new HostsOperatorProperty()
                        .setHostsOperatorCategory(operatorCategory));
                subItem.setExpanded(true);
                parentItem.getChildren().add(subItem);
                addTreeItem(subItem, operatorCategory);
            }
        }
    }

    private void selectSysHostsItem(Callable<HostsOperator> callable) {
        hostsFileTreeView.getSelectionModel().select(sysHostsOperatorTreeItem);
        if (callable == null) {
            return;
        }
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error("选中系统节点回调出错", e);
        }
    }

    @FXML
    public void refreshHostsTable(MouseEvent mouseEvent) {
        // 单击，刷新table
        if (mouseEvent.getSource() == hostsFileTreeView
                && hostsFileTreeView.getSelectionModel().getSelectedItem() != null) {
            HostsOperatorProperty hostsOperatorProperty = hostsFileTreeView.getSelectionModel().getSelectedItem()
                    .getValue();
            if (mouseEvent.getClickCount() == 2 && hostsOperatorProperty.getHostsOperator() != null) {
                // 检查管理员密码
                if (!Config.checkAdminPassword()) {
                    getCallbackObjectProperty().getValue().call(null);
                } else {
                    try {
                        HostsOperatorFactory.getSystemHostsOperator().switchTo(
                                HostsOperatorFactory.getCommonHostsOperator(),
                                hostsOperatorProperty.getHostsOperator());
                        // TODO 自动备份
                        if (HostsOperatorFactory.getSystemHostsOperator().isChanged()) {
                            HostsOperatorFactory.getSystemHostsOperator().flush();
                            messageLabel.setText("当前使用【" + hostsOperatorProperty.getHostsOperator().getName() + "】");
                            selectSysHostsItem(() -> {
                                setHostsOperator(HostsOperatorFactory.getSystemHostsOperator());
                                refreshData();
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

    private void refreshCurrentHostsOperator(HostsOperatorProperty hostsOperatorProperty) {
        if (hostsOperatorProperty.getHostsOperator() != null) {
            setHostsOperator(hostsOperatorProperty.getHostsOperator());
            refreshData();
        }
    }

    public void saveIP(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveIp(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().ipProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            callbackObjectProperty.get().call(e);
            LOGGER.error("保存hosts IP 失败", e);
        }
    }

    public void saveDomain(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveDomain(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().domainProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            getCallbackObjectProperty().getValue().call(e);
            LOGGER.error("保存hosts 域名失败", e);
        }
    }

    public void saveComment(CellEditEvent<HostProperty, String> event) {
        try {
            hostsOperator.saveComment(event.getRowValue().idProperty().get(), event.getNewValue());
            event.getRowValue().commentProperty().set(event.getNewValue());
            if (hostsOperator.isChanged()) {
                hostsOperator.flush();
            }
        } catch (IOException e) {
            getCallbackObjectProperty().getValue().call(e);
            LOGGER.error("保存hosts备注失败", e);
        }
    }

    private void refreshData() {
        if (getHostsOperator() == null) {
            return;
        }
        getHostsOperator().init();
        hostList = Lists.newArrayList();
        for (HostBean hostBean : getHostsOperator().getHostBeanList()) {
            hostList.add(new HostProperty(hostBean));
        }
        final ObservableList<HostProperty> data = FXCollections.observableArrayList(
                hostProperty -> new Observable[] { hostProperty.enableProperty() });
        data.addAll(hostList);
        hostsTableView.setItems(data);
        hostsTableView.refresh();
    }

    public Map<HostsOperator, List<HostBean>> search(String key) {
        if (StringUtils.isBlank(key)) {
            return Collections.emptyMap();
        }
        Map<HostsOperator, List<HostBean>> result = Maps.newLinkedHashMap();
        result.put(getHostsOperator(), getHostsOperator().search(key));
        return result;
    }

    public void getToItem(Integer id) {
        if (id != null && id >= 0 && CollectionUtils.isNotEmpty(hostList)){
            int index = 0;
            for (HostProperty hostProperty : hostList){
                if (hostProperty.idProperty().get() == id){
                    hostsTableView.getSelectionModel().select(hostProperty);
                    hostsTableView.scrollTo(index > 6 ? index - 2: 0);
                    // hostsTableView.getFocusModel().focus(index);
                    hostsTableView.setFocusTraversable(true);
                    Platform.runLater(() -> {
                        hostsTableView.requestFocus();
                    });
                    break;
                }
                index ++;
            }
        }
    }
}
