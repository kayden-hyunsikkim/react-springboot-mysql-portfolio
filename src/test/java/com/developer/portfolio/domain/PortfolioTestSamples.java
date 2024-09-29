package com.developer.portfolio.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PortfolioTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Portfolio getPortfolioSample1() {
        return new Portfolio().id(1L).projectName("projectName1").description("description1").imageUrl("imageUrl1").link("link1");
    }

    public static Portfolio getPortfolioSample2() {
        return new Portfolio().id(2L).projectName("projectName2").description("description2").imageUrl("imageUrl2").link("link2");
    }

    public static Portfolio getPortfolioRandomSampleGenerator() {
        return new Portfolio()
            .id(longCount.incrementAndGet())
            .projectName(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .imageUrl(UUID.randomUUID().toString())
            .link(UUID.randomUUID().toString());
    }
}
