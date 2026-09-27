package com.tecmilenio.mapsconect.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Configuración del cluster y mensajería distribuida.
 *
 * <p>Selecciona automáticamente el bus de eventos adecuado:</p>
 * <ul>
 *   <li>Si {@code app.redis.enabled: true}: activa Redis Pub/Sub y el contenedor de escucha de mensajes.</li>
 *   <li>Si {@code app.redis.enabled: false} (default): usa el bus local en memoria sin dependencias externas.</li>
 * </ul>
 */
@Configuration
public class RedisClusterConfig {

    @Bean
    @Primary
    public MensajeriaDistribuidaService mensajeriaPrincipal(
            @Value("${app.redis.enabled:false}") boolean redisHabilitado,
            LocalMensajeriaService localService,
            org.springframework.beans.factory.ObjectProvider<RedisMensajeriaService> redisProvider) {

        if (redisHabilitado) {
            RedisMensajeriaService redisService = redisProvider.getIfAvailable();
            if (redisService != null) {
                return redisService;
            }
        }
        return localService;
    }

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMensajeriaService redisMensajeriaService) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisMensajeriaService, new ChannelTopic(RedisMensajeriaService.CANAL_CLUSTER));
        return container;
    }

}
