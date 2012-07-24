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
package com.cloud.network;

import java.net.URI;
import java.net.URISyntaxException;

import com.cloud.utils.exception.CloudRuntimeException;

/**
 * Network includes all of the enums used within networking.
 * 
 */
public class Networks {

    public enum RouterPrivateIpStrategy {
        None,
        DcGlobal, // global to data center
        HostLocal;

        public static String DummyPrivateIp = "169.254.1.1";
    }

    /**
     * Different ways to assign ip address to this network.
     */
    public enum Mode {
        None,
        Static,
        Dhcp,
        ExternalDhcp;
    };

    public enum AddressFormat {
        Ip4,
        Ip6,
        Mixed
    }

    /**
     * Different types of broadcast domains.
     */
    public enum BroadcastDomainType {
        Native(null, null),
        Vlan("vlan", Integer.class),
        Vswitch("vs", String.class),
        LinkLocal(null, null),
        Vnet("vnet", Long.class),
        Storage("storage", Integer.class),
        Lswitch("lswitch", String.class),
        UnDecided(null, null);

        private String scheme;
        private Class<?> type;

        private BroadcastDomainType(String scheme, Class<?> type) {
            this.scheme = scheme;
            this.type = type;
        }

        /**
         * @return scheme to be used in broadcast uri. Null indicates that this type does not have broadcast tags.
         */
        public String scheme() {
            return scheme;
        }

        /**
         * @return type of the value in the broadcast uri. Null indicates that this type does not have broadcast tags.
         */
        public Class<?> type() {
            return type;
        }

        public <T> URI toUri(T value) {
            try {
                return new URI(scheme + "://" + value);
            } catch (URISyntaxException e) {
                throw new CloudRuntimeException("Unable to convert to broadcast URI: " + value);
            }
        }
    };

    /**
     * Different types of network traffic in the data center.
     */
    public enum TrafficType {
        None,
        Public,
        Guest,
        Storage,
        Management,
        Control,
        Vpn;

        public static boolean isSystemNetwork(TrafficType trafficType) {
            if (Storage.equals(trafficType)
                    || Management.equals(trafficType)
                    || Control.equals(trafficType)) {
                return true;
            }
            return false;
        }

        public static TrafficType getTrafficType(String type) {
            if ("Public".equals(type)) {
                return Public;
            } else if ("Guest".equals(type)) {
                return Guest;
            } else if ("Storage".equals(type)) {
                return Storage;
            } else if ("Management".equals(type)) {
                return Management;
            } else if ("Control".equals(type)) {
                return Control;
            } else if ("Vpn".equals(type)) {
                return Vpn;
            } else {
                return None;
            }
        }
    };

    public enum IsolationType {
        None(null, null),
        Ec2("ec2", String.class),
        Vlan("vlan", Integer.class),
        Vswitch("vs", String.class),
        Undecided(null, null),
        Vnet("vnet", Long.class);

        private final String scheme;
        private final Class<?> type;

        private IsolationType(String scheme, Class<?> type) {
            this.scheme = scheme;
            this.type = type;
        }

        public String scheme() {
            return scheme;
        }

        public Class<?> type() {
            return type;
        }

        public <T> URI toUri(T value) {
            try {
                // assert(this!=Vlan || value.getClass().isAssignableFrom(Integer.class)) :
                // "Why are you putting non integer into vlan url";
                return new URI(scheme + "://" + value.toString());
            } catch (URISyntaxException e) {
                throw new CloudRuntimeException("Unable to convert to isolation type URI: " + value);
            }
        }
    }

    public enum BroadcastScheme {
        Vlan("vlan"),
        VSwitch("vswitch");

        private String scheme;

        private BroadcastScheme(String scheme) {
            this.scheme = scheme;
        }

        @Override
        public String toString() {
            return scheme;
        }
    }
}
