/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.catalog.content.monitor;

import static org.awaitility.Awaitility.await;
import static org.codice.ddf.catalog.content.monitor.configurators.KarafConfigurator.karafConfiguration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.codice.ddf.catalog.content.monitor.util.BundleInfo;
import org.junit.After;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class AbstractComponentTest {
    protected static final int TIMEOUT_IN_SECONDS = 10;

    @Inject
    private BundleContext bundleContext;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    @Configuration
    public Option[] config() {
        Option[] bundleDependencies = bundlesToStart().stream()
                .map(this::startBundle)
                .toArray(Option[]::new);

        return options(karafConfiguration(), setupDistribution(), composite(bundleDependencies));
    }

    @After
    public void teardown() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
    }

    protected abstract Option setupDistribution();

    protected abstract List<BundleInfo> bundlesToStart();

    private Option startBundle(BundleInfo bundle) {
        return mavenBundle(bundle.groupId, bundle.artifactId).versionAsInProject()
                .start();
    }

    protected <T> void registerService(T service, Class<T> registerClass) {
        ServiceRegistration<T> registration = bundleContext.registerService(registerClass,
                service,
                null);
        serviceRegistrations.add(registration);
        await("service registration").atMost(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .until(() -> bundleContext.getServiceReference(registerClass) != null);
    }
}