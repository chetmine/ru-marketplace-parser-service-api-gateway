package chetmine.marketplace.parser.config;

import chetmine.marketplace.parser.model.ProofTokenData;
import chetmine.marketplace.parser.model.RiskTokenData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = "emailCodeRiskToken")
    public RedisTemplate<String, RiskTokenData> emailCodeRiskTokenTemplate(RedisConnectionFactory redisConnectionFactory) {
        return buildTemplate(redisConnectionFactory, RiskTokenData.class);
    }

    @Bean
    public RedisTemplate<String, Integer> emailCodeResendRateLimiterTemplate(RedisConnectionFactory redisConnectionFactory) {
        return buildTemplate(redisConnectionFactory, Integer.class);
    }

    @Bean
    public RedisTemplate<String, ProofTokenData> proofTokenTemplate(RedisConnectionFactory redisConnectionFactory) {
        return buildTemplate(redisConnectionFactory, ProofTokenData.class);
    }

    private <T> RedisTemplate<String, T> buildTemplate(RedisConnectionFactory redisConnectionFactory, Class<T> clazz) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        JacksonJsonRedisSerializer<T> serializer = new JacksonJsonRedisSerializer<>(clazz);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
