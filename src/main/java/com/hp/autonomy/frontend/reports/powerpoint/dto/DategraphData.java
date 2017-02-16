/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DategraphData implements ComposableElement {

    long[] timestamps;

    List<Row> rows;

    public boolean validateInput() {
        final int length = this.timestamps.length;

        for(Row row : rows) {
            if (row.getValues().length != length) {
                return false;
            }
        }

        return length > 1 && !rows.isEmpty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        String color;
        String label;
        boolean secondaryAxis;
        double[] values;
    }
}