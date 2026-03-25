package com.collaberadigital.cove.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.collaberadigital.cove.model.DeliveryRagProjectList;
import com.collaberadigital.cove.model.LoginRequestOneView;
import com.collaberadigital.cove.model.PerformaceModel;
import com.collaberadigital.cove.model.PerformanceResponse;
import com.collaberadigital.cove.model.PerformanceResponseData;
import com.collaberadigital.cove.service.OneViewService;

@Service
public class OneViewServiceImpl implements OneViewService {

	@Override
	public PerformanceResponse getPerFormaceData(String projectType, String account) {
		PerformanceResponse performanceResponse = new PerformanceResponse();
		List<PerformanceResponseData> resData = new ArrayList<PerformanceResponseData>();
		
		
		    String url = "https://cdoneview.avateam.io/user/signin";
	        RestTemplate restTemplate = new RestTemplate();

	        LoginRequestOneView loginRequest = new LoginRequestOneView();
	        loginRequest.setUserName("Nagahemanthkn");
	        loginRequest.setPassword("Digital@$2458");

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Content-Type", "application/json");

	        HttpEntity<LoginRequestOneView> request = new HttpEntity<>(loginRequest, headers);

	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

	        String token = response.getHeaders().getFirst("X-Ava-Access-Token");
	        
	        String baseUrl = "https://cdoneview.avateam.io/ava/oneview/internal/api/dashboard/rag/list";

	        LocalDate today = LocalDate.now();
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	        String formattedDate = today.format(formatter);

	        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
	                .queryParam("weekEndingDates", formattedDate);
	                if(projectType!=null && !projectType.equals("All")) {
	                	uriBuilder.queryParam("projectTypes", projectType);

	                }
	                
	        String urlData = uriBuilder.toUriString();

	        RestTemplate restTemplate1 = new RestTemplate();

	        HttpHeaders headers1 = new HttpHeaders();
	        headers1.set("X-Ava-Access-Token", token);

	        HttpEntity<String> request1 = new HttpEntity<>(headers1);

	        ResponseEntity<PerformaceModel> response1 = restTemplate.exchange(urlData, HttpMethod.GET, request1, PerformaceModel.class);
	        
	        List<DeliveryRagProjectList> resposneData = response1.getBody().getData().getDeliveryRagProjectList();
	        
	        if(resposneData.size()==0) {
	            LocalDate now = LocalDate.now();
	            LocalDate previousDay = now.minusDays(1);
	            String formattedDate1 = previousDay.format(formatter);
	            
	            UriComponentsBuilder uriBuilder1 = UriComponentsBuilder.fromHttpUrl(baseUrl)
		                .queryParam("weekEndingDates", formattedDate1);
	            
	            String urlData1 = uriBuilder1.toUriString();

		        RestTemplate restTemplate2 = new RestTemplate();

		        HttpHeaders headers2 = new HttpHeaders();
		        headers2.set("X-Ava-Access-Token", token);

		        HttpEntity<String> request2 = new HttpEntity<>(headers2);

		        ResponseEntity<PerformaceModel> response2 = restTemplate.exchange(urlData1, HttpMethod.GET, request2, PerformaceModel.class);
		        resposneData = response2.getBody().getData().getDeliveryRagProjectList();
	        }
	        for (DeliveryRagProjectList  data :resposneData) {
				if(data.getClient().equals(account)) {
					PerformanceResponseData performanceResponseData = new PerformanceResponseData();
					performanceResponseData.setProject(data.getProject());
					performanceResponseData.setRagSatus(data.getRagStatus());
					resData.add(performanceResponseData);
				}
			}
	        performanceResponse.setData(resData);
	        
		return performanceResponse;
	}

}
