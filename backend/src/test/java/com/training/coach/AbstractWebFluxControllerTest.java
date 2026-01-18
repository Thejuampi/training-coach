package com.training.coach;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class AbstractWebFluxControllerTest {

    @Autowired
    protected ApplicationContext context;

    protected WebTestClient webTestClient;

    @BeforeEach
    void setUpWebTestClient() {
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .build();
    }
}