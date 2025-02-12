package com.example.democrudproject;

import com.example.democrudproject.model.User;
import com.example.democrudproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.32")
            .withDatabaseName("test_db")
            .withUsername("root")
            .withPassword("root");

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void createUser_Integration() {
        User user = new User("Alice", "Wonderland", "alice@example.com", "123 Wonderland Ave");
        ResponseEntity<User> response = restTemplate.postForEntity("/api/users", user, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        User createdUser = response.getBody();
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertEquals("Alice", createdUser.getName());
        assertEquals("Wonderland", createdUser.getSurname());
        assertEquals("alice@example.com", createdUser.getEmail());
        assertEquals("123 Wonderland Ave", createdUser.getAddress());

        assertEquals(1, userRepository.count());
    }

    @Test
    public void getUserById_Integration() {
        User user = new User("Bob", "Builder", "bob@example.com", "456 Build St");
        user = userRepository.save(user);

        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + user.getId(), User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        User fetchedUser = response.getBody();
        assertThat(fetchedUser).isNotNull();
        assertEquals("Bob", fetchedUser.getName());
        assertEquals("Builder", fetchedUser.getSurname());
        assertEquals("bob@example.com", fetchedUser.getEmail());
        assertEquals("456 Build St", fetchedUser.getAddress());
    }

    @Test
    public void updateUser_Integration() {
        User user = new User("Charlie", "Chocolate", "charlie@example.com", "789 Sweet St");
        user = userRepository.save(user);
        user.setSurname("Choco");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

        ResponseEntity<User> response = restTemplate.exchange(
                "/api/users/" + user.getId(),
                HttpMethod.PUT,
                requestEntity,
                User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        User updatedUser = response.getBody();
        assertThat(updatedUser).isNotNull();
        assertEquals("Choco", updatedUser.getSurname());
        assertEquals("Charlie", updatedUser.getName());
        assertEquals("charlie@example.com", updatedUser.getEmail());
        assertEquals("789 Sweet St", updatedUser.getAddress());
    }

    @Test
    public void deleteUser_Integration() {
        User user = new User("Diana", "Prince", "diana@example.com", "Themyscira");
        user = userRepository.save(user);

        restTemplate.delete("/api/users/" + user.getId());

        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + user.getId(), User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertEquals(0, userRepository.count());
    }

    @Test
    public void searchUsers_Integration() {
        User user1 = new User("Eve", "Online", "eve@example.com", "Space Station");
        User user2 = new User("Eve", "Tester", "evet@example.com", "Lab");
        userRepository.saveAll(Arrays.asList(user1, user2));

        ResponseEntity<User[]> response = restTemplate.getForEntity(
                "/api/users/search?name=Eve&surname=Online",
                User[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] users = response.getBody();
        assertThat(users).isNotNull();
        assertThat(users.length).isGreaterThanOrEqualTo(1);
        for (User u : users) {
            assertEquals("Eve", u.getName());
            assertEquals("Online", u.getSurname());
        }
    }

    @Test
    public void importCsvUsers_Integration() {
        String csvContent = "name,surname,email,address\n"
                + "Frank,Castle,frank@castle.com,New York\n"
                + "Grace,Hopper,grace@navy.com,Arlington";

        ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes()) {
            @Override
            public String getFilename() {
                return "users.csv";
            }
        };

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/users/import", requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String responseBody = response.getBody();
        assertThat(responseBody).contains("Users imported successfully");

        List<User> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
        User frank = allUsers.stream().filter(u -> u.getName().equals("Frank")).findFirst().orElse(null);
        User grace = allUsers.stream().filter(u -> u.getName().equals("Grace")).findFirst().orElse(null);
        assertThat(frank).isNotNull();
        assertThat(grace).isNotNull();
        assertEquals("Castle", frank.getSurname());
        assertEquals("Hopper", grace.getSurname());
    }
}