package at.ac.tuwien.dsg.comot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author omoser
 */
public class ComotClient {

    private static Logger log = LoggerFactory.getLogger(ComotClient.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
         ctx.register(ComotContext.class);
         ctx.refresh();
    }
}
