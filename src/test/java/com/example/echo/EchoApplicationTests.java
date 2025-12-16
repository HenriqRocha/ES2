package com.example.echo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(properties = "api.externo.url=http://url-teste-mock.com")
class EchoApplicationTests {

	@Test
	void contextLoads() {

	}

}