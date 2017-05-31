package ru.otus.chepiov.l8;

/**
 * Test model.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class Model {

    private String firstField;
    private int secondField;
    private transient String transientField;

    public void setFirstField(String firstField) {
        this.firstField = firstField;
    }

    public void setSecondField(int secondField) {
        this.secondField = secondField;
    }

    public void setTransientField(String transientField) {
        this.transientField = transientField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (secondField != model.secondField) return false;
        return firstField != null ? firstField.equals(model.firstField) : model.firstField == null;
    }

    @Override
    public int hashCode() {
        int result = firstField != null ? firstField.hashCode() : 0;
        result = 31 * result + secondField;
        return result;
    }

    @Override
    public String toString() {
        return "Model{" +
                "firstField='" + firstField + '\'' +
                ", secondField=" + secondField +
                ", transientField='" + transientField + '\'' +
                '}';
    }
}
