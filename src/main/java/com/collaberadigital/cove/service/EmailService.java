package com.collaberadigital.cove.service;

import reactor.core.publisher.Mono;

public interface EmailService {

    boolean sendEmailReject(String toEmail,String description,String name);
    boolean sendEmailApproved(String toEmail,String name);
    boolean sendEmailPendingApproval(String toEmail,String name);
}
