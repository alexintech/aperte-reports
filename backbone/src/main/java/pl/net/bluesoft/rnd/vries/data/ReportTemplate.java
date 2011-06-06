package pl.net.bluesoft.rnd.vries.data;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Holds all the Jasper report template data and specific report configuration.
 */
@Entity
@Table(schema = "public", name = "vries_report_template")
public class ReportTemplate implements Serializable {
    private static final long serialVersionUID = -7196776812526154078L;

    /**
     * Indicates this report is active or not.
     */
    @Column
    private Boolean active = true;

    /**
     * JRXML data formatted in Base64 manner.
     */
    @Lob
    @Type(type = "org.hibernate.type.PrimitiveCharacterArrayClobType")
    @Column
    @Basic(fetch = FetchType.LAZY)
    private char[] content;

    /**
     * Date of creation.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date created = new Date();

    /**
     * Description of the report template.
     */
    @Column
    private String description;

    /**
     * Filename it was uploaded from.
     */
    @Column
    private String filename;

    /**
     * Should report engine allow online display.
     */
    @Column(name = "allow_online_display")
    private Boolean allowOnlineDisplay = true;

    /**
     * Should report engine allow background order generation.
     */
    @Column(name = "allow_background_order")
    private Boolean allowBackgroundOrder = true;

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "id", nullable = false, length = 10)
    private Integer id;

    /**
     * Report name taken from JRXML it was uploaded from (the <code>name</code> attribute of <code>jasperReport</code> tag).
     */
    @Column(unique = true)
    private String reportname;

    public boolean getActive() {
        return active != null && active;
    }

    public char[] getContent() {
        return content;
    }

    public Date getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getId() {
        return id;
    }

    public String getReportname() {
        return reportname;
    }

    public Boolean getAllowBackgroundOrder() {
        return allowBackgroundOrder != null && allowBackgroundOrder;
    }

    public Boolean getAllowOnlineDisplay() {
        return allowOnlineDisplay != null && allowOnlineDisplay;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAllowBackgroundOrder(Boolean allowBackgroundOrder) {
        this.allowBackgroundOrder = allowBackgroundOrder;
    }

    public void setAllowOnlineDisplay(Boolean allowOnlineDisplay) {
        this.allowOnlineDisplay = allowOnlineDisplay;
    }

    public void setContent(char[] content) {
        this.content = content.clone();
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setReportname(String reportname) {
        this.reportname = reportname;
    }

    /**
     * Field identifiers for Vaadin tables.
     */
    public enum Fields {
        ACTIVE, CONTENT, CREATED, DESCRIPTION, FILENAME, ALLOW_ONLINE_DISPLAY, ALLOW_BACKGROUND_ORDER, REPORTNAME, ID
    }
}
