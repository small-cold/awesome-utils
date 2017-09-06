package com.microcold.hosts.view.controller;

import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorCategory;
import com.microcold.hosts.operate.HostsOperatorFactory;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/*
 * Created by MicroCold on 2017/9/6.
 */
public class HostsFileTreeView extends TreeView<HostsOperatorProperty> {

    private static final Logger LOGGER = Logger.getLogger(HostsFileTreeView.class);

    public void init() {
        final TreeItem<HostsOperatorProperty> treeRoot = new TreeItem<>();
        this.setRoot(treeRoot);
        this.setShowRoot(false);
        treeRoot.setExpanded(true);
        try {
            treeRoot.getChildren().add(new TreeItem<>(new HostsOperatorProperty()
                    .setHostsOperator(HostsOperatorFactory.getSystemHostsOperator())));
            addItem(treeRoot, HostsOperatorFactory.getUserHostsOperatorCategory());
        } catch (IOException e) {
            DialogUtils.createExceptionDialog("加载Hosts文件异常", e);
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
                parentItem.getChildren().add(subItem);
                addItem(subItem, operatorCategory);
            }
        }
    }
}
