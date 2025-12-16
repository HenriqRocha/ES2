package com.example.echo;

import com.example.echo.service.EmailService;
import com.example.echo.service.externo.ExternoClient;
import com.example.echo.service.externo.EquipamentoClient;
import org.junit.jupiter.api.DisplayName;
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

	@Test
	@DisplayName("Teste de sanidade da classe Main")
	void main() {
		// Isso garante que a classe principal e suas configurações iniciam sem erro
		// E conta pontos preciosos de cobertura na classe @SpringBootApplication
		EchoApplication.main(new String[] {});
	}
}