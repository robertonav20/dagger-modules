package com.example.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class HelloWorldApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

    @Bean
    public HttpExchangeRepository httpTraceRepository() {
        return new InMemoryHttpExchangeRepository();
    }

    @Tag(name = "EchoController")
    @RestController
    public static class EchoController {

        public static record EchoResponse(Object protocol, Object method, Object path, Object headers,
                Object queryString, Object body) {
        }

        @RequestMapping(value = "/echo", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<EchoResponse> echo(HttpServletRequest request, @RequestBody Object object)
                throws JsonProcessingException {
            return ResponseEntity.ok()
                    .body(new EchoResponse(request.getProtocol(), request.getMethod(), request.getRequestURI(),
                            Collections.list(request.getHeaderNames()).stream()
                                    .collect(Collectors.toMap(Function.identity(),
                                            h -> Collections.list(request.getHeaders(h)))),
                            request.getQueryString(), object));
        }
    }

    @Tag(name = "HelloWorldController")
    @RestController
    public static class HelloWorldController {

        @RequestMapping("/hello")
        public String home() {
            return "Hello World!";
        }
    }
}
