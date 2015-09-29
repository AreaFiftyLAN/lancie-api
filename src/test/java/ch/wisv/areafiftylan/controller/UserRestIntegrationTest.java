package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.Application;
import ch.wisv.areafiftylan.service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest("server.port=0")
public class UserRestIntegrationTest {

    @Value("${local.server.port}")
    int port;

    //Required to Generate JSON content from Java objects
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //Required to delete the data added for tests.
    //Directly invoke the APIs interacting with the DB
    @Autowired
    private UserRepository userRepository;

    //Test RestTemplate to invoke the APIs.
    private RestTemplate restTemplate = new TestRestTemplate();

    private String serverPath;

    @Before
    public void init() {
        userRepository.deleteAll();
        serverPath = "http://localhost:" + port;
    }

    @Test
    public void testCreateUserApi() throws JsonProcessingException, URISyntaxException {

        //Building the Request body data
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "testUser1");
        requestBody.put("email", "testuser@mail.com");
        requestBody.put("password", "testPassword");
        HttpEntity<String> httpEntity = getStringHttpEntity(requestBody);

        //Invoking the API
        URI uri = new URI(serverPath + "/users");

        ResponseEntity<Map> response = restTemplate.postForEntity(uri, httpEntity, Map.class);

        assertNotNull(response);

        assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        HttpHeaders headers = response.getHeaders();
        URI location = headers.getLocation();
        assertThat(location.toString(), startsWith(serverPath + "/users/"));

        assertNull(response.getBody());
    }

    @Test
    public void testCreateUserUniqueUsername() throws JsonProcessingException, URISyntaxException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("username", "testUser1");
        requestBody.put("email", "testuser@mail.com");
        requestBody.put("password", "testPassword");
        HttpEntity<String> httpEntity = getStringHttpEntity(requestBody);

        URI uri = new URI(serverPath + "/users");

        ResponseEntity<Map> response = restTemplate.postForEntity(uri, httpEntity, Map.class);

        Map<String, Object> requestBody2 = new HashMap<>();
        requestBody.put("username", "testUser1");
        requestBody.put("email", "testuser2@mail.com");
        requestBody.put("password", "testPassword2");
        HttpEntity<String> httpEntity2 = getStringHttpEntity(requestBody2);

        ResponseEntity<Map> response2 = restTemplate.postForEntity(uri, httpEntity2, Map.class);

        assertEquals(HttpStatus.CONFLICT, response2.getStatusCode());
    }

    private HttpEntity<String> getStringHttpEntity(Map<String, Object> requestBody) throws JsonProcessingException {
        //Creating http entity object with request body and headers
        return new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(requestBody), getHttpHeaders());
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        return requestHeaders;
    }
}