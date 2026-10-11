package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.entity.Duck;

/** Outcome of an add: the stored duck, and whether a new record was created (false = quantities were merged). */
public record AddResult(Duck duck, boolean created) {
}
