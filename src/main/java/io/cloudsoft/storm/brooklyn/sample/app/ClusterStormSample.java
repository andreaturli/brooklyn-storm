package io.cloudsoft.storm.brooklyn.sample.app;

import brooklyn.catalog.Catalog;
import brooklyn.catalog.CatalogConfig;
import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.messaging.storm.StormDeployment;
import brooklyn.entity.proxying.EntitySpec;

@Catalog(name="Storm Cluster",
description="Elastic Storm cluster with resizable supervisors tier and zookeeper ensemble",
iconUrl="classpath://storm-logo.jpg")
/** This example starts a storm cluster. */
public class ClusterStormSample extends AbstractApplication {

    @CatalogConfig(label="Number Storm supervisors", priority=1)
    public static final ConfigKey<Integer> STORM_SUPERVISORS = StormDeployment.SUPERVISORS_COUNT;

    @CatalogConfig(label="Number ZooKeeper nodes", priority=2)
    public static final ConfigKey<Integer> STORM_ZOOKEEPERS = StormDeployment.ZOOKEEPERS_COUNT;

    /** Initialize our application. In this case it consists of a Storm deployment */
    @Override
    public void init() {
        addChild(EntitySpec.create(StormDeployment.class).configure(getAllConfig()));
    }

}
