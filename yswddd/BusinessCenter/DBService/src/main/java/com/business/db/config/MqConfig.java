package com.business.db.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public Queue queue() {
        return new Queue("queue");
    }

    @Bean(name = "message")
    public Queue queueMessage() {
        return new Queue("topic.message");
    }

    @Bean(name = "messages")
    public Queue queueMessages() {
        return new Queue("topic.messages");
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("exchange");
    }

    @Bean
    Binding bindingExchangeMessage(@Qualifier("message") Queue queueMessage, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessage).to(exchange).with("topic.message");
    }

    @Bean
    Binding bindingExchangeMessages(@Qualifier("messages") Queue queueMessages, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessages).to(exchange).with("topic.#");
    }

    @Bean(name = "aMessage")
    public Queue aMessage() {
        return new Queue("fanout.A");
    }

    @Bean(name = "bMessage")
    public Queue bMessage() {
        return new Queue("fanout.B");
    }

    @Bean(name = "cMessage")
    public Queue cMessage() {
        return new Queue("fanout.C");
    }

    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanoutExchange");
    }

    @Bean
    Binding bindingExchangeA(@Qualifier("aMessage") Queue queue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeB(@Qualifier("bMessage") Queue queue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeC(@Qualifier("cMessage") Queue queue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
}

