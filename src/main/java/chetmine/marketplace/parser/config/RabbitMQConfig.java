package chetmine.marketplace.parser.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TASKS_EXCHANGE = "tasks";
    public static final String PARSER_EXCHANGE = "marketplace.parser";

    public static final String ROUTING_PREVIEW = "tasks.preview";
    public static final String ROUTING_DETAILED = "tasks.detailed";

    public static final String QUEUE_PREVIEW = "marketplace.parser.preview";
    public static final String QUEUE_DETAILED = "marketplace.parser.detailed";

//    @Bean
//    public DirectExchange tasksExchange() {
//        return new DirectExchange(TASKS_EXCHANGE, true, false);
//    }

//    @Bean
//    public TopicExchange parserExchange() {
//        return new TopicExchange(PARSER_EXCHANGE, true, false);
//    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
