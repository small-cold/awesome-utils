package com.microcold.hosts.view;

import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorCategory;
import com.microcold.hosts.operate.HostsOperatorFactory;
import com.microcold.hosts.view.properties.HostsOperatorProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Callable;

/*
 * Created by MicroCold on 2017/9/6.
 */
public class HostsFileTreeView extends TreeView<HostsOperatorProperty> {

    private static final Logger LOGGER = Logger.getLogger(HostsFileTreeView.class);

    private static TreeItem<HostsOperatorProperty> firstItem;

    private ObservableValue<Callback<HostsOperatorProperty, HostsOperator>> observableValue;

    public void init() {
        if (firstItem == null) {
            firstItem = new TreeItem<>(new HostsOperatorProperty()
                    .setHostsOperator(HostsOperatorFactory.getSystemHostsOperator()));
        }
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        final TreeItem<HostsOperatorProperty> treeRoot = new TreeItem<>();
        this.setRoot(treeRoot);
        this.setShowRoot(false);
        treeRoot.setExpanded(true);
        try {
            treeRoot.getChildren().add(firstItem);
            addItem(treeRoot, HostsOperatorFactory.getUserHostsOperatorCategory());
        } catch (IOException e) {
            DialogUtils.createExceptionDialog("加载Hosts文件异常", e);
        }
        selectSysHostsItem(null);
    }

    public void selectSysHostsItem(Callable<HostsOperator> callable) {
        getSelectionModel().select(firstItem);
        if (callable == null) {
            return;
        }
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error("选中系统节点回调出错", e);
        }
    }

    private void addItem(TreeItem<HostsOperatorProperty> parentItem, HostsOperatorCategory hostsOperatorCategory) {
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
                addItem(subItem, operatorCategory);
            }
        }
    }
}
