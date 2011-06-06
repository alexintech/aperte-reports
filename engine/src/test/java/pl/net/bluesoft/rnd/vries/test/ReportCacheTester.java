/**
 *
 */
package pl.net.bluesoft.rnd.vries.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.codec.binary.Base64;

import pl.net.bluesoft.rnd.vries.engine.ReportCache;

/**
 * @author MW
 */
public class ReportCacheTester {

    private String defaultTestReport = "/test vries 44.jrxml";

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.vries.engine.ReportCache#getReport(java.lang.Integer)}
     * . Test method for
     * {@link pl.net.bluesoft.rnd.vries.engine.ReportCache#putReport(java.lang.Integer, net.sf.jasperreports.engine.JasperReport)}
     * . Test method for
     * {@link pl.net.bluesoft.rnd.vries.engine.ReportCache#removeReport(java.lang.Integer)}
     * .
     *
     * @throws IOException
     * @throws JRException
     */
    @Test
    public final void testGetReport() throws IOException, JRException {
        String ds = readTestFileToString(defaultTestReport);
        ByteArrayInputStream contentInputStream = new ByteArrayInputStream(ds.getBytes());
        JasperReport testReport = JasperCompileManager.compileReport(contentInputStream);
        Integer report1 = 1;
        Integer report2 = 2;

        /* check if empty */
        assertNull(ReportCache.getReport(report1));

        /* add item and check if it's there */
        ReportCache.putReport(report1, testReport);
        assertEquals(ReportCache.getReport(report1), testReport);

        /* check if other items are empty */
        assertNull(ReportCache.getReport(report2));

        /* add another item and check if it's there */
        ReportCache.putReport(report2, testReport);
        assertEquals(ReportCache.getReport(report2), testReport);

        /* remove item and check if it's empty */
        ReportCache.removeReport(report1);
        assertNull(ReportCache.getReport(report1));

        /* check if other items are unaffected */
        assertEquals(ReportCache.getReport(report2), testReport);

    }

    /**
     * @return
     * @throws IOException
     */
    private String readTestFileToString(String path) throws IOException {
        StringBuffer ds = new StringBuffer();
        InputStream s = getClass().getResourceAsStream(path);
        int c = 0;
        while ((c = s.read()) >= 0) {
            ds.append((char) c);
        }
        return ds.toString();
    }
}
