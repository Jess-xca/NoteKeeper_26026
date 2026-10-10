package com.notekeeper.notekeeper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:notekeeper;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"jwt.secret=test-secret-key-that-is-long-enough-for-hs256",
		"google.client.id=test-client",
		"google.client.secret=test-secret",
		"spring.mail.username=test@example.com",
		"spring.mail.password=test"
})
class NotekeeperApplicationTests {

	@Test
	void contextLoads() {
	}
}
