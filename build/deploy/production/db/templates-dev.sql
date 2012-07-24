# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (1, 'routing', 'DomR Template', 0, 'tank/volumes/demo/template/private/u000000/os/routing', now(), 'ext3', 0, 64, 1, 'http://vmopsserver.lab.vmops.com/images/routing/vmi-root-fc8-x86_64-domR.img.bz2', 'd00927f863a23b98cc6df6e377c9d0c6', 0, 'DomR Template', 0);
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (3, 'centos53-x86_64', 'Centos 5.3(x86_64) no GUI', 1, 'tank/volumes/demo/template/public/os/centos53-x86_64', now(), 'ext3', 0, 64, 1, 'http://vmopsserver.lab.vmops.com/images/centos52-x86_64/vmi-root-centos.5-2.64.pv.img.gz', 'd4ca80825d936db00eedf26620f13d69', 0, 'Centos 5.3(x86_64) no GUI', 0);
#INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
#    VALUES (4, 'centos52-x86_64-gui', 'Centos 5.2(x86_64) GUI', 1, 'tank/volumes/demo/template/public/os/centos52-x86_64-gui', now(), 'ext3', 0, 64, 1, 'http://vmopsserver.lab.vmops.com/images/centos52-x86_64/vmi-root-centos.5-2.64.pv.img.gz', 'd4ca80825d936db00eedf26620f13d69', 0, 'Centos 5.2(x86_64) GUI', 0);
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (5, 'winxpsp3', 'Windows XP SP3 (32-bit)', 1, 'tank/volumes/demo/template/public/os/winxpsp3', now(), 'ntfs', 1, 32, 1, 'http://vmopsserver.lab.vmops.com/images/fedora10-x86_64/vmi-root-fedora10.64.img.gz', 'c76d42703f14108b15acc9983307c759', 0, 'Windows XP SP3 (32-bit)', 0);
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (7, 'win2003sp2', 'Windows 2003 SP2 (32-bit)', 1, 'tank/volumes/demo/template/public/os/win2003sp2', now(), 'ntfs', 1, 32, 1, 'http://vmopsserver.lab.vmops.com/images/win2003sp2/vmi-root-win2003sp2.img.gz', '4d2cc51898d05c0f7a2852c15bcdc77b', 0, 'Windows 2003 SP2 (32-bit)', 0);
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (8, 'win2003sp2-x64', 'Windows 2003 SP2 (64-bit)', 1, 'tank/volumes/demo/template/public/os/win2003sp2-x64', now(), 'ntfs', 1, 64, 1, 'http://vmopsserver.lab.vmops.com/images/win2003sp2-x86_64/vmi-root-win2003sp2-x64.img.gz', '35d4de1c38eb4fb9d81a31c1d989c482', 0, 'Windows 2003 SP2 (64-bit)', 0);
INSERT INTO `vmops`.`vm_template` (id, unique_name, name, public, path, created, type, hvm, bits, created_by, url, checksum, ready, display_text, enable_password)
    VALUES (9, 'fedora12-GUI-x86_64', 'Fedora 12 Desktop(64-bit)', 1, 'tank/volumes/demo/template/public/os/fedora12-GUI-x86_64', now(), 'ext3', 1, 64, 1, 'http://vmopsserver.lab.vmops.com/images/fedora12-GUI-x86_64/vmi-root-fedora12-GUI-x86_64.qcow2.gz', '', 0, 'Fedora 12 Desktop (with httpd,java and mysql)', 0);
