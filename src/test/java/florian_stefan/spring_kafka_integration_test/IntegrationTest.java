package florian_stefan.spring_kafka_integration_test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Target(TYPE)
@Retention(RUNTIME)
@ActiveProfiles("test")
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@TestPropertySource(locations = "classpath:docker-ports.properties")
public @interface IntegrationTest {

}
