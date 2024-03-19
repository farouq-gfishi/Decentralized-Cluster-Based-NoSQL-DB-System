package com.atypon.nosql.bootstrappingnode.controller;

import com.atypon.nosql.bootstrappingnode.BootstrappingNode;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class BootstrappingNodeRunner implements ApplicationRunner {

    private final BootstrappingNode bootstrappingNode;

    public BootstrappingNodeRunner(BootstrappingNode bootstrappingNode) {
        this.bootstrappingNode = bootstrappingNode;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrappingNode.startCluster();
    }
}
