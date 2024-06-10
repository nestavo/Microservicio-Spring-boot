package Email.inter;

import java.util.Date;

//import org.eclipse.angus.mail.imap.protocol.ID;
import org.springframework.data.jpa.repository.JpaRepository;




import Email.modelo.Correo;


public interface EmailRepository extends JpaRepository<Correo, Long> {
	
	boolean existsByRemitenteAndFecha(String remitente, Date fecha);
}


