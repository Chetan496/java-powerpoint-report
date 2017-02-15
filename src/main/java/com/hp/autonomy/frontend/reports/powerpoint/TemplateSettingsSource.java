/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint;

public interface TemplateSettingsSource {
    TemplateSettings getSettings();

    TemplateSettingsSource DEFAULT = TemplateSettings::new;
}
