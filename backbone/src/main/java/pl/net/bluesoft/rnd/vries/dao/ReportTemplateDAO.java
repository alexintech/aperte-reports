/**
 *
 */
package pl.net.bluesoft.rnd.vries.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import pl.net.bluesoft.rnd.vries.data.ReportTemplate;
import pl.net.bluesoft.rnd.vries.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.vries.util.ExceptionUtil;
import pl.net.bluesoft.rnd.vries.util.WHS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author MW
 */
public class ReportTemplateDAO {

    public static Collection<ReportTemplate> fetchAllReports(final boolean onlyActive) {
        return new WHS<Collection<ReportTemplate>>() {
            @Override
            public Collection<ReportTemplate> lambda() {
                Criteria criteria = sess.createCriteria(ReportTemplate.class);
                if (onlyActive) {
                    criteria.add(Restrictions.eq("active", true));
                }
                return criteria.list();
            }
        }.p();
    }

    public static List<ReportTemplate> fetchReportsByName(final String reportName) {
        return new WHS<List<ReportTemplate>>(false) {
            @Override
            public List<ReportTemplate> lambda() {
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class)
                        .add(Restrictions.eq("reportname", reportName))
                        .addOrder(org.hibernate.criterion.Order.desc("id")).list();
                return list != null ? list : new ArrayList<ReportTemplate>();
            }
        }.p();
    }

    public static ReportTemplate fetchReport(final Integer reportId) {
        return new WHS<ReportTemplate>() {
            @Override
            public ReportTemplate lambda() {
                ReportTemplate rt = (ReportTemplate) sess.createCriteria(ReportTemplate.class)
                        .add(Restrictions.eq("id", reportId)).add(Restrictions.eq("active", true)).uniqueResult();
                return rt;
            }
        }.p();
    }

    public static List<ReportTemplate> fetchReports(final Integer... reportId) {
        return new WHS<List<ReportTemplate>>(false) {
            @Override
            public List<ReportTemplate> lambda() {
                if (reportId.length == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class)
                        .add(Restrictions.in("id", reportId)).add(Restrictions.eq("active", true)).list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                return list;
            }
        }.p();
    }

    public static void remove(Integer reportId) {
        final ReportTemplate report = fetchReport(reportId);
        if (report != null) {
            new WHS<Void>() {
                @Override
                public Void lambda() {
                    sess.delete(report);
                    return null;
                }
            }.p();
        }
    }

    public static void remove(final ReportTemplate report) {
        new WHS<Void>() {
            @Override
            public Void lambda() {
                sess.delete(report);
                return null;
            }
        }.p();
    }

    public static void saveOrUpdate(final ReportTemplate report) {

        try {
            new WHS<Void>() {
                @Override
                public Void lambda() {
                    sess.saveOrUpdate(report);
                    return null;
                }
            }.p();

        }
        catch (ConstraintViolationException e) {
            ExceptionUtil.logSevereException(e);
            throw new VriesRuntimeException("manager.form.duplicate.report.name");
        }
    }
}
