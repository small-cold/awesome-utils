/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.microcold.hosts.view;

import com.google.common.collect.Maps;
import com.microcold.hosts.operate.HostsOperator;
import com.microcold.hosts.view.controller.HomePageController;
import com.microcold.hosts.view.controller.HostsSearchResult;
import com.microcold.hosts.view.controller.Popover;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Implementation of popover to show search results
 */
public class SearchPopover extends Popover {
    private final SearchBox searchBox;
    private HomePageController indexSearcher;
    private Tooltip searchErrorTooltip = null;
    private Timeline searchErrorTooltipHidder = null;
    private SearchResultPopoverList searchResultPopoverList;

    public SearchPopover(final SearchBox searchBox, HomePageController pageBrowser) {
        super();
        this.searchBox = searchBox;
        this.indexSearcher = pageBrowser;
        this.searchResultPopoverList = new SearchResultPopoverList(pageBrowser);
        getStyleClass().add("right-tooth");
        setPrefWidth(400);

        searchBox.textProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            updateResults();
        });

        searchBox.addEventFilter(KeyEvent.ANY, (KeyEvent t) -> {
            if (t.getCode() == KeyCode.DOWN
                    || t.getCode() == KeyCode.UP
                    || t.getCode() == KeyCode.PAGE_DOWN
                    || (t.getCode() == KeyCode.HOME && (t.isControlDown() || t.isMetaDown()))
                    || (t.getCode() == KeyCode.END && (t.isControlDown() || t.isMetaDown()))
                    || t.getCode() == KeyCode.PAGE_UP) {
                searchResultPopoverList.fireEvent(t);
                t.consume();
            } else if (t.getCode() == KeyCode.ENTER) {
                t.consume();
                if (t.getEventType() == KeyEvent.KEY_PRESSED) {
                    HostsSearchResult selectedItem = searchResultPopoverList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null)
                        searchResultPopoverList.itemClicked(selectedItem);
                }
            }
        });

        // if list gets focus then send back to search box
        searchResultPopoverList.focusedProperty()
                .addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) -> {
                    if (hasFocus) {
                        searchBox.requestFocus();
                        searchBox.selectPositionCaret(searchBox.getText().length());
                    }
                });
    }

    private void updateResults() {
        if (StringUtils.isBlank(searchBox.getText())) {
            populateMenu(Maps.newHashMap());
            return;
        }
        boolean haveResults = false;
        Map<HostsOperator, List<HostsSearchResult>> results = indexSearcher.search(searchBox.getText());
        // check if we have any results
        for (List<HostsSearchResult> categoryResults : results.values()) {
            if (categoryResults.size() > 0) {
                haveResults = true;
                break;
            }
        }
        if (haveResults) {
            showError(null);
            populateMenu(results);
            show();
        } else {
            if (searchErrorTooltip == null || searchErrorTooltip.getText() == null)
                showError("No matches");
            hide();
        }
    }

    private void populateMenu(Map<HostsOperator, List<HostsSearchResult>> results) {
        searchResultPopoverList.getItems().clear();
        for (Map.Entry<HostsOperator, List<HostsSearchResult>> entry : results.entrySet()) {
            searchResultPopoverList.getItems().addAll(entry.getValue());
        }
        clearPages();
        pushPage(searchResultPopoverList);
    }

    private void showError(String message) {
        if (searchErrorTooltip == null) {
            searchErrorTooltip = new Tooltip();
        }
        searchErrorTooltip.setText(message);
        if (searchErrorTooltipHidder != null)
            searchErrorTooltipHidder.stop();
        if (message != null) {
            Point2D toolTipPos = searchBox.localToScene(0, searchBox.getLayoutBounds().getHeight());
            double x = toolTipPos.getX() + searchBox.getScene().getX() + searchBox.getScene().getWindow().getX();
            double y = toolTipPos.getY() + searchBox.getScene().getY() + searchBox.getScene().getWindow().getY();
            searchErrorTooltip.show(searchBox.getScene().getWindow(), x, y);
            searchErrorTooltipHidder = new Timeline();
            searchErrorTooltipHidder.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(3), (ActionEvent t) -> {
                        searchErrorTooltip.hide();
                        searchErrorTooltip.setText(null);
                    })
            );
            searchErrorTooltipHidder.play();
        } else {
            searchErrorTooltip.hide();
        }
    }
}
