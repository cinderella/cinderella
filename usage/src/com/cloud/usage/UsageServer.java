// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.usage;

import org.apache.log4j.Logger;

import com.cloud.utils.component.ComponentLocator;

public class UsageServer {
    private static final Logger s_logger = Logger.getLogger(UsageServer.class.getName());
    public static final String Name = "usage-server";

    /**
     * @param args
     */
    public static void main(String[] args) {
        UsageServer usage = new UsageServer();
        usage.init(args);
        usage.start();
    }

    public void init(String[] args) {

    }

    public void start() {
        final ComponentLocator _locator = ComponentLocator.getLocator(UsageServer.Name, "usage-components.xml", "log4j-cloud_usage");
        UsageManager mgr = _locator.getManager(UsageManager.class);
        if (mgr != null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("UsageServer ready...");
            }
        }
    }

    public void stop() {

    }

    public void destroy() {

    }
}
