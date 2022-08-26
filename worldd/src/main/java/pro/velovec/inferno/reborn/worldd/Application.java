package pro.velovec.inferno.reborn.worldd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import pro.velovec.inferno.reborn.common.server.Listener;
import pro.velovec.inferno.reborn.common.xor.XORCodec;
import pro.velovec.inferno.reborn.worldd.properties.WorldServerProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    WorldServerProperties.class
})
@EnableJpaRepositories(basePackages = {
    "pro.velovec.inferno.reborn.worldd"
})
@EntityScan(basePackages = {
    "pro.velovec.inferno.reborn.worldd"
})
public class Application {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        ctx.registerShutdownHook();

        Listener listener = ctx.getBean(Listener.class);
        WorldServerProperties properties = ctx.getBean(WorldServerProperties.class);

        listener.setListenerConfig(properties.getListen());

        listener.addHandler(XORCodec.class);
        listener.addHandler(new WorldHandler(ctx));

        listener.start();
    }
}
