package com.example.prog4.controller.view;

import com.example.prog4.config.CompanyConf;
import com.example.prog4.controller.PopulateController;
import com.example.prog4.controller.mapper.EmployeeMapper;
import com.example.prog4.model.Employee;
import com.example.prog4.model.EmployeeAge;
import com.example.prog4.model.EmployeeFilter;
import com.example.prog4.model.enums.AgeCalculationOption;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;

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
 /*
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

  */
 @GetMapping("/pdf/{eId}")
 public void generatePdf(
         @PathVariable String eId,
         @RequestParam(required = false, defaultValue = "BIRTHDAY") AgeCalculationOption ageCalculationOption,
         @RequestParam(required = false) Integer birthdayMinInterval,
         Model model,
         HttpServletResponse response
 ) throws IOException {
     // Obtenez l'employé
     EmployeeAge toPdf = employeeMapper.toViewPdf(employeeService.getOne(eId));

     // Déterminez la date de naissance de l'employé
     LocalDate birthdate = toPdf.getBirthDate();

     // Effectuez le calcul en fonction de l'option choisie
     if (ageCalculationOption == AgeCalculationOption.BIRTHDAY) {
         // par default il est deja calculer du birthday
     } else if (ageCalculationOption == AgeCalculationOption.YEAR_ONLY) {
         // Le calcul de l'âge est basé sur l'année actuelle - année de naissance
         int currentYear = LocalDate.now().getYear();
         int birthYear = birthdate.getYear();       int age = currentYear - birthYear;
         toPdf.setAge(age);
     } else if (ageCalculationOption == AgeCalculationOption.CUSTOM_DELAY) {
         if (birthdayMinInterval != null) {
             // Calcul de l'âge avec un délai personnalisé
             LocalDate currentDate = LocalDate.now();
             LocalDate adjustedBirthday = birthdate.plusDays(birthdayMinInterval);
             int age = Period.between(adjustedBirthday, currentDate).getYears();
             toPdf.setAge(age);
         } else {
             // Gérer le cas où birthdayMinInterval est manquant
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Le paramètre birthdayMinInterval est requis pour l'option CUSTOM_DELAY");
             return;
         }
     }

     CompanyConf companyConf = new CompanyConf();
     Context context = new Context();
     context.setVariable("employee", toPdf);
     context.setVariable("companyConf", companyConf);

     byte[] pdfBytes = pdfGeneratorService.generatePdfFromHtmlTemplate("employee_pdf", context);

     if (pdfBytes != null) {
         response.setContentType("application/pdf");
         response.setHeader("Content-Disposition", "inline; filename=employee.pdf");

         response.getOutputStream().write(pdfBytes);
         response.flushBuffer();
     } else {
         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "La génération du PDF a échoué");
     }
 }
    @GetMapping("/")
    public String home() {
        return "redirect:/employee/list";
    }
}
