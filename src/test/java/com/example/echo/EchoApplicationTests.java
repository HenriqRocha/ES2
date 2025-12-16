package com.example.echo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@SpringBootTest(properties = "api.externo.url=http://url-teste-mock.com")
@AutoConfigureTestDatabase
class EchoApplicationTests {

	@Test
	void contextLoads() {
		// Agora ele usa H2 em memória e tem a URL externa definida
	}

}