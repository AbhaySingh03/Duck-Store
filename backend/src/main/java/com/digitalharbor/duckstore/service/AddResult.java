package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.entity.Duck;


public record AddResult(Duck duck, boolean created) {
}
