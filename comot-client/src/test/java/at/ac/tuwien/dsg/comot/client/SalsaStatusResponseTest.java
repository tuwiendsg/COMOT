package at.ac.tuwien.dsg.comot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author omoser
 */
public class SalsaStatusResponseTest {

    @Test
    public void marshallSalsaStatusResponse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SalsaServiceStatusResponse response = mapper.reader(SalsaServiceStatusResponse.class)
                .readValue(new ClassPathResource("salsa-status-response.json").getInputStream());


    }
}
