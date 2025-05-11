package com.example.demo;

import org.crac.CheckpointException;
import org.crac.Core;
import org.crac.RestoreException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * Этот бин активируется только при профиле "snapshot":
     * после ContextRefreshedEvent делает чек-пойнт и завершает JVM.
     */
    @Bean
    @Profile("snapshot")
    public ApplicationListener<ContextRefreshedEvent> checkpointAndExit() {
        return event -> {
            try {
                // вызывает CRaC: сохраняет снимок кучи, JIT-кэша и т.д.
                Core.checkpointRestore();
            } catch (CheckpointException | RestoreException e) {
                throw new IllegalStateException("Не удалось создать CRaC-снапшот", e);
            }
            // после checkpoint-а завершаем приложение
            SpringApplication.exit(event.getApplicationContext(), () -> 0);
        };
    }
}