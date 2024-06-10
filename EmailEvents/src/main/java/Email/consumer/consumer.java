package Email.consumer;



import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class consumer {
	
	@RabbitListener(queues = { "${sacavix.queue.name}"})
	public void recive(@Payload String message) {
 
		log.info("Mensaje recibido: {} ", message);
		
	
		makeSlow();
}

	private void makeSlow() {
		
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
