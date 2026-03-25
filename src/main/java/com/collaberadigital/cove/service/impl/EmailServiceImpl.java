package com.collaberadigital.cove.service.impl;

import com.collaberadigital.cove.exception.reactive.CustomException;
import com.collaberadigital.cove.service.EmailService;
import com.collaberadigital.cove.utils.constant.ExceptionStatus;
import com.collaberadigital.cove.utils.constant.ExceptionTitle;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    @Qualifier("EmailOnboardingStatus")
    private JavaMailSender sendingEmail;

    @Autowired
    private Configuration config;

    @Value("${spring.mail.username}")
    private String fromEmail;

//    EmailServiceImpl(){
//
//    }

    @Override
    public boolean sendEmailReject(String toEmail, String description,String name) {

        try {

            //Send to user who delete
            MimeMessage message = sendingEmail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            model.put("description", description);
            Template t = config.getTemplate("email-template-rejected.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            helper.setTo(toEmail);
            helper.setText(html, true);
            helper.setSubject("COVE - Account Rejected");
            helper.setFrom(fromEmail);

            sendingEmail.send(message);

            return true;


        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("451", ExceptionTitle.ErrorTitle, ExceptionStatus.Error, "Unable to send reject email to "+toEmail);

        }
    }

    @Override
    public boolean sendEmailApproved(String toEmail, String name) {
        try {

            //Send to user who delete
            MimeMessage message = sendingEmail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            Template t = config.getTemplate("email-template-approved.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            helper.setTo(toEmail);
            helper.setText(html, true);
            helper.setSubject("COVE - Account Approved");
            helper.setFrom(fromEmail);

            sendingEmail.send(message);

            return true;


        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("451", ExceptionTitle.ErrorTitle, ExceptionStatus.Error, "Unable to send reject email to "+toEmail);

        }
    }

    @Override
    public boolean sendEmailPendingApproval(String toEmail, String name) {
        try {

            //Send to user who delete
            MimeMessage message = sendingEmail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            Template t = config.getTemplate("email-template-pending-approval.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            helper.setTo(toEmail);
            helper.setText(html, true);
            helper.setSubject("COVE - Pending Approval");
            helper.setFrom(fromEmail);

            sendingEmail.send(message);

            return true;


        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("451", ExceptionTitle.ErrorTitle, ExceptionStatus.Error, "Unable to send pending approval email to "+toEmail);

        }
    }
}
