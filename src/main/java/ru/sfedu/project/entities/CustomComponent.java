package ru.sfedu.project.entities;

import jakarta.persistence.Embeddable;

@Embeddable
public class CustomComponent {
    private String field1;
    private Integer field2;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public Integer getField2() {
        return field2;
    }

    public void setField2(Integer field2) {
        this.field2 = field2;
    }
}