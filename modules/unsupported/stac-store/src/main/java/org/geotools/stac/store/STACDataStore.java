/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2022, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.stac.store;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.stac.client.STACClient;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;

/**
 * A {@link org.geotools.data.DataStore} implementation connecting to a <a
 * href="https://github.com/radiantearth/stac-api-spec">STAC API</a>, exposing collections as
 * feature types, and items as features.
 */
public class STACDataStore extends ContentDataStore {

    static final int DEFAULT_FETCH_SIZE = 1000;

    static final Logger LOGGER = Logging.getLogger(STACDataStore.class);

    private final STACClient client;

    private STACClient.SearchMode searchMode = STACClient.SearchMode.GET;

    private int fetchSize = DEFAULT_FETCH_SIZE;

    public STACDataStore(STACClient client) {
        this.client = client;
    }

    public STACClient.SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(STACClient.SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        return client.getCollections().stream()
                .map(c -> new NameImpl(namespaceURI, c.getId()))
                .collect(Collectors.toList());
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new STACFeatureSource(entry, client, searchMode, fetchSize);
    }

    @Override
    public void dispose() {
        try {
            if (client != null) client.close();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Failed to cleanly close the STAC client", e);
        }
    }
}