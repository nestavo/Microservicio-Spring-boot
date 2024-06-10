package Email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class EventsEmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventsEmailApplication.class, args);
	}

}
