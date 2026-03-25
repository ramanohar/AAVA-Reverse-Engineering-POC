package com.collaberadigital.cove.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.collaberadigital.cove.model.PerformanceResponse;
import com.collaberadigital.cove.service.OneViewService;

@CrossOrigin
@RestController
public class PerformaceController {
	
	
	@Autowired
	private OneViewService oneViewService;
	
	@GetMapping("/performance")
    public ResponseEntity<PerformanceResponse> getPerformance(@RequestParam String account, @RequestParam(required = false) String performanceType) {

        PerformanceResponse performanceResponse = oneViewService.getPerFormaceData(performanceType,account);

        return ResponseEntity.ok(performanceResponse);
    }
	

}
