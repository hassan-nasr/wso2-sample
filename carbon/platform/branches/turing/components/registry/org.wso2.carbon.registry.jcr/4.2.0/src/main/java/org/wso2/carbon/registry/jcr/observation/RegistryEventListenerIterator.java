/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.jcr.observation;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RegistryEventListenerIterator implements EventListenerIterator {

    private Set<Event> eventListners = new HashSet<Event>();
    private Iterator it;
    private long counter = 0;

    public RegistryEventListenerIterator(Set set) {

        this.eventListners = set;
        this.it = set.iterator();

    }

    public EventListener nextEventListener() {

        return (EventListener) next();
    }

    public void skip(long l) {

        counter = l;
    }

    public long getSize() {

        return eventListners.size();
    }

    public long getPosition() {

        return counter;
    }

    public boolean hasNext() {

        return it.hasNext();
    }

    public Object next() {

        counter++;
        return it.next();
    }

    public void remove() {

        it.remove();
    }
}
