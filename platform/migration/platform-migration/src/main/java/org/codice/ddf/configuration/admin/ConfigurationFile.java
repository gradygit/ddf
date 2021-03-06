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

package org.codice.ddf.configuration.admin;

import static org.apache.commons.lang.Validate.notNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Dictionary;

import javax.validation.constraints.NotNull;

import org.codice.ddf.configuration.persistence.PersistenceStrategy;
import org.codice.ddf.configuration.status.ConfigurationFileException;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Base class for configuration file objects. Configuration file objects can be created using the
 * {@link ConfigurationFileFactory} class.
 */
public abstract class ConfigurationFile {

    final Dictionary<String, Object> properties;

    final ConfigurationAdmin configAdmin;

    private final PersistenceStrategy persistenceStrategy;

    /**
     * Constructor called by {@link ConfigurationFileFactory}. Assumes that none of the parameters
     * are {@code null}.
     *  @param properties          properties associated with the configuration file
     * @param configAdmin         reference to OSGi's {@link ConfigurationAdmin}
     * @param persistenceStrategy how to write out the file {@link PersistenceStrategy}
     */
    ConfigurationFile(Dictionary<String, Object> properties, ConfigurationAdmin configAdmin,
            PersistenceStrategy persistenceStrategy) {
        this.configAdmin = configAdmin;
        this.properties = properties;
        this.persistenceStrategy = persistenceStrategy;
    }

    public abstract void createConfig() throws ConfigurationFileException;

    public void exportConfig(@NotNull String destination) throws IOException {
        notNull(destination, "destination cannot be null");
        try (FileOutputStream fileOutputStream = getOutputStream(destination)) {
            persistenceStrategy.write(fileOutputStream, properties);
        }
    }

    FileOutputStream getOutputStream(String destination) throws FileNotFoundException {
        return new FileOutputStream(destination);
    }

    /**
     * Provides a convenient way to construct {@link ConfigurationFile}.
     */
    protected abstract static class ConfigurationFileBuilder {
        final ConfigurationAdmin configAdmin;

        Dictionary<String, Object> properties = null;

        final PersistenceStrategy persistenceStrategy;

        /**
         * Constructs a ConfigurationFileBuilder.
         *
         * @param configAdmin         reference to OSGi's {@link ConfigurationAdmin}
         * @param persistenceStrategy how to write out the file {@link PersistenceStrategy}
         */
        public ConfigurationFileBuilder(ConfigurationAdmin configAdmin,
                PersistenceStrategy persistenceStrategy) {
            this.configAdmin = configAdmin;
            this.persistenceStrategy = persistenceStrategy;
        }

        public ConfigurationFileBuilder properties(Dictionary<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public abstract ConfigurationFile build();
    }
}
