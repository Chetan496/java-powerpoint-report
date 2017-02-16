/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextData implements ComposableElement {
    private Paragraph[] text;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Paragraph {
        private boolean bold;
        private boolean italic;
        private String text = "\n";
        private String color = "#000000";
        private double fontSize = 12;
    }

}