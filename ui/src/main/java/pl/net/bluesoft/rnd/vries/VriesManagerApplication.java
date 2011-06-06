package pl.net.bluesoft.rnd.vries;

import com.vaadin.ui.Window;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.vries.components.VriesManagerComponent;

/**
 * This is the main report administration portlet.
 * <p/>A user can add new, modify or delete existing report templates from the application.
 * The reports are displayed in a table, providing information about the name of the report,
 * description and permissions whether to let users generate in the background or directly.
 * It is also possible to disable a report so that is no longer available in the dashboards,
 * but remains in the database.
 * <p/>To add a new report one needs to upload a JRXML Jasper template using the panel on the right
 * of the table. Once the report is uploaded one can configure the permissions and generate a
 * sample report for checking.
 */
public class VriesManagerApplication extends AbstractVriesApplication {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        VriesManagerComponent manager = new VriesManagerComponent();

        Window mainWindow = new Window(TM.get("manager.window.title"), manager);

        setMainWindow(mainWindow);
    }

}
