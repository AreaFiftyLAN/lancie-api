package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.Application;
import ch.wisv.areafiftylan.model.Profile;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
public class UserRestControllerTest {

    //Required to Generate JSON content from Java objects
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //Required to delete the data added for tests.
    //Directly invoke the APIs interacting with the DB
    @Autowired
    private UserRepository userRepository;

    //Test RestTemplate to invoke the APIs.
    private RestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testCreateUserApi() throws JsonProcessingException {

        //Building the Request body data
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("username", "testUser1");
        requestBody.put("email", "testuser@mail.com");
        requestBody.put("password", "testPassword");
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Authorization", "Basic dXNlcjpwYXNzd29yZA==");

        //Creating http entity object with request body and headers
        HttpEntity<String> httpEntity =
                new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody), requestHeaders);

        //Invoking the API
        Map<String, Object> apiResponse =
                restTemplate.postForObject("http://localhost:8080/users", httpEntity, Map.class, Collections.EMPTY_MAP);

        assertNotNull(apiResponse);

        //Asserting the response of the API.
//        String message = apiResponse.get("message").toString();
//        assertEquals("Book created successfully", message);
        Long userId = Integer.toUnsignedLong((int) apiResponse.get("id"));

        assertNotNull(userId);

        //Fetching the Book details directly from the DB to verify the API succeeded
        User userFromDb = userRepository.findOne(userId);
        assertEquals("testUser1", userFromDb.getUsername());
        assertEquals("testuser@mail.com", userFromDb.getEmail());
        //Delete the data added for testing
        userRepository.delete(userId);

    }


}