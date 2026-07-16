package com.example.equipment.utils.ksuuid;

import com.github.ksuid.KsuidGenerator;
import jakarta.persistence.Embeddable;

import java.security.SecureRandom;
import java.util.Objects;

@Embeddable
public class KsuidVersion implements Comparable<KsuidVersion> {

    private KsuidGenerator generator;

    private String value;

    public KsuidVersion() {
        this.generator = new KsuidGenerator(new SecureRandom());
        this.value = generator.newKsuid().toString();
    }

    public KsuidVersion(String value) {
        this.generator = new KsuidGenerator(new SecureRandom());
        this.value = value;
    }

    public KsuidVersion(KsuidGenerator generator, String value) {
        this.generator = generator;
        this.value = value;
    }

    public KsuidVersion increment() {
        return new KsuidVersion(this.generator, generator.newKsuid().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KsuidVersion that = (KsuidVersion) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int compareTo(KsuidVersion o2) {
        if (this == o2) return 0;
        if (this == null) return -1;
        if (o2 == null) return 1;
        return this.toString().compareTo(o2.toString());
    }

}