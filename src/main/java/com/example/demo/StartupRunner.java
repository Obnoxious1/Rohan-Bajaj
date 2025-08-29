package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("✅ App started! Now calling generateWebhook API...");

        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        // STEP 1: Send your real details
        Map<String, String> request = new HashMap<>();
        request.put("name", "Rohan Krishna Das");
        request.put("regNo", "22BCE1642");   // your actual regNo
        request.put("email", "rkdcoc341@gmail.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            System.out.println("Response from generateWebhook: " + response.getBody());

            // Extract values
            String webhookUrl = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            System.out.println("📌 Webhook URL: " + webhookUrl);
            System.out.println("📌 Access Token: " + accessToken);

            // STEP 2: SQL solution (Question 2, since regNo ends in even digit)
            String finalQuery =
                "SELECT e.EMP_ID, " +
                "       e.FIRST_NAME, " +
                "       e.LAST_NAME, " +
                "       d.DEPARTMENT_NAME, " +
                "       COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 " +
                "     ON e.DEPARTMENT = e2.DEPARTMENT " +
                "    AND e2.DOB > e.DOB " +
                "GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e.EMP_ID DESC;";

            // STEP 3: Submit SQL to webhook
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            // submitHeaders.setBearerAuth(accessToken);
            submitHeaders.set("Authorization", accessToken);


            Map<String, String> submitBody = new HashMap<>();
            submitBody.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(submitBody, submitHeaders);

            System.out.println("📤 Sending finalQuery: " + submitBody);

            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, submitEntity, String.class);

            System.out.println("✅ Submission Response: " + submitResponse.getBody());

        } catch (Exception e) {
            System.err.println("❌ API call failed: " + e.getMessage());
        }
    }
}
