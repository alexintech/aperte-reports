package org.apertereports.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.backbone.scheduler.CyclicReportOrderScheduler;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.CyclicReportOrderDAO;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.CronExpressionValidator;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.VaadinUtil;
import org.quartz.SchedulerException;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;
import java.util.LinkedList;
import java.util.List;
import org.apertereports.common.users.User;
import org.apertereports.common.xml.config.ReportConfigParameter;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;

@SuppressWarnings("serial")
public class CyclicReportsComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String COMPONENT_STYLE_NAME = "borderless light";
    private PaginatedPanelList<CyclicReportOrder, CyclicReportPanel> list;
    private static final String DESCRIPTION_STYLE = "small";
    private static final String FORMAT_STYLE = "h4";
    private static final String ORDER_DESCRIPTION = "description";
    private static final String ORDER_CRON_SPEC = "cronSpec";
    private static final String ORDER_RECIPIENT_EMAIL = "recipientEmail";
    private static final String ORDER_OUTPUT_FORMAT = "outputFormat";
    private static final String ORDER_REPORT_REPORTNAME = "reportname";
    private static final String ORDER_REPORT = "report";
    private static final String VALIDATION_EMAIL = "validation.email";
    private static final String VALIDATION_CRON_EXPRESSION = "validation.cronExpression";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR = "cycylic.edit.required-error.";
    private static final String CYCLIC_EDIT_INPUT_PROMPT = "cyclic.edit.input-prompt.";
    private static final String CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME = "cyclic.edit.input-prompt.reportname";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME = "cycylic.edit.required-error.reportname";
    private static final String CYCLIC_EDIT_INPUT_PROMPT_FORMAT = "cyclic.edit.input-prompt.format";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT = "cycylic.edit.required-error.format";
    private boolean addingNew = false;
    private User user = null;
    private Button addButton;

    public CyclicReportsComponent() {
        init();
    }

    public void setUser(User user) {
        this.user = user;
        addButton.setVisible(user != null);
    }

    private void init() {

        HorizontalLayout header = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);
        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, header, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        });
        filterField.setWidth("150px");

        UiFactory.createSpacer(header, "10px", null);

        Label expandedGap = new Label();
        expandedGap.setWidth("100%");
        header.addComponent(expandedGap);
        header.setExpandRatio(expandedGap, 1.0f);

        UiFactory.createSpacer(header);
        addButton = UiFactory.createButton(UiIds.LABEL_ADD, header, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                addNew();
            }
        });
        addButton.setVisible(false);

        list = new PaginatedPanelList<CyclicReportOrder, CyclicReportsComponent.CyclicReportPanel>(PAGE_SIZE) {

            @Override
            protected CyclicReportPanel transform(CyclicReportOrder object) {
                return new CyclicReportPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return CyclicReportOrderDAO.countMatching(filter);
            }

            @Override
            protected Collection<CyclicReportOrder> fetch(String filter, int firstResult, int maxResults) {
                return CyclicReportOrderDAO.fetch(filter, firstResult, maxResults);
            }
        };
        list.setMargin(false, false, true, false);
        addComponent(list);
        setStyleName(COMPONENT_STYLE_NAME);
        list.filter(null);

        if (!AperteReportsJmsFacade.isJmsAvailable()) {
            HorizontalLayout validator = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);
            Form form = new Form();
            form.setComponentError(new UserError("JMS unavailable, cyclic reports execution is disabled!"));
            validator.addComponent(form);
        }
    }

    private void addNew() {
        if (addingNew) {
            return;
        }
        addingNew = true;
        CyclicReportOrder order = new CyclicReportOrder();
        EditCyclicReportPanel ecrp = new EditCyclicReportPanel(order, true);
        list.addComponent(ecrp, 0);
    }

    private class CyclicReportPanel extends Panel {

        private static final String REPORT_PANEL_STYLE = COMPONENT_STYLE_NAME;
        private CyclicReportOrder order;
        private ReportParamPanel paramsPanel;
        private Button toggleParams;
        private HorizontalLayout container;
        private Button enabledButton;

        public CyclicReportPanel(CyclicReportOrder order) {
            this.order = order;
            setStyleName(REPORT_PANEL_STYLE);
            BeanItem<CyclicReportOrder> item = new BeanItem<CyclicReportOrder>(order);
            HorizontalLayout row1 = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);
            HorizontalLayout row2 = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);

            container = UiFactory.createHLayout(row1, FAction.ALIGN_LEFT);
            container.setEnabled(CyclicReportPanel.this.order.getEnabled() == Boolean.TRUE);
            UiFactory.createLabel(new BeanItem<ReportTemplate>(order.getReport()),
                    ORDER_REPORT_REPORTNAME, container, FORMAT_STYLE, FAction.ALIGN_LEFT);

            UiFactory.createLabel(item, ORDER_OUTPUT_FORMAT, container, FAction.ALIGN_LEFT);
            Label spacerLabel = UiFactory.createSpacer(row1);
            enabledButton = UiFactory.createButton(getStateLabelCaption(), row1, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleEnable();
                }

                private void toggleEnable() {
                    boolean enabled = CyclicReportPanel.this.order.getEnabled() != Boolean.TRUE;
                    CyclicReportPanel.this.order.setEnabled(enabled);
                    container.setEnabled(enabled);
                    enabledButton.setCaption(VaadinUtil.getValue(getStateLabelCaption()));
                    CyclicReportOrderDAO.saveOrUpdate(CyclicReportPanel.this.order);
                    try {
                        if (enabled) {
                            CyclicReportOrderScheduler.rescheduleCyclicReportOrder(CyclicReportPanel.this.order);
                        } else {
                            CyclicReportOrderScheduler.unscheduleCyclicReportOrder(CyclicReportPanel.this.order);
                        }
                    } catch (SchedulerException e) {
                        throw new AperteReportsRuntimeException(e);
                    }
                }
            }, FAction.ALIGN_RIGTH);
            row1.setExpandRatio(spacerLabel, 1.0f);

            UiFactory.createLabel(item, ORDER_RECIPIENT_EMAIL, row2);
            UiFactory.createSpacer(row2);
            UiFactory.createLabel(item, ORDER_CRON_SPEC, row2, FAction.ALIGN_RIGTH);

            UiFactory.createLabel(item, ORDER_DESCRIPTION, this, DESCRIPTION_STYLE, FAction.SET_FULL_WIDTH);

            HorizontalLayout row3 = UiFactory.createHLayout(this);

            toggleParams = UiFactory.createButton(UiIds.LABEL_PARAMETERS, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    showParams();
                }
            });
            UiFactory.createButton(UiIds.LABEL_EDIT, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    edit();
                }
            });
            UiFactory.createButton(UiIds.LABEL_DELETE, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    remove();
                }
            });

        }

        private String getStateLabelCaption() {
            return order.getEnabled() == Boolean.TRUE ? UiIds.LABEL_DISABLE : UiIds.LABEL_ENABLE;
        }

        protected void remove() {
            CyclicReportOrderDAO.remove(order);
            list.removeComponent(this);
        }

        protected void edit() {
            list.replaceComponent(this, new EditCyclicReportPanel(this.order, false));
        }

        protected void showParams() {
            if (paramsPanel == null) {
                addComponent(paramsPanel = createParamsPanel());

                toggleParams.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PARAMETERS));
            } else {
                removeComponent(paramsPanel);
                paramsPanel = null;
                toggleParams.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            }
        }

        private ReportParamPanel createParamsPanel() {
            List<ReportConfigParameter> params = new LinkedList<ReportConfigParameter>();
            String paramsXml = order.getParametersXml();
            if (paramsXml != null) {
                params = XmlReportConfigLoader.getInstance().xmlAsParameters(paramsXml);
            }

            final ReportParamPanel panel = new ReportParamPanel(order.getReport(), false, params);
            panel.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            HorizontalLayout hl = UiFactory.createHLayout(panel);

            UiFactory.createButton(UiIds.LABEL_SAVE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!paramsPanel.validateForm()) {
                        return;
                    }
                    order.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(
                            paramsPanel.collectParametersValues()));
                    CyclicReportOrderDAO.saveOrUpdate(order);
                }
            });
            UiFactory.createButton(UiIds.LABEL_GENERATE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        if (!panel.validateForm()) {
                            return;
                        }
                        ReportMaster rm = new ReportMaster(order.getReport().getContent(), order.getReport().getId().toString(),
                                new ReportTemplateProvider());
                        byte[] reportData = rm.generateAndExportReport(order.getOutputFormat(),
                                new HashMap<String, Object>(panel.collectParametersValues()),
                                org.apertereports.dao.utils.ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), order.getReport().getReportname(), reportData,
                                order.getOutputFormat());
                    } catch (AperteReportsException e) {
                        throw new AperteReportsRuntimeException(e);
                    }
                }
            });
            return panel;
        }
    }

    private class EditCyclicReportPanel extends Panel {

        private CyclicReportOrder order;
        private EditCyclicReportForm form;
        private boolean newItem;

        public EditCyclicReportPanel(CyclicReportOrder order, boolean newItem) {
            this.newItem = newItem;
            this.order = order;

            setCaption(VaadinUtil.getValue(newItem ? UiIds.LABEL_ADDING : UiIds.LABEL_EDITION));

            setWidth("100%");
            addComponent(form = new EditCyclicReportForm(order));
            HorizontalLayout buttons = UiFactory.createHLayout(this);
            UiFactory.createButton(UiIds.LABEL_SAVE, buttons, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    save();
                }
            });
            UiFactory.createButton(UiIds.LABEL_CANCEL, buttons, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    cancel();
                }
            });
        }

        protected void cancel() {
            form.discard();
            if (!newItem) {
                list.replaceComponent(this, new CyclicReportPanel(order));
            } else {
                list.removeComponent(this);
            }
            addingNew = false;
        }

        protected void save() {
            try {
                form.commit();
                CyclicReportOrderDAO.saveOrUpdate(order);
                list.replaceComponent(this, new CyclicReportPanel(order));
                addingNew = false;
            } catch (InvalidValueException e) {
                ExceptionUtils.logWarningException("Edit cyclic report: invalid user input", e);
            }
        }
    }

    private class EditCyclicReportForm extends Form {

        private GridLayout layout;

        public EditCyclicReportForm(CyclicReportOrder order) {
            layout = new GridLayout(2, 4);
            layout.setWidth("100%");
            layout.setSpacing(true);
            setLayout(layout);
            setFormFieldFactory(new EditCyclicFormFactory());
            setItemDataSource(new BeanItem<CyclicReportOrder>(order));
            setVisibleItemProperties(Arrays.asList(new String[]{ORDER_REPORT, ORDER_CRON_SPEC, ORDER_RECIPIENT_EMAIL,
                        ORDER_OUTPUT_FORMAT, ORDER_DESCRIPTION}));
            setWidth("100%");
            setWriteThrough(false);
        }

        @Override
        protected void attachField(Object propertyId, Field field) {
            if (propertyId.equals(ORDER_REPORT)) {
                layout.addComponent(field, 0, 0);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_OUTPUT_FORMAT)) {
                layout.addComponent(field, 1, 0);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                layout.addComponent(field, 0, 1, 1, 1);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_CRON_SPEC)) {
                layout.addComponent(field, 0, 2, 1, 2);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_DESCRIPTION)) {
                layout.addComponent(field, 0, 3, 1, 3);
            }
        }
    }

    private class EditCyclicFormFactory extends DefaultFieldFactory {

        private static final String CYCLIC_EDIT_CAPTION = "cyclic.edit.caption.";

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            if (propertyId.equals(ORDER_OUTPUT_FORMAT)) {
                String value = (String) item.getItemProperty(ORDER_OUTPUT_FORMAT).getValue();
                if (value == null) {
                    value = ReportType.PDF.name();
                }
                ReportType selectedValue = ReportType.valueOf(value);
                ComboBox format = ComponentFactory.createFormatCombo(selectedValue, UiIds.LABEL_FORMAT);
                format.setRequired(true);
                format.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT));
                format.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_FORMAT));
                //format.setWidth("100%");
                return format;
            } else if (propertyId.equals(ORDER_REPORT)) {
                ComboBox reportname = ComponentFactory.createReportTemplateCombo(
                        user, (ReportTemplate) item.getItemProperty(ORDER_REPORT).getValue(), CYCLIC_EDIT_CAPTION + propertyId);
                reportname.setRequired(true);
                reportname.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME));
                reportname.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME));
                //reportname.setWidth("100%");
                return reportname;
            } else if (propertyId.equals(ORDER_DESCRIPTION)) {
                TextField field = (TextField) super.createField(item, propertyId, uiContext);
                field.setWidth("100%");
                field.setCaption(VaadinUtil.getValue(UiIds.LABEL_DESCRIPTION));
                field.setNullRepresentation("");
                field.setInputPrompt(VaadinUtil.getValue(UiIds.LABEL_DESCRIPTION));
                return field;
            } else {
                Field field = super.createField(item, propertyId, uiContext);
                field.setWidth("150px");
                field.setCaption(VaadinUtil.getValue(CYCLIC_EDIT_CAPTION + propertyId));
                field.setRequired(true);
                if (propertyId.equals(ORDER_CRON_SPEC) || propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                    ((TextField) field).setNullRepresentation("");
                    ((TextField) field).setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT + propertyId));
                }
                field.setRequired(true);
                field.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR + propertyId));
                if (propertyId.equals(ORDER_CRON_SPEC)) {
                    field.addValidator(new CronExpressionValidator(VaadinUtil.getValue(VALIDATION_CRON_EXPRESSION)));
                }
                if (propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                    field.addValidator(new EmailValidator(VaadinUtil.getValue(VALIDATION_EMAIL)));
                }
                return field;
            }
        }
    }
}
