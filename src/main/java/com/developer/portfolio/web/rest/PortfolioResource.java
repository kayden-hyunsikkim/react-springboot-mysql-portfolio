package com.developer.portfolio.web.rest;

import com.developer.portfolio.domain.Portfolio;
import com.developer.portfolio.repository.PortfolioRepository;
import com.developer.portfolio.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.developer.portfolio.domain.Portfolio}.
 */
@RestController
@RequestMapping("/api/portfolios")
@Transactional
public class PortfolioResource {

    private static final Logger LOG = LoggerFactory.getLogger(PortfolioResource.class);

    private static final String ENTITY_NAME = "portfolio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PortfolioRepository portfolioRepository;

    public PortfolioResource(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * {@code POST  /portfolios} : Create a new portfolio.
     *
     * @param portfolio the portfolio to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new portfolio, or with status {@code 400 (Bad Request)} if the portfolio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<Portfolio>> createPortfolio(@Valid @RequestBody Portfolio portfolio) throws URISyntaxException {
        LOG.debug("REST request to save Portfolio : {}", portfolio);
        if (portfolio.getId() != null) {
            throw new BadRequestAlertException("A new portfolio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return portfolioRepository
            .save(portfolio)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/portfolios/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /portfolios/:id} : Updates an existing portfolio.
     *
     * @param id the id of the portfolio to save.
     * @param portfolio the portfolio to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated portfolio,
     * or with status {@code 400 (Bad Request)} if the portfolio is not valid,
     * or with status {@code 500 (Internal Server Error)} if the portfolio couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Portfolio>> updatePortfolio(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Portfolio portfolio
    ) throws URISyntaxException {
        LOG.debug("REST request to update Portfolio : {}, {}", id, portfolio);
        if (portfolio.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, portfolio.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return portfolioRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return portfolioRepository
                    .save(portfolio)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /portfolios/:id} : Partial updates given fields of an existing portfolio, field will ignore if it is null
     *
     * @param id the id of the portfolio to save.
     * @param portfolio the portfolio to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated portfolio,
     * or with status {@code 400 (Bad Request)} if the portfolio is not valid,
     * or with status {@code 404 (Not Found)} if the portfolio is not found,
     * or with status {@code 500 (Internal Server Error)} if the portfolio couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Portfolio>> partialUpdatePortfolio(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Portfolio portfolio
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Portfolio partially : {}, {}", id, portfolio);
        if (portfolio.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, portfolio.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return portfolioRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Portfolio> result = portfolioRepository
                    .findById(portfolio.getId())
                    .map(existingPortfolio -> {
                        if (portfolio.getProjectName() != null) {
                            existingPortfolio.setProjectName(portfolio.getProjectName());
                        }
                        if (portfolio.getDescription() != null) {
                            existingPortfolio.setDescription(portfolio.getDescription());
                        }
                        if (portfolio.getImageUrl() != null) {
                            existingPortfolio.setImageUrl(portfolio.getImageUrl());
                        }
                        if (portfolio.getLink() != null) {
                            existingPortfolio.setLink(portfolio.getLink());
                        }

                        return existingPortfolio;
                    })
                    .flatMap(portfolioRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /portfolios} : get all the portfolios.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of portfolios in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<Portfolio>>> getAllPortfolios(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Portfolios");
        return portfolioRepository
            .count()
            .zipWith(portfolioRepository.findAllBy(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /portfolios/:id} : get the "id" portfolio.
     *
     * @param id the id of the portfolio to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the portfolio, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Portfolio>> getPortfolio(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Portfolio : {}", id);
        Mono<Portfolio> portfolio = portfolioRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(portfolio);
    }

    /**
     * {@code DELETE  /portfolios/:id} : delete the "id" portfolio.
     *
     * @param id the id of the portfolio to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePortfolio(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Portfolio : {}", id);
        return portfolioRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
