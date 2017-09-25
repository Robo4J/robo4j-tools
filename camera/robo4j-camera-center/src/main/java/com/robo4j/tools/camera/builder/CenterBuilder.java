/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.tools.camera.builder;

import com.robo4j.tools.camera.enums.SupportedConfigElements;
import com.robo4j.tools.camera.model.CameraCenterProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CenterBuilder {

    private Map<SupportedConfigElements, String> configMap = new HashMap<>();

    private SupportedConfigElements currentElement;
    private class CenterXMLHandler extends DefaultHandler {

        private boolean parsing = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentElement = SupportedConfigElements.byName(qName);
            if(currentElement != null){
                parsing = true;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(parsing){
                configMap.put(currentElement,new String(ch, start, length) );
                parsing = false;
            }
        }
    }

    public CenterBuilder add(InputStream inputStream) throws CenterBuilderException {
        try{
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(inputStream, new CenterXMLHandler());

        } catch (SAXException | IOException | ParserConfigurationException e){
            throw new CenterBuilderException("Could not initiate from xml", e);
        }

        return this;
    }

    public CameraCenterProperties build(){
        return new CameraCenterProperties(configMap);
    }
}
