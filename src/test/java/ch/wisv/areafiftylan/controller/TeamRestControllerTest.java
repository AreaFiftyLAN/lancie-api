package ch.wisv.areafiftylan.controller;

import ch.wisv.areafiftylan.Application;
import ch.wisv.areafiftylan.model.User;
import ch.wisv.areafiftylan.service.repository.TeamRepository;
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
@WebIntegrationTest("server.port = 0")
public class TeamRestControllerTest {

    @Value("${local.server.port}")
    int port;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplate = new TestRestTemplate();

    private String serverPath;

    @Before
    public void init() {
        // Must be handled with caution, because you can delete the whole DB with this.
        /* teamRepository.deleteAll();
        userRepository.deleteAll(); */
        serverPath = "http://localhost:" + port;
    }

    @Test
    public void testCreateUserApi() throws JsonProcessingException, URISyntaxException {
        User test = new User("Test", "password", "nordin_v_nes@hotmail.com");
        User captain = userRepository.saveAndFlush(test);

        //Building the Request body data
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("teamName", "testTeam1");
        requestBody.put("captainID", captain.getId());
        HttpEntity<String> httpEntity = getStringHttpEntity(requestBody);

        //Invoking the API
        URI uri = new URI(serverPath + "/teams");

        ResponseEntity<Map> response = restTemplate.postForEntity(uri, httpEntity, Map.class);

        assertNotNull(response);

        assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        HttpHeaders headers = response.getHeaders();
        URI location = headers.getLocation();
        assertThat(location.toString(), startsWith(serverPath + "/teams/"));

        assertNull(response.getBody());
    }

    private HttpEntity<String> getStringHttpEntity(Map<String, Object> requestBody) throws JsonProcessingException {
        return new HttpEntity<>(OBJECT_MAPPER.writeValueAsString(requestBody), getHttpHeaders());
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        return requestHeaders;
    }
}
