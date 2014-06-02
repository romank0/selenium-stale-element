package net.selemium.stalesafe;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;

@Controller
@EnableAutoConfiguration
public class TestSite {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TestSite.class, args);
    }
}
