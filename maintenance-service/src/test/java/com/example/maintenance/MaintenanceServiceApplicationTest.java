package com.example.maintenance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest
class MaintenanceServiceApplicationTest {

    @MockitoBean
    private RestClient restClient;

    @Test
    void contextLoads() {
    }
}
