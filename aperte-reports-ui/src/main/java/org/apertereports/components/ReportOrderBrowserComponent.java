package org.apertereports.components;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ReportOrderBrowserComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String COMPONENT_STYLE = "borderless light";
    private static final String BACKGROUND_BUTTON_PARAMS = "background.button.params";
    private static final String BACKGROUND_BUTTON_PREVIEW = "background.button.preview";
    private static final String CREATE_DATE = "createDate";
    private static final String REPORTNAME_STYLE = "h4";
    private static final String REPORTNAME = "reportname";
    private static final String REPORT_STATUS = "reportStatus";
    private static final String BUTTON_REFRESH = "button.refresh";
    private PaginatedPanelList<ReportOrder, ReportOrderPanel> list;

    public ReportOrderBrowserComponent() {
    }

    @Override
    public void attach() {
        super.attach();
        init();
    }

    private void init() {
        HorizontalLayout hl = new HorizontalLayout();

        TextField searchField = ComponentFactory.createSearchBox(new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        }, hl);
        searchField.setWidth("150px");

        Label gap = new Label();
        gap.setWidth("10px");
        hl.addComponent(gap);

        Label expandedGap = new Label();
        expandedGap.setWidth("100%");
        hl.addComponent(expandedGap);
        hl.setExpandRatio(expandedGap, 1.0f);

        ComponentFactory.createButton(BUTTON_REFRESH, null, hl, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                list.refresh();
            }
        });

        hl.setWidth("100%");
        addComponent(hl);

        list = new PaginatedPanelList<ReportOrder, ReportOrderBrowserComponent.ReportOrderPanel>(PAGE_SIZE) {

            @Override
            protected ReportOrderPanel transform(ReportOrder object) {
                return new ReportOrderPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportOrderDAO.countMatching(filter);
            }

            @Override
            protected Collection<ReportOrder> fetch(String filter, int firstResult, int maxResults) {
                return ReportOrderDAO.fetch(filter, firstResult, maxResults);
            }
        };

        list.setMargin(true, false, false, false);
        addComponent(list);
        list.filter(null);
        setStyleName(COMPONENT_STYLE);
    }

    private class ReportOrderPanel extends Panel {

        private ReportOrder order;
        private ReportOrderParamsPanel params;
        private boolean paramsVisible;

        public ReportOrderPanel(ReportOrder order) {
            this.order = order;
        }

        @Override
        public void attach() {
            super.attach();
            init();
        }

        private void init() {

            setStyleName(COMPONENT_STYLE);
            ((AbstractLayout) getContent()).setMargin(false, false, false, false);
            BeanItem<ReportOrder> item = new BeanItem<ReportOrder>(order);
            GridLayout grid = new GridLayout(6, 1);
            grid.setWidth("100%");
            grid.setSpacing(true);
            grid.setColumnExpandRatio(1, 1);
            addComponent(grid);
            ComponentFactory.createIcon(item, REPORT_STATUS, grid);
            ComponentFactory.createLabel(new BeanItem<ReportTemplate>(order.getReport()), REPORTNAME, REPORTNAME_STYLE, grid);

            ComponentFactory.createCalendarLabel(item, CREATE_DATE, "", grid);
            Button previewButton = ComponentFactory.createButton(BACKGROUND_BUTTON_PREVIEW, BaseTheme.BUTTON_LINK, grid, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    showReport();
                }
            });
            ComponentFactory.createButton(BACKGROUND_BUTTON_PARAMS, BaseTheme.BUTTON_LINK, grid, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();
                }
            });

            if (order.getReportStatus() != Status.SUCCEEDED) {
                previewButton.setEnabled(false);
            }

            params = new ReportOrderParamsPanel(order.getParametersXml());
        }

        private void showReport() {
            byte[] reportData;
            try {
                reportData = ReportGeneratorUtils.decodeContent(this.order.getReportResult());
            } catch (UnsupportedEncodingException e) {
                throw new AperteReportsRuntimeException(e);
            }
            FileStreamer.showFile(getApplication(), this.order.getReport().getReportname(), reportData,
                    this.order.getOutputFormat());
        }

        private void toggleParams() {
            paramsVisible = !paramsVisible;
            if (paramsVisible) {
                addComponent(params);
            } else {
                removeComponent(params);
            }
        }
    }

    private class ReportOrderParamsPanel extends Panel {

        public ReportOrderParamsPanel(String paramsXml) {
            Map<String, String> params = XmlReportConfigLoader.getInstance().xmlAsMap(paramsXml);
            List<String> sortedParamNames = new ArrayList<String>(params.keySet());
            Collections.sort(sortedParamNames);
            PropertysetItem item = new PropertysetItem();
            for (String string : sortedParamNames) {
                item.addItemProperty(string, new ObjectProperty<String>(params.get(string)));
            }
            Form form = new Form();
            form.setItemDataSource(item);
            form.setReadOnly(true);
            addComponent(form);
            form.getLayout().setMargin(false);
            ((AbstractLayout) getContent()).setMargin(false, true, false, true);
        }
    }
}
