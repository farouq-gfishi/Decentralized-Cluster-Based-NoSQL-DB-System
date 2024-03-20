package com.atypon.nosql.bootstrappingnode;

import com.atypon.nosql.bootstrappingnode.service.BootstrappingNodeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class BootstrappingNodeRunner implements ApplicationRunner {

    private final BootstrappingNodeService bootstrappingNode;

    public BootstrappingNodeRunner(BootstrappingNodeService bootstrappingNode) {
        this.bootstrappingNode = bootstrappingNode;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrappingNode.startCluster();
    }
}
