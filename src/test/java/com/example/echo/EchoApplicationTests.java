package com.example.echo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@SpringBootTest(properties = {

		"api.externo.url=http://url-teste-mock.com",


		"spring.mail.host=localhost",
		"spring.mail.port=1025",
		"spring.mail.username=teste",
		"spring.mail.password=teste",
		"spring.mail.properties.mail.smtp.auth=false",
		"spring.mail.properties.mail.smtp.starttls.enable=false",

		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase
class EchoApplicationTests {

	@Test
	void contextLoads() {

	}

}