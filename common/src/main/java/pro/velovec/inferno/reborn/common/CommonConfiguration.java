package pro.velovec.inferno.reborn.common;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import pro.velovec.inferno.reborn.common.properties.InfernoCommonProperties;

@Configuration
@EnableConfigurationProperties({InfernoCommonProperties.class})
@EnableJpaRepositories(basePackages = {"pro.velovec.inferno.reborn.common"})
@EntityScan(basePackages = {"pro.velovec.inferno.reborn.common"})
@ComponentScan({"pro.velovec.inferno.reborn.common"})
public class CommonConfiguration {

}
