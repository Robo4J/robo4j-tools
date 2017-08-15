/*
 * Copyright (C) 2017. Miroslav Kopecky
 * This MainTest.java  is part of robo4j.
 * path: /Users/miroslavkopecky/GiTHub_MiroKopecky/robo4j-tools/compiler/robo4j-center/src/test/java/com/robo4j/tools/center/MainTest.java
 * module: robo4j-center_main
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.tools.center;

import org.junit.Test;

import com.robo4j.tools.center.builder.CenterBuilder;
import com.robo4j.tools.center.model.CenterProperties;
import com.sun.tools.javac.util.Assert;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterBuilderTest {

    @Test
    public void test() throws Exception {

        CenterBuilder builder = new CenterBuilder().add(getClass().getClassLoader().getResourceAsStream("robo4jCenter.xml"));
        CenterProperties centerProperties = builder.build();
        Assert.checkNonNull(centerProperties);
        Assert.check(centerProperties.getMainPackage().equals("com.robo4j.lego.j1kids.example"));
        Assert.check(centerProperties.getMainClass().equals("Number42Main"));
        Assert.check(centerProperties.getRobo4jLibrary().equals("robo4j-units-lego-alpha-0.3.jar"));
        Assert.check(centerProperties.getOutDirectory().equals("out"));
        Assert.check(centerProperties.getCenterActions().equals("upload,compile"));
        Assert.check(centerProperties.getDeviceIP().equals("10.0.1.1"));
        Assert.check(centerProperties.getDeviceType().equals("lego"));
    }

}
