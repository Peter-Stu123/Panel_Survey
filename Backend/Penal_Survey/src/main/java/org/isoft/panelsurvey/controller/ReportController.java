package org.isoft.panelsurvey.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.isoft.panelsurvey.common.Result;
import org.isoft.panelsurvey.service.ReportService;
import org.isoft.panelsurvey.vo.ReportVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "报告管理", description = "调查报告生成、导出等操作")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @Operation(summary = "生成调查报告", description = "根据问卷ID生成调查报告")
    @PostMapping("/generate/{questionnaireId}")
    public Result<ReportVO> generateReport(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId) {
        ReportVO report = reportService.generateReport(questionnaireId);
        return Result.success("报告生成成功", report);
    }

    @Operation(summary = "获取报告详情", description = "根据报告ID获取报告详细信息")
    @GetMapping("/{id}")
    public Result<ReportVO> getReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id) {
        ReportVO report = reportService.getReportById(id);
        return Result.success(report);
    }

    @Operation(summary = "根据问卷ID获取报告", description = "根据问卷ID获取对应的报告")
    @GetMapping("/questionnaire/{questionnaireId}")
    public Result<ReportVO> getReportByQuestionnaireId(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId) {
        ReportVO report = reportService.getReportByQuestionnaireId(questionnaireId);
        return Result.success(report);
    }

    @Operation(summary = "导出Excel报告", description = "将报告导出为Excel格式")
    @GetMapping("/export/excel/{reportId}")
    public Result<String> exportExcelReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId) {
        String downloadUrl = reportService.exportToExcel(reportId);
        return Result.success("Excel报告导出成功", downloadUrl);
    }

    @Operation(summary = "导出PDF报告", description = "将报告导出为PDF格式")
    @GetMapping("/export/pdf/{reportId}")
    public Result<String> exportPdfReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long reportId) {
        String downloadUrl = reportService.exportToPdf(reportId);
        return Result.success("PDF报告导出成功", downloadUrl);
    }

    @Operation(summary = "从Excel生成报告", description = "从上传的Excel文件生成报告")
    @PostMapping("/generate/excel/{questionnaireId}")
    public Result<ReportVO> generateReportFromExcel(
            @Parameter(description = "问卷ID", required = true)
            @PathVariable Long questionnaireId,
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file) {
        ReportVO report = reportService.generateReportFromExcel(file, questionnaireId);
        return Result.success("Excel数据分析完成", report);
    }
}