package snghk.mineus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ItsMineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItsMineApplication.class, args);
    }

}
