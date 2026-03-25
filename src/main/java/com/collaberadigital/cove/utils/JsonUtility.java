package com.collaberadigital.cove.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public final class JsonUtility {

	private static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	public static String toJson(Object requestToLog) {
		
		String json = "";
		try {
			json = objectMapper.writeValueAsString(requestToLog);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			log.info("Error in toJson: {}",e.getMessage());
		}
		return json;
	}

	public static <T> T toObject(String json, Class<T> type){
		try {
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			return objectMapper.readValue(json, type);
		} catch (IOException e) {
			e.printStackTrace();
			log.info("Error in toObject: {}",e.getMessage());
		}
		return null;
	}


}
