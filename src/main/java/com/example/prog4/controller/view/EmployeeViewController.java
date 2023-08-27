package com.example.prog4.controller.view;

import com.example.prog4.config.CompanyConf;
import com.example.prog4.controller.PopulateController;
import com.example.prog4.controller.mapper.EmployeeMapper;
import com.example.prog4.model.Employee;
import com.example.prog4.model.EmployeeAge;
import com.example.prog4.model.EmployeeFilter;
import com.example.prog4.service.EmployeeService;
import com.example.prog4.service.PdfGeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Controller
@RequestMapping("/employee")
@AllArgsConstructor
public class EmployeeViewController extends PopulateController {
    private EmployeeService employeeService;
    private EmployeeMapper employeeMapper;

    private final PdfGeneratorService pdfGeneratorService;
    @GetMapping("/list")
    public String getAll(
            @ModelAttribute EmployeeFilter filters,
            Model model,
            HttpSession session
    ) {
        model.addAttribute("employees", employeeService.getAll(filters).stream().map(employeeMapper::toView).toList())
                .addAttribute("employeeFilters", filters)
                .addAttribute("directions", Sort.Direction.values());
        session.setAttribute("employeeFiltersSession", filters);

        return "employees";
    }

    @GetMapping("/create")
    public String createEmployee(Model model) {
        model.addAttribute("employee", Employee.builder().build());
        return "employee_creation";
    }

    @GetMapping("/edit/{eId}")
    public String editEmployee(@PathVariable String eId, Model model) {
        Employee toEdit = employeeMapper.toView(employeeService.getOne(eId));
        model.addAttribute("employee", toEdit);

        return "employee_edition";
    }

    @GetMapping("/show/{eId}")
    public String showEmployee(@PathVariable String eId, Model model) {
        Employee toShow = employeeMapper.toView(employeeService.getOne(eId));
        model.addAttribute("employee", toShow);

        return "employee_show";
    }

    @GetMapping("/pdf/{eId}")
    public void generatePdf(
            @PathVariable String eId, Model model,
            HttpServletResponse response
    ) throws IOException {
        EmployeeAge toPdf = employeeMapper.toViewPdf(employeeService.getOne(eId));
        CompanyConf companyConf = new CompanyConf();
        Context context = new Context();
        // Add data to the context
        context.setVariable("employee", toPdf);
        context.setVariable("companyConf", companyConf);

        // Generate the PDF from the Thymeleaf template
        byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtmlTemplate("employee_pdf", context);

        if (pdfBytes != null) {
            // Set the content type and headers for PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=employee.pdf");

            // Write the PDF to the response output stream
            response.getOutputStream().write(pdfBytes);
            response.flushBuffer();
        } else {
            // Handle the case where PDF generation failed
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "PDF generation failed");
        }
    }
    @GetMapping("/")
    public String home() {
        return "redirect:/employee/list";
    }
}
