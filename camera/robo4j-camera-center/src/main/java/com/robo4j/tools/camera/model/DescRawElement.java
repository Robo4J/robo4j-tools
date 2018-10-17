/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.camera.model;

import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DescRawElement {

    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE = "value";
    public static final String KEY_VALUE2 = "value2";
    public static final String KEY_DESC = "desc";

    private String name;
    private String value;
    private String value2;
    private String desc;

    public DescRawElement(String name, String value, String value2, String desc) {
        this.name = name;
        this.value = value;
        this.value2 = value2;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getValue2() {
        return value2;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DescRawElement that = (DescRawElement) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(value2, that.value2) &&
                Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, value2, desc);
    }

    @Override
    public String toString() {
        return "DescRawElement{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", value2='" + value2 + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
