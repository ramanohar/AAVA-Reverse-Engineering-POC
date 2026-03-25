package com.collaberadigital.cove.service.impl;

import com.collaberadigital.cove.dto.response.ActionHistoryDto;
import com.collaberadigital.cove.model.entity.ActionHistory;
import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.ActionHistoryRepo;
import com.collaberadigital.cove.repository.UserRepository;
import com.collaberadigital.cove.service.ActionHistoryService;
import com.collaberadigital.cove.utils.JsonUtility;
import com.collaberadigital.cove.utils.PayloadUtil;
import com.collaberadigital.cove.utils.constant.AccountStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ActionHistoryServiceImpl implements ActionHistoryService {

    private final ActionHistoryRepo actionHistoryRepo;
    private final UserRepository userRepository;

    ActionHistoryServiceImpl(ActionHistoryRepo actionHistoryRepo,UserRepository userRepository){
        this.actionHistoryRepo = actionHistoryRepo;
        this.userRepository = userRepository;
    }

    @Override
    public void saveActionHistory( String adminEmail,
                                   String userEmail,
                                   String status,
                                   String comments,
                                   String actionType) {

       try {
           UserEntity admin = userRepository.findByEmail(adminEmail).orElseThrow();
           UserEntity userUpdated = userRepository.findByEmail(userEmail).orElseThrow();


           ActionHistory actionHistory = PayloadUtil.actionHistoryRequestPayload(
                   userUpdated.getFirstname()+ " "+userUpdated.getLastname(),
                   userEmail,
                   userUpdated.getRole(),
                   userUpdated.getCompany(),
                   admin.getFirstname()+ " "+admin.getLastname(),
                   adminEmail,
                   userUpdated.getRegistrationId(),
                   status,
                   comments,
                   actionType
           );
           log.info("Action History: {}", JsonUtility.toJson(actionHistory));
           actionHistoryRepo.save(actionHistory);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    @Override
    public Mono<Page<ActionHistoryDto>> getActionHistoryPagination(Map<String, Object> headers, int page, int size, String sortBy, boolean ascending, List<String> statuses, List<String> role, String date, String company,String name,String email) {

        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Mono<Page<ActionHistory>> actionHistoryPageMono;

        if (date == null || date.isEmpty()) {
            actionHistoryPageMono = Mono.just(actionHistoryRepo.findByActionHistoryWithoutDate(statuses, role,company,name,email, pageable));

        } else {
            LocalDate localDate = LocalDate.parse(date);
            actionHistoryPageMono = Mono.just(actionHistoryRepo.findByActionHistoryWithDate(statuses, role, localDate,company,name,email, pageable));
        }
        return actionHistoryPageMono.map(paginationData -> {
            List<ActionHistoryDto> dtoList =  paginationData.getContent().stream()
                     .map(contentData-> {
                         String content = JsonUtility.toJson(contentData);
                        return JsonUtility.toObject(content,ActionHistoryDto.class);

                     }).collect(Collectors.toList());
            return new PageImpl<>(dtoList, paginationData.getPageable(), paginationData.getTotalElements());
        });


    }
}
