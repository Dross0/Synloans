package com.synloans.loans.mapper;

public interface Mapper<K, V>{
    K mapTo(V value);

    V mapFrom(K value);
}
