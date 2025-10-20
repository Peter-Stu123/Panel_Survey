package org.isoft.panelsurvey.dto;

import java.util.List;

public class PolishedBackgroundDTO {
    private List<ComparisonRow> comparisonTable;
    private String sideEffects;
    private String additionalInfo;

    public List<ComparisonRow> getComparisonTable() {
        return comparisonTable;
    }

    public void setComparisonTable(List<ComparisonRow> comparisonTable) {
        this.comparisonTable = comparisonTable;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public static class ComparisonRow {
        private String category;
        private String intervention;
        private String comparison;

        public ComparisonRow() {
        }

        public ComparisonRow(String category, String intervention, String comparison) {
            this.category = category;
            this.intervention = intervention;
            this.comparison = comparison;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getIntervention() {
            return intervention;
        }

        public void setIntervention(String intervention) {
            this.intervention = intervention;
        }

        public String getComparison() {
            return comparison;
        }

        public void setComparison(String comparison) {
            this.comparison = comparison;
        }
    }
}

