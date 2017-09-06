package com.microcold.hosts.view.controller;

import com.google.common.collect.Lists;
import com.microcold.hosts.operate.HostBean;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.operate.HostsOperatorFactory;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/*
 * Created by MicroCold on 2017/9/5.
 */
public class HostTableView extends TableView<HostProperty> {

    private static final Logger LOGGER = Logger.getLogger(HostTableView.class);

    /**
     * 当前hosts操作类
     */
    @Setter
    @Getter
    private HostsOperator hostsOperator;

    private List<HostProperty> hostList;

    public HostTableView() {
        this(HostsOperatorFactory.getSystemHostsOperator());
    }

    public HostTableView(HostsOperator hostsOperator) {
        super();
        this.hostsOperator = hostsOperator;
        initTableContent();
    }

    private void initTableContent() {

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
        TableColumn<HostProperty, Boolean> enableCol = new TableColumn<>();
        enableCol.setText("启用");
        enableCol.setMinWidth(50);
        enableCol.setCellValueFactory(new PropertyValueFactory<>("enable"));
        enableCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
            if (CollectionUtils.isEmpty(hostList)) {
                return null;
            }
            HostProperty hostProperty = hostList.get(param);
            try {
                hostsOperator.enable(hostProperty.idProperty().get(), hostProperty.enableProperty().getValue());
            } catch (IOException e) {
                LOGGER.error("保存hosts状态失败", e);
                DialogUtils.createDialogCheckPermission(e);
            }
            return hostProperty.enableProperty();
        }));
        TableColumn<HostProperty, String> ipCol = new TableColumn<>();
        ipCol.setOnEditCommit(event -> {
            try {
                hostsOperator.saveIp(event.getRowValue().idProperty().get(), event.getNewValue());
            } catch (IOException e) {
                LOGGER.error("保存hosts状态失败", e);
                DialogUtils.createDialogCheckPermission(e);
            }
        });
        ipCol.setMinWidth(150);
        ipCol.setText("IP");
        ipCol.setCellValueFactory(new PropertyValueFactory<>("ip"));
        ipCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));

        TableColumn<HostProperty, String> domainCol = new TableColumn<>();
        domainCol.setOnEditCommit(event -> {
            try {
                hostsOperator.saveDomain(event.getRowValue().idProperty().get(), event.getNewValue());
            } catch (IOException e) {
                LOGGER.error("保存hosts状态失败", e);
                DialogUtils.createDialogCheckPermission(e);
            }
        });
        domainCol.setText("DOMAIN");
        domainCol.setMinWidth(150);
        domainCol.setCellValueFactory(new PropertyValueFactory<>("domain"));
        domainCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));

        TableColumn<HostProperty, String> contentCol = new TableColumn<>();
        contentCol.setOnEditCommit(event ->
                hostsOperator.saveComment(event.getRowValue().idProperty().get(), event.getNewValue()));
        contentCol.setText("备注");
        contentCol.setMinWidth(220);
        contentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        contentCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));

        setEditable(true);
        super.getColumns().setAll(enableCol, ipCol, domainCol, contentCol);
    }

    public void refreshData() throws FileNotFoundException {
        if (hostsOperator == null) {
            return;
        }
        // HostsOperator sysHostsOperator = HostsOperatorFactory.getSystemHostsOperator();
        hostsOperator.init();
        hostList = Lists.newArrayList();
        for (HostBean hostBean : hostsOperator.getHostBeanList()) {
            hostList.add(new HostProperty(hostBean));
        }
        final ObservableList<HostProperty> data = FXCollections.observableArrayList(
                hostProperty -> new Observable[] { hostProperty.enableProperty() });
        data.addAll(hostList);
        setItems(data);
        refresh();
    }
}
