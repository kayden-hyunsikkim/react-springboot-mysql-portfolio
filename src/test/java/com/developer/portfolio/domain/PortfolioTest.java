package com.developer.portfolio.domain;

import static com.developer.portfolio.domain.PortfolioTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.developer.portfolio.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PortfolioTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Portfolio.class);
        Portfolio portfolio1 = getPortfolioSample1();
        Portfolio portfolio2 = new Portfolio();
        assertThat(portfolio1).isNotEqualTo(portfolio2);

        portfolio2.setId(portfolio1.getId());
        assertThat(portfolio1).isEqualTo(portfolio2);

        portfolio2 = getPortfolioSample2();
        assertThat(portfolio1).isNotEqualTo(portfolio2);
    }
}
