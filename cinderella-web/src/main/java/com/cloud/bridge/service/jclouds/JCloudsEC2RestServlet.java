// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.bridge.service.jclouds;

import com.cloud.bridge.service.EC2Engine;
import com.cloud.bridge.service.EC2RestServlet;

public class JCloudsEC2RestServlet extends EC2RestServlet {

   private static final long serialVersionUID = -1940325245216425159L;

   @Override
   public EC2Engine get() {
      return JCloudsServiceProvider.INSTANCE.get();
   }

}
