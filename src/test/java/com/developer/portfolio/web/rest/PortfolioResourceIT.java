package com.developer.portfolio.web.rest;

import static com.developer.portfolio.domain.PortfolioAsserts.*;
import static com.developer.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.developer.portfolio.IntegrationTest;
import com.developer.portfolio.domain.Portfolio;
import com.developer.portfolio.domain.User;
import com.developer.portfolio.repository.EntityManager;
import com.developer.portfolio.repository.PortfolioRepository;
import com.developer.portfolio.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link PortfolioResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PortfolioResourceIT {

    private static final String DEFAULT_PROJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_PROJECT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_IMAGE_URL = "http://Wnul.jpeg";
    private static final String UPDATED_IMAGE_URL = "https://.jpg";

    private static final String DEFAULT_LINK = "http://q${";
    private static final String UPDATED_LINK = "https://i";

    private static final String ENTITY_API_URL = "/api/portfolios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private PortfolioRepository portfolioRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Portfolio portfolio;

    private Portfolio insertedPortfolio;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Portfolio createEntity(EntityManager em) {
        Portfolio portfolio = new Portfolio()
            .projectName(DEFAULT_PROJECT_NAME)
            .description(DEFAULT_DESCRIPTION)
            .imageUrl(DEFAULT_IMAGE_URL)
            .link(DEFAULT_LINK);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        portfolio.setUser(user);
        return portfolio;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Portfolio createUpdatedEntity(EntityManager em) {
        Portfolio updatedPortfolio = new Portfolio()
            .projectName(UPDATED_PROJECT_NAME)
            .description(UPDATED_DESCRIPTION)
            .imageUrl(UPDATED_IMAGE_URL)
            .link(UPDATED_LINK);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        updatedPortfolio.setUser(user);
        return updatedPortfolio;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Portfolio.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        UserResourceIT.deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        portfolio = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedPortfolio != null) {
            portfolioRepository.delete(insertedPortfolio).block();
            insertedPortfolio = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createPortfolio() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Portfolio
        var returnedPortfolio = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Portfolio.class)
            .returnResult()
            .getResponseBody();

        // Validate the Portfolio in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPortfolioUpdatableFieldsEquals(returnedPortfolio, getPersistedPortfolio(returnedPortfolio));

        insertedPortfolio = returnedPortfolio;
    }

    @Test
    void createPortfolioWithExistingId() throws Exception {
        // Create the Portfolio with an existing ID
        portfolio.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkProjectNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        portfolio.setProjectName(null);

        // Create the Portfolio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        portfolio.setDescription(null);

        // Create the Portfolio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkImageUrlIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        portfolio.setImageUrl(null);

        // Create the Portfolio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLinkIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        portfolio.setLink(null);

        // Create the Portfolio, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllPortfolios() {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        // Get all the portfolioList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(portfolio.getId().intValue()))
            .jsonPath("$.[*].projectName")
            .value(hasItem(DEFAULT_PROJECT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].imageUrl")
            .value(hasItem(DEFAULT_IMAGE_URL))
            .jsonPath("$.[*].link")
            .value(hasItem(DEFAULT_LINK));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPortfoliosWithEagerRelationshipsIsEnabled() {
        when(portfolioRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(portfolioRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPortfoliosWithEagerRelationshipsIsNotEnabled() {
        when(portfolioRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(portfolioRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getPortfolio() {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        // Get the portfolio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, portfolio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(portfolio.getId().intValue()))
            .jsonPath("$.projectName")
            .value(is(DEFAULT_PROJECT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.imageUrl")
            .value(is(DEFAULT_IMAGE_URL))
            .jsonPath("$.link")
            .value(is(DEFAULT_LINK));
    }

    @Test
    void getNonExistingPortfolio() {
        // Get the portfolio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingPortfolio() throws Exception {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the portfolio
        Portfolio updatedPortfolio = portfolioRepository.findById(portfolio.getId()).block();
        updatedPortfolio.projectName(UPDATED_PROJECT_NAME).description(UPDATED_DESCRIPTION).imageUrl(UPDATED_IMAGE_URL).link(UPDATED_LINK);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPortfolio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedPortfolio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPortfolioToMatchAllProperties(updatedPortfolio);
    }

    @Test
    void putNonExistingPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, portfolio.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePortfolioWithPatch() throws Exception {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the portfolio using partial update
        Portfolio partialUpdatedPortfolio = new Portfolio();
        partialUpdatedPortfolio.setId(portfolio.getId());

        partialUpdatedPortfolio.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPortfolio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPortfolio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Portfolio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPortfolioUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPortfolio, portfolio),
            getPersistedPortfolio(portfolio)
        );
    }

    @Test
    void fullUpdatePortfolioWithPatch() throws Exception {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the portfolio using partial update
        Portfolio partialUpdatedPortfolio = new Portfolio();
        partialUpdatedPortfolio.setId(portfolio.getId());

        partialUpdatedPortfolio
            .projectName(UPDATED_PROJECT_NAME)
            .description(UPDATED_DESCRIPTION)
            .imageUrl(UPDATED_IMAGE_URL)
            .link(UPDATED_LINK);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPortfolio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPortfolio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Portfolio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPortfolioUpdatableFieldsEquals(partialUpdatedPortfolio, getPersistedPortfolio(partialUpdatedPortfolio));
    }

    @Test
    void patchNonExistingPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, portfolio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPortfolio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        portfolio.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(portfolio))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Portfolio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePortfolio() {
        // Initialize the database
        insertedPortfolio = portfolioRepository.save(portfolio).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the portfolio
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, portfolio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return portfolioRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Portfolio getPersistedPortfolio(Portfolio portfolio) {
        return portfolioRepository.findById(portfolio.getId()).block();
    }

    protected void assertPersistedPortfolioToMatchAllProperties(Portfolio expectedPortfolio) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPortfolioAllPropertiesEquals(expectedPortfolio, getPersistedPortfolio(expectedPortfolio));
        assertPortfolioUpdatableFieldsEquals(expectedPortfolio, getPersistedPortfolio(expectedPortfolio));
    }

    protected void assertPersistedPortfolioToMatchUpdatableProperties(Portfolio expectedPortfolio) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPortfolioAllUpdatablePropertiesEquals(expectedPortfolio, getPersistedPortfolio(expectedPortfolio));
        assertPortfolioUpdatableFieldsEquals(expectedPortfolio, getPersistedPortfolio(expectedPortfolio));
    }
}
