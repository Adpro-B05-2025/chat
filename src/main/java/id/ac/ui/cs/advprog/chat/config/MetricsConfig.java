package id.ac.ui.cs.advprog.chat.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    private final MeterRegistry registry;

    public MetricsConfig(MeterRegistry registry) {
        this.registry = registry;
    }

    @Bean
    public Counter chatMessageSentCounter() {
        return Counter.builder("chat.messages.sent")
                .description("Total number of chat messages sent")
                .register(registry);
    }
    
    @Bean
    public Counter chatMessageEditedCounter() {
        return Counter.builder("chat.messages.edited")
                .description("Total number of chat messages edited")
                .register(registry);
    }
    
    @Bean
    public Counter chatMessageDeletedCounter() {
        return Counter.builder("chat.messages.deleted")
                .description("Total number of chat messages deleted")
                .register(registry);
    }
    
    @Bean
    public Counter chatRoomCreatedCounter() {
        return Counter.builder("chat.rooms.created")
                .description("Total number of chat rooms created")
                .register(registry);
    }
    
    @Bean
    public Timer messageProcessingTimer() {
        return Timer.builder("chat.message.processing.time")
                .description("Time taken to process and deliver messages")
                .register(registry);
    }
}