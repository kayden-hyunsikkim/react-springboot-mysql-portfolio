package com.developer.portfolio.repository;

import com.developer.portfolio.domain.Portfolio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Portfolio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PortfolioRepository extends ReactiveCrudRepository<Portfolio, Long>, PortfolioRepositoryInternal {
    Flux<Portfolio> findAllBy(Pageable pageable);

    @Override
    Mono<Portfolio> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Portfolio> findAllWithEagerRelationships();

    @Override
    Flux<Portfolio> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM portfolio entity WHERE entity.user_id = :id")
    Flux<Portfolio> findByUser(Long id);

    @Query("SELECT * FROM portfolio entity WHERE entity.user_id IS NULL")
    Flux<Portfolio> findAllWhereUserIsNull();

    @Override
    <S extends Portfolio> Mono<S> save(S entity);

    @Override
    Flux<Portfolio> findAll();

    @Override
    Mono<Portfolio> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PortfolioRepositoryInternal {
    <S extends Portfolio> Mono<S> save(S entity);

    Flux<Portfolio> findAllBy(Pageable pageable);

    Flux<Portfolio> findAll();

    Mono<Portfolio> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Portfolio> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Portfolio> findOneWithEagerRelationships(Long id);

    Flux<Portfolio> findAllWithEagerRelationships();

    Flux<Portfolio> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
