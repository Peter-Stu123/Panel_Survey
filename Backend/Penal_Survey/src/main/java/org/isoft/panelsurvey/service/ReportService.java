package org.isoft.panelsurvey.service;

import org.isoft.panelsurvey.vo.ReportVO;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {
    
    ReportVO generateReport(Long questionnaireId);
    
    ReportVO getReportByQuestionnaireId(Long questionnaireId);
    
    ReportVO getReportById(Long id);
    
    String exportToPdf(Long reportId);
    
    String exportToExcel(Long reportId);
    
    /**
     * 从Excel文件生成报告（针对医生的专业分析报告）
     * 
     * @param file 问卷网导出的Excel文件
     * @param questionnaireId 问卷ID
     * @return 生成的报告
     */
    ReportVO generateReportFromExcel(MultipartFile file, Long questionnaireId);
}

