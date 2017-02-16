/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint.dto;

import lombok.Data;

@Data
public class MapData implements ComposableElement {

    private String image;
    private Marker[] markers;

    @Data
    public static class Marker {
        private double x, y;
        private String text;
        private boolean cluster;
        private String color;
        private String fontColor;
        private boolean fade;
    }
}

