package zzz.service.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication()
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"zzz.service.forum"})
public class Demo404ForumApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(Demo404ForumApplication.class, args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String dbUrl = env.getProperty("spring.datasource.url");
        System.out.println(
                "\n\t" +
                        "----------------------------------------------------------\n\t" +
                        "Application Sailrui-Boot is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:" + port + "/doc.html\n\t" +
                        "External: \thttp://" + ip + ":" + port + "/doc.html\n\t" +
                        "Web: \t\thttp://" + ip + ":" + port + "\n\t" +
                        "Nacos: \t\thttp://dev.inksyun.com:31080/cost/doc.html\n\t" +
                        "Database URL: \t" + dbUrl + "\n\t" +
                        "------------------------------------------------------------");
    }
}