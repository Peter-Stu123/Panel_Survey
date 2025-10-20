package org.isoft;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = "org.isoft.panelsurvey")
@MapperScan("org.isoft.panelsurvey.mapper")
@Slf4j
public class PenalSurveyApplication {

    public static void main(String[] args) {
        // 配置TLS协议版本，解决问卷网HTTPS连接SSL握手问题
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");

        // 如果还有SSL问题，取消下面的注释启用详细调试（仅开发环境）
        // System.setProperty("javax.net.debug", "ssl,handshake");

        System.out.println("=== 已配置TLS协议支持: TLSv1.2, TLSv1.3 ===");

        SpringApplication app = new SpringApplication(PenalSurveyApplication.class);
        Environment env = app.run(args).getEnvironment();
        app.setBannerMode(Banner.Mode.CONSOLE);
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path");
        String docPath;
        if (!StringUtils.hasText(contextPath)) {
            docPath = "/doc.html";
        } else {
            docPath = contextPath + "/doc.html";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info("""
                        ----------------------------------------------------------
                        \t应用程序"{}"正在运行中......
                        \t接口文档访问 URL:
                        \t本地: \t\t{}://localhost:{}{}
                        \t外部: \t{}://{}:{}{}
                        \tSwagger UI: \t{}://localhost:{}/swagger-ui.html
                        \t配置文件: \t{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name", "Panel Survey"),
                protocol,
                serverPort,
                docPath,
                protocol,
                hostAddress,
                serverPort,
                docPath,
                protocol,
                serverPort,
                env.getActiveProfiles().length > 0 ? 
                    String.join(", ", env.getActiveProfiles()) : "default");
    }
}