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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.cloud.bridge.service.EC2Engine;
import com.cloud.bridge.util.ConfigurationHelper;
import com.google.common.base.Supplier;

/**
 * @author Kelven Yang
 */
public enum JCloudsServiceProvider implements Supplier<EC2Engine> {
   INSTANCE;

   private final Logger logger;

   private final Properties properties;
   private final String serviceEndpoint;
   private final JCloudsEC2Engine EC2Engine;

   JCloudsServiceProvider() {
      logger = Logger.getLogger(JCloudsServiceProvider.class);
      properties = loadStartupProperties();
      serviceEndpoint = initialize(properties);

      // register service implementation object
      EC2Engine = new JCloudsEC2Engine();
   }

   @Override
   public EC2Engine get() {
      return EC2Engine;
   }

   public String getServiceEndpoint() {
      return serviceEndpoint;
   }

   public Properties getStartupProperties() {
      return properties;
   }

   private String initialize(Properties properties) {
      if (logger.isInfoEnabled())
         logger.info("Initializing JCloudsServiceProvider...");

      File file = ConfigurationHelper.findConfigurationFile("log4j-cloud.xml");
      if (file != null) {
         System.out.println("Log4j configuration from : " + file.getAbsolutePath());
         DOMConfigurator.configureAndWatch(file.getAbsolutePath(), 10000);
      } else {
         System.out.println("Configure log4j with default properties");
      }

      if (logger.isInfoEnabled())
         logger.info("ServiceProvider initialized");
      return (String) properties.get("serviceEndpoint");

   }

   private Properties loadStartupProperties() {
      File propertiesFile = ConfigurationHelper.findConfigurationFile("cloud-bridge.properties");
      Properties properties = new Properties();
      if (propertiesFile != null) {
         try {
            properties.load(new FileInputStream(propertiesFile));
         } catch (FileNotFoundException e) {
            logger.warn("Unable to open properties file: " + propertiesFile.getAbsolutePath(), e);
         } catch (IOException e) {
            logger.warn("Unable to read properties file: " + propertiesFile.getAbsolutePath(), e);
         }

         logger.info("Use startup properties file: " + propertiesFile.getAbsolutePath());
      } else {
         if (logger.isInfoEnabled())
            logger.info("Startup properties is not found.");
      }
      return properties;
   }

   public void shutdown() {
      if (logger.isInfoEnabled())
         logger.info("ServiceProvider stopped");
   }

}
