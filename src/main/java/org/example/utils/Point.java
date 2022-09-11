package org.example.utils;

import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Point {
    int x, y;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
