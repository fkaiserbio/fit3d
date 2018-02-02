package bio.fkaiser.fit3d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan("bio.fkaiser.fit3d")
public class Fit3DApp {

    public static void main(String[] args) {
        SpringApplication.run(Fit3DApp.class, args);
    }
}

