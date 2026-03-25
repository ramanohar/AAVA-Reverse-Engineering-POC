package com.collaberadigital.cove.service;

import com.collaberadigital.cove.dto.response.ActionHistoryDto;
import com.collaberadigital.cove.model.entity.ActionHistory;
import com.collaberadigital.cove.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ActionHistoryService {
    void saveActionHistory(String adminEmail,
                           String userEmail,
                           String status,
                           String comments,
                           String actionType);

    Mono<Page<ActionHistoryDto>> getActionHistoryPagination(Map<String, Object> headers,
                                                            int page,
                                                            int size,
                                                            String sortBy,
                                                            boolean ascending,

                                                            List<String> statuses,
                                                            List<String>  role,
                                                            String date,
                                                            String company,
                                                            String name,
                                                            String email);
}
