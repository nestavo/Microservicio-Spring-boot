package Email.servicio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//import org.apache.commons.io.IOUtils;

import org.apache.commons.io.IOUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import Email.inter.EmailRepository;
import Email.modelo.Correo;
import jakarta.mail.*;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GmailService {

	// @Autowired
	// private JavaMailSender javaMailSender;
	Properties props = new Properties();

	@Autowired
	private AmqpTemplate rabbitTemplate;

	@Autowired
	private EmailRepository repositorioCorreo2;

	@Scheduled(cron = "*/30 * * * * *")
	public void conectarLeerCorreosAlmacenar() {

		try {
			jakarta.mail.Session session = Session.getInstance(props);
			Store store = session.getStore("imaps");
			store.connect("imap.gmail.com", "correorecibenes@gmail.com", "dsbi riqk bgfy rvpf");
			log.info("Conectado");
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);

			Message[] mensajes = inbox.getMessages();
			log.info("Lectura");
			for (Message mensaje : mensajes) {
				Correo modelo2 = new Correo();
				modelo2.setRemitente(InternetAddress.toString(mensaje.getFrom()));
				modelo2.setDestinatario(InternetAddress.toString(mensaje.getAllRecipients()));
				modelo2.setAsunto(mensaje.getSubject());
				modelo2.setCuerpo(mensaje.getContent().toString());
				modelo2.setFecha(mensaje.getSentDate());

				// Obtener el contenido del mensaje como un String
				String cuerpo = "";
				Object contenido = mensaje.getContent();
				if (contenido instanceof String) {
					// El contenido es un String
					cuerpo = (String) contenido;
				} else if (contenido instanceof MimeMultipart) {
					// El contenido es un MimeMultipart
					MimeMultipart mimeMultipart = (MimeMultipart) contenido;
					for (int i = 0; i < mimeMultipart.getCount(); i++) {
						BodyPart bodyPart = mimeMultipart.getBodyPart(i);
						cuerpo = bodyPart.getContent().toString();
					}
				}

				String asunto = mensaje.getSubject();
				modelo2.setAsunto(asunto);
				modelo2.setCuerpo(cuerpo);

				if (asunto != null && asunto.toLowerCase().contains("importante")) {
					// "importante" aparece en cualquier parte del asunto

					// Enviar el mensaje a RabbitMQ
					// sendToRabbit("Contenido de correo con asunto importante " + cuerpo);
					rabbitTemplate.convertAndSend("${sacavix.queue.name}", cuerpo);
					rabbitTemplate.convertAndSend("${sacavix.queue.name}", asunto);
					log.info("Asunto: '{}')", asunto);
					log.info("Mensaje: '{}')", cuerpo);
				}

				// Verificar si ya existe un correo con el mismo remitente y fecha en la base de
				// datos
				if (!repositorioCorreo2.existsByRemitenteAndFecha(modelo2.getRemitente(), modelo2.getFecha())) {
					// El correo no existe en la base de datos, guardarlo
					// Imprimir
					log.info("Remitente: {}", modelo2.getRemitente());
					log.info("Destinatario: {}", modelo2.getDestinatario());
					log.info("Asunto: {}", modelo2.getAsunto());
					log.info("Cuerpo: {}", modelo2.getCuerpo());
					log.info("Fecha: {}", modelo2.getFecha());
					log.info("--------------   ---------------");

					repositorioCorreo2.save(modelo2);
				} else {
					log.info("El correo ya existe en la base de datos");
				}
			}

			log.info("Termina y guarda en BBDD");

			inbox.close(false);
			store.close();

		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}
	}
}
