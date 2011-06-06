/**
 *
 */
package pl.net.bluesoft.rnd.vries.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import pl.net.bluesoft.rnd.vries.dao.CyclicReportOrderDAO;
import pl.net.bluesoft.rnd.vries.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.vries.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.vries.data.CyclicReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportTemplate;
import pl.net.bluesoft.rnd.vries.exception.VriesException;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.DeCoder;
import pl.net.bluesoft.rnd.vries.util.TestUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author MW
 */
public class CyclicReportOrderJobTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        TestUtil.initDB();
    }

    @Test
    public void testParams() {
        Map<String, String> parameters1 = new HashMap<String, String>();
        parameters1.put("id1", "1");
        parameters1.put("xxx", "44");

        char[] xml = DeCoder.serializeParameters(parameters1);
        System.out.println(xml);

        Map<String, String> parameters2 = DeCoder.deserializeParameters(xml);

        assertTrue(parameters1.keySet().containsAll(parameters2.keySet()));
        assertTrue(parameters2.keySet().containsAll(parameters1.keySet()));
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.vries.scheduler.CyclicReportOrderJob#processOrder(org.quartz.JobDetail)}
     * .
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws VriesException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public final void testProcessOrder() throws SecurityException, NoSuchMethodException, VriesException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ReportOrder rep1 = null;
        ReportTemplate reportTemplate1 = null;
        CyclicReportOrder cro = null;
        try {
            reportTemplate1 = new ReportTemplate();
            ReportTemplateDAO.saveOrUpdate(reportTemplate1);

            Map<String, String> parameters1 = new HashMap<String, String>();
            parameters1.put("id1", "1");
            String format = "a", recipientEmail = "b";
            cro = new CyclicReportOrder();
            cro.setOutputFormat(format);
            cro.setParametersXml(DeCoder.serializeParameters(parameters1));
            cro.setRecipientEmail(recipientEmail);
            cro.setReport(reportTemplate1);
            Long croId = CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cro);

            JobDetail jobDetail = new JobDetail(cro.getId().toString(), CyclicReportOrder.class.toString(),
                    CyclicReportOrderJob.class);
            CyclicReportOrderJob croj = new CyclicReportOrderJob();

            Method method = CyclicReportOrderJob.class.getDeclaredMethod("processOrder", JobDetail.class);
            method.setAccessible(true);
            rep1 = (ReportOrder) method.invoke(croj, jobDetail);

            assertNotNull("ReportOrder not created", rep1);

            ReportOrder repOrderFromDB = ReportOrderDAO.fetchReport(rep1.getId());
            assertEquals(reportTemplate1.getId(), repOrderFromDB.getReport().getId());
            assertEquals(cro.getOutputFormat(), repOrderFromDB.getOutputFormat());
            assertArrayEquals(cro.getParametersXml(), repOrderFromDB.getParametersXml());
            assertEquals(cro.getRecipientEmail(), repOrderFromDB.getRecipientEmail());
            assertEquals(Constants.CYCLIC_REPORT_ORDER_RESPONSE_Q, repOrderFromDB.getReplyToQ());

            CyclicReportOrder cyclicReportOrder1 = CyclicReportOrderDAO.fetchCyclicReportOrder(croId);
            assertNotNull(cyclicReportOrder1.getProcessedOrder());
            assertEquals(repOrderFromDB.getId(), cyclicReportOrder1.getProcessedOrder().getId());

        }
        finally {
            try {
                CyclicReportOrderDAO.removeCyclicReportOrder(cro);
            }
            finally {
                try {
                    ReportOrderDAO.removeReportOrder(rep1);
                }
                finally {
                    ReportTemplateDAO.remove(reportTemplate1);
                }
            }
        }
    }

}
