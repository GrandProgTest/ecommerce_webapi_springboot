package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration;

import graphql.language.StringValue;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class GraphQLScalarConfiguration {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(dateTimeScalar());
    }

    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar for ISO 8601 date-time format in UTC")
                .coercing(new Coercing<Instant, String>() {

                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        if (dataFetcherResult instanceof Instant) {
                            return DateTimeFormatter.ISO_INSTANT.format((Instant) dataFetcherResult);
                        }
                        throw new CoercingSerializeException("Expected an Instant object.");
                    }

                    @Override
                    public Instant parseValue(Object input) throws CoercingParseValueException {
                        if (input instanceof String) {
                            try {
                                return Instant.parse((String) input);
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseValueException("Invalid date format. Expected ISO 8601 format.", e);
                            }
                        }
                        throw new CoercingParseValueException("Expected a String");
                    }

                    @Override
                    public Instant parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof StringValue) {
                            try {
                                return Instant.parse(((StringValue) input).getValue());
                            } catch (DateTimeParseException e) {
                                throw new CoercingParseLiteralException("Invalid date format. Expected ISO 8601 format.");
                            }
                        }
                        throw new CoercingParseLiteralException("Expected a StringValue");
                    }
                })
                .build();
    }
}

