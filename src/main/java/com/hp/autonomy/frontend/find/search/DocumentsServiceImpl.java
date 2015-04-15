/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.search;

import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.iod.client.api.search.*;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentsServiceImpl implements DocumentsService {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private QueryTextIndexService queryTextIndexService;

    @Override
    public Documents queryTextIndex(final String text, final int maxResults, final Summary summary, final List<String> indexes) throws IodErrorException {

        final Map<String, Object> params = new QueryRequestBuilder()
                .setAbsoluteMaxResults(maxResults)
                .setSummary(summary)
                .setIndexes(indexes)
                .build();

        return queryTextIndexService.queryTextIndexWithText(apiKeyService.getApiKey(), text, params);
    }
}
