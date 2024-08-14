package cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cms", "fileserver","fileresizer"})
public class RhApplication {

	public static void main(String[] args) {
		SpringApplication.run(RhApplication.class, args);
	}

}
