package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class ColdAndHotPublisherTest {
    @Test
    @DisplayName("Cold Publisher emits values from beginning")
    void coldPublisherTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Integer> flux = Flux.range(1, 6)
                .delayElements(Duration.ofSeconds(1));
        flux.map(i -> "Subscriter 1: " + i).subscribe(System.out::println, null, latch::countDown); //emits values from beginning

        Thread.sleep(2000);

        flux.map(i -> "Subscriter 2: " + i).subscribe(System.out::println);//emits values from beginning

        latch.await();
    }

    @Test
    @DisplayName("Hot Publisher does not emit values from beginning")
    void hotPublisherTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Integer> flux = Flux.range(1, 6)
                .delayElements(Duration.ofSeconds(1));

        ConnectableFlux<Integer> connectableFlux = flux.publish();
        connectableFlux.connect();

        connectableFlux.map(i -> "Subscriter 1: " + i).subscribe(System.out::println, null, latch::countDown);

        Thread.sleep(2000);

        connectableFlux.map(i -> "Subscriter 2: " + i).subscribe(System.out::println);//does NOT emit values from beginning

        latch.await();
    }
}
