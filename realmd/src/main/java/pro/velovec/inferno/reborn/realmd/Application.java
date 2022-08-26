package pro.velovec.inferno.reborn.realmd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import pro.velovec.inferno.reborn.common.server.Listener;
import pro.velovec.inferno.reborn.common.xor.XORCodec;
import pro.velovec.inferno.reborn.realmd.properties.RealmServerProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
    RealmServerProperties.class
})
@EnableJpaRepositories(basePackages = {
    "pro.velovec.inferno.reborn.realmd"
})
@EntityScan(basePackages = {
    "pro.velovec.inferno.reborn.realmd"
})
public class Application {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        ctx.registerShutdownHook();

        Listener listener = ctx.getBean(Listener.class);
        RealmServerProperties properties = ctx.getBean(RealmServerProperties.class);

        listener.setListenerConfig(properties.getListen());

        listener.addHandler(XORCodec.class);
        listener.addHandler(new RealmHandler(ctx));

        listener.start();
    }

}
