package com.developer.portfolio.repository;

import com.developer.portfolio.domain.Portfolio;
import com.developer.portfolio.repository.rowmapper.PortfolioRowMapper;
import com.developer.portfolio.repository.rowmapper.UserRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Portfolio entity.
 */
@SuppressWarnings("unused")
class PortfolioRepositoryInternalImpl extends SimpleR2dbcRepository<Portfolio, Long> implements PortfolioRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final UserRowMapper userMapper;
    private final PortfolioRowMapper portfolioMapper;

    private static final Table entityTable = Table.aliased("portfolio", EntityManager.ENTITY_ALIAS);
    private static final Table userTable = Table.aliased("jhi_user", "e_user");

    public PortfolioRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        UserRowMapper userMapper,
        PortfolioRowMapper portfolioMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Portfolio.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.userMapper = userMapper;
        this.portfolioMapper = portfolioMapper;
    }

    @Override
    public Flux<Portfolio> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Portfolio> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = PortfolioSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(UserSqlHelper.getColumns(userTable, "user"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(userTable)
            .on(Column.create("user_id", entityTable))
            .equals(Column.create("id", userTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Portfolio.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Portfolio> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Portfolio> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<Portfolio> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Portfolio> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Portfolio> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Portfolio process(Row row, RowMetadata metadata) {
        Portfolio entity = portfolioMapper.apply(row, "e");
        entity.setUser(userMapper.apply(row, "user"));
        return entity;
    }

    @Override
    public <S extends Portfolio> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
