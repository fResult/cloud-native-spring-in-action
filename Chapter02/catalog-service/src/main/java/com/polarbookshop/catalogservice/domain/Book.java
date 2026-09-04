package com.polarbookshop.catalogservice.domain;

import java.math.BigDecimal;
import lombok.With;

@With
public record Book(String isbn, String title, String author, BigDecimal price) {}
