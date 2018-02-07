package bio.fkaiser.fit3d.web.model.constant;

public enum MotifComplexity {

    LOW("low"), MEDIUM("medium"), HIGH("high");

    private String label;

    MotifComplexity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
