package com.developer.portfolio.repository.rowmapper;

import com.developer.portfolio.domain.Portfolio;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Portfolio}, with proper type conversions.
 */
@Service
public class PortfolioRowMapper implements BiFunction<Row, String, Portfolio> {

    private final ColumnConverter converter;

    public PortfolioRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Portfolio} stored in the database.
     */
    @Override
    public Portfolio apply(Row row, String prefix) {
        Portfolio entity = new Portfolio();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setProjectName(converter.fromRow(row, prefix + "_project_name", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setImageUrl(converter.fromRow(row, prefix + "_image_url", String.class));
        entity.setLink(converter.fromRow(row, prefix + "_link", String.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", String.class));
        return entity;
    }
}
