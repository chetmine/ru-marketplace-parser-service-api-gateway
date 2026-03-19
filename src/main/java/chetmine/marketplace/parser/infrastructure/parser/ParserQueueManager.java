package chetmine.marketplace.parser.infrastructure.parser;

import chetmine.marketplace.parser.config.RabbitMQConfig;
import chetmine.marketplace.parser.dto.parser.ParseTask;
import chetmine.marketplace.parser.dto.parser.TaskParams;
import chetmine.marketplace.parser.infrastructure.ws.WsSessionRegistry;
import chetmine.marketplace.parser.model.WsSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParserQueueManager {
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ParserResultDispatcher resultDispatcher;
    private final WsSessionRegistry wsSessionRegistry;

    private final Map<String, SimpleMessageListenerContainer> activeListeners = new ConcurrentHashMap<>();

    public void startParsingSession(String sessionId, String taskType, String query, TaskParams params) {
        String queueName = resolveQueueName(taskType);

        Queue queue = new Queue(queueName, true, false, false);
        rabbitAdmin.declareQueue(queue);

        Binding binding = BindingBuilder
                .bind(queue)
                .to(new org.springframework.amqp.core.TopicExchange(RabbitMQConfig.PARSER_EXCHANGE))
                .with(sessionId);
        rabbitAdmin.declareBinding(binding);
        log.info("Bound queue={} with routingKey={}", queueName, sessionId);

        SimpleMessageListenerContainer container = buildListenerContainer(sessionId, taskType, queueName);
        activeListeners.put(sessionId, container);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.start();


//        TaskParams params = TaskParams.builder()
//                .retryOnParserExposed(true)
//                .marketplace("ozon")
//                .build();

        params.setRetryOnParserExposed(true);

        ParseTask task = ParseTask.builder()
                .sessionId(sessionId)
                .type(taskType)
                .query(query)
                .params(params)
                .build();

        rabbitTemplate.convertAndSend(RabbitMQConfig.TASKS_EXCHANGE, resolveTaskRoutingKey(taskType), task);
        log.info("Published {} task for sessionId={}, query={}", taskType, sessionId, query);
    }

    public void stopParsingSession(String sessionId, String taskType) {
        String queueName = resolveQueueName(taskType);

        SimpleMessageListenerContainer container = activeListeners.remove(sessionId);
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("Stopped listener for sessionId={}", sessionId);
        }

        Binding binding = BindingBuilder
                .bind(new Queue(queueName))
                .to(new org.springframework.amqp.core.TopicExchange(RabbitMQConfig.PARSER_EXCHANGE))
                .with(sessionId);
        rabbitAdmin.removeBinding(binding);
        log.info("Unbound queue={} with routingKey={}", queueName, sessionId);


    }

    public void stopWebSocketSession(String sessionId) throws IOException {
        WsSession session = wsSessionRegistry.get(sessionId).get();
        //session.getSocket().close(CloseStatus.NORMAL);
        wsSessionRegistry.remove(sessionId);
        session.getSocket().close(CloseStatus.NORMAL);
    }

    private SimpleMessageListenerContainer buildListenerContainer(String sessionId,
                                                                  String taskType,
                                                                  String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                rabbitTemplate.getConnectionFactory()
        );
        container.setQueueNames(queueName);
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            try {
                boolean isDone = resultDispatcher.dispatch(sessionId, taskType, message);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                // Stops parsing session when dispatch() method returns true (Parsing finished signal)
                if (isDone) {
                    stopSession(sessionId, taskType);
                    wsSessionRegistry.remove(sessionId);
                }
            } catch (Exception e) {
                log.error("Error dispatching parser result for sessionId={}", sessionId, e);
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
        });
        return container;
    }

    private void stopSession(String sessionId, String taskType) {
        try {
            stopWebSocketSession(sessionId);
            stopParsingSession(sessionId, taskType);
        } catch (Exception e) {
            log.error("Error stopping parsing session for sessionId={}", sessionId, e);
        }
    }

    private String resolveQueueName(String taskType) {
        return "preview".equals(taskType)
                ? RabbitMQConfig.QUEUE_PREVIEW
                : RabbitMQConfig.QUEUE_DETAILED;
    }

    private String resolveTaskRoutingKey(String taskType) {
        return "preview".equals(taskType)
                ? RabbitMQConfig.ROUTING_PREVIEW
                : RabbitMQConfig.ROUTING_DETAILED;
    }
}
