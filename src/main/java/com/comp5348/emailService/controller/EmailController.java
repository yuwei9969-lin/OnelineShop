// EmailController.java
package com.comp5348.emailService.controller;

import com.comp5348.emailService.model.EmailLog;
import com.comp5348.emailService.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailLogRepository emailLogRepository;

    @Autowired
    public EmailController(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    /**
     * 获取所有邮件日志
     */
    @GetMapping
    public List<EmailLog> getAllEmailLogs() {
        return emailLogRepository.findAll();
    }

    /**
     * 根据用户 ID 获取邮件日志
     */
    @GetMapping("/user/{userId}")
    public List<EmailLog> getEmailLogsByUserId(@PathVariable Long userId) {
        return emailLogRepository.findByUserId(userId);
    }

    /**
     * 根据邮件日志 ID 获取邮件日志
     */
    @GetMapping("/{id}")
    public EmailLog getEmailLogById(@PathVariable Long id) {
        return emailLogRepository.findById(id).orElse(null);
    }
}
