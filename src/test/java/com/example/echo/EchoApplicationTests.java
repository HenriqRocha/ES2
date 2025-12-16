package com.example.echo;

import com.example.echo.service.EmailService;
import com.example.echo.service.externo.ExternoClient;
import com.example.echo.service.externo.EquipamentoClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@SpringBootTest
@AutoConfigureTestDatabase
class EchoApplicationTests {

	@MockBean
	private ExternoClient externoClient;

	@MockBean
	private EquipamentoClient equipamentoClient;

	@MockBean
	private EmailService emailService;

	@Test
	void contextLoads() {

	}
}