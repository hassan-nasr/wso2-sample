package org.wso2.carbon.event.input.adaptor.manager.admin.internal.util;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;

/**
 * This class is used to hold the event adaptor service
 */
public final class InputEventAdaptorHolder {
    private InputEventAdaptorService eventAdaptorService;
    private static InputEventAdaptorHolder instance = new InputEventAdaptorHolder();

    private InputEventAdaptorHolder() {
    }

    public InputEventAdaptorService getEventAdaptorService() {
        return eventAdaptorService;
    }

    public static InputEventAdaptorHolder getInstance() {
        return instance;
    }

    public void registerEventService(InputEventAdaptorService eventService) {
        this.eventAdaptorService = eventService;
    }

    public void unRegisterEventService(InputEventAdaptorService eventService) {
        this.eventAdaptorService = null;
    }
}
