package com.kvitka.spring_api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        List<Integer> integers = Stream.generate(() -> 1).limit(10).collect(Collectors.toList());
        integers.add(100);
        System.out.println(integers);

    }
}
