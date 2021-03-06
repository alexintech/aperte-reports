/**
 * 
 */
package org.apertereports.engine;

import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperReport;

/**
 * 
 * Wrapper for class {@link net.sf.jasperreports.engine.JasperPrint} used by {@link ReportMaster} for
 * storing information about subreports.
 * 
 */
public class AperteReport {
	private Map<String, AperteReport> subreports = new HashMap<String, AperteReport>();
	private JasperReport jasperReport;

	/**
	 * Constructs a report by specifying the template report and compile
	 * information.
	 * 
	 */
	public AperteReport(JasperReport jasperReport) {
		this.jasperReport = jasperReport;
	}

	public Map<String, AperteReport> getAllNestedSubreports() {
		Map<String, AperteReport> map = new HashMap<String, AperteReport>();
		map.putAll(subreports);
		for (AperteReport subreport : map.values()) {
			map.putAll(subreport.getAllNestedSubreports());
		}
		return map;
	}

	public Map<String, AperteReport> getSubreports() {
		return subreports;
	}

	public void setSubreports(Map<String, AperteReport> subreports) {
		this.subreports = subreports;
	}

	public JasperReport getJasperReport() {
		return jasperReport;
	}

	public void setJasperReport(JasperReport jasperReport) {
		this.jasperReport = jasperReport;
	}

}
