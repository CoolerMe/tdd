package com.coolme.di;

import java.lang.annotation.Annotation;

public record Component(Class<?> type, Annotation qualifier) {

}
