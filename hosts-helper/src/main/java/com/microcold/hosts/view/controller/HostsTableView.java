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
public class HostsTableView extends TableView<HostProperty> {

    private static final Logger LOGGER = Logger.getLogger(HostsTableView.class);

    /**
     * 当前hosts操作类
     */
    @Setter
    @Getter
    private HostsOperator hostsOperator;

    private List<HostProperty> hostList;

    public HostsTableView() {
        this(HostsOperatorFactory.getSystemHostsOperator());
    }

    public HostsTableView(HostsOperator hostsOperator) {
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
                if (hostsOperator.isChanged()){
                    hostsOperator.flush();
                }
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
                event.getRowValue().ipProperty().set(event.getNewValue());
                if (hostsOperator.isChanged()){
                    hostsOperator.flush();
                }
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
                event.getRowValue().domainProperty().set(event.getNewValue());
                if (hostsOperator.isChanged()){
                    hostsOperator.flush();
                }
            } catch (IOException e) {
                LOGGER.error("保存hosts状态失败", e);
                DialogUtils.createDialogCheckPermission(e);
            }
        });
        domainCol.setText("DOMAIN");
        domainCol.setMinWidth(150);
        domainCol.setCellValueFactory(new PropertyValueFactory<>("domain"));
        domainCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));

        TableColumn<HostProperty, String> commentCol = new TableColumn<>();
        commentCol.setOnEditCommit(event ->{
                    try {
                        hostsOperator.saveComment(event.getRowValue().idProperty().get(), event.getNewValue());
                        event.getRowValue().commentProperty().set(event.getNewValue());
                        if (hostsOperator.isChanged()){
                            hostsOperator.flush();
                        }
                    } catch (IOException e) {
                        LOGGER.error("保存hosts状态失败", e);
                        DialogUtils.createDialogCheckPermission(e);
                    }
                });
        commentCol.setText("备注");
        commentCol.setMinWidth(220);
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setCellFactory(TextFieldTableCell.forTableColumn(sc));

        setEditable(true);
        super.getColumns().setAll(enableCol, ipCol, domainCol, commentCol);
    }

    public void refreshData(){
        if (hostsOperator == null) {
            return;
        }
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
