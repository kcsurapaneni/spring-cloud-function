package dev.kc.scf;

import org.springframework.boot.SpringApplication;

public class TestSpringCloudFunctionApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringCloudFunctionApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
